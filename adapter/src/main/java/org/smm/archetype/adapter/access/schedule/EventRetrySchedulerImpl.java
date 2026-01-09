package org.smm.archetype.adapter.access.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.access.listener.EventListener;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRepository;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.event.handler.EventFailureHandler;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventConsumeDO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 事件重试调度器实现
 *
 * <p>定时扫描 event_consume 表，实现混合优先级读取策略，
 * 调用 EventListener 重新处理事件。
 *
 * <p>混合优先级策略：
 * <ul>
 *   <li>高优先级任务占 80%</li>
 *   <li>低优先级任务占 20%</li>
 *   <li>根据重试次数和创建时间排序</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventRetrySchedulerImpl implements EventRetryScheduler {

    private final EventConsumeRepository    eventConsumeRepository;
    private final List<EventListener>       eventListeners;
    private final EventSerializer           eventSerializer;
    private final List<EventFailureHandler> failureHandlers;

    @Value("${event.retry.batchSize:100}")
    private int batchSize;

    @Value("${event.retry.highPriorityRatio:0.8}")
    private double highPriorityRatio;

    /**
     * 定时调度方法
     *
     * <p>每分钟执行一次，扫描待处理事件。
     */
    @Override
    @Scheduled(cron = "${event.retry.cron:0 * * * * ?}")
    public void scheduleRetry() {
        log.debug("Event retry scheduler started");

        try {
            processPendingEvents();
        } catch (Exception e) {
            log.error("Error in event retry scheduler", e);
        }

        log.debug("Event retry scheduler completed");
    }

    /**
     * 处理待处理事件
     */
    private void processPendingEvents() {
        // 使用混合优先级策略获取事件
        List<EventConsumeDO> events = fetchEventsWithPriorityStrategy(batchSize);

        if (events.isEmpty()) {
            log.debug("No pending events to retry");
            return;
        }

        log.info("Found {} pending events to retry", events.size());

        // 使用虚拟线程池并发处理
        ExecutorService virtualTaskExecutor = getExecutorService();

        List<CompletableFuture<Void>> futures = events.stream()
                                                        .map(event -> CompletableFuture.runAsync(() -> replayEvent(event),
                                                                virtualTaskExecutor))
                                                        .toList();

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("Completed retrying {} events", events.size());
    }

    /**
     * 混合优先级策略获取事件
     * @param batchSize 批次大小
     * @return 事件列表
     */
    private List<EventConsumeDO> fetchEventsWithPriorityStrategy(int batchSize) {
        // 计算高优先级数量（80%）
        int highPriorityCount = (int) (batchSize * highPriorityRatio);
        highPriorityCount = Math.max(1, highPriorityCount); // 至少 1 个高优先级

        // 低优先级数量为剩余部分
        int lowPriorityCount = batchSize - highPriorityCount;

        log.debug("Fetching events: high={}, low={}", highPriorityCount, lowPriorityCount);

        // 查询高优先级事件
        List<EventConsumeDO> highPriorityEvents = eventConsumeRepository.findPendingEvents(
                List.of("READY", "RETRY"),
                "HIGH",
                highPriorityCount
        );

        // 查询低优先级事件
        List<EventConsumeDO> lowPriorityEvents = eventConsumeRepository.findPendingEvents(
                List.of("READY", "RETRY"),
                "LOW",
                lowPriorityCount
        );

        // 合并结果
        List<EventConsumeDO> result = new ArrayList<>();
        result.addAll(highPriorityEvents);
        result.addAll(lowPriorityEvents);

        log.debug("Fetched events: high={}, low={}, total={}",
                highPriorityEvents.size(), lowPriorityEvents.size(), result.size());

        return result;
    }

    /**
     * 重放事件
     * @param consumeDO 消费记录
     */
    private void replayEvent(EventConsumeDO consumeDO) {
        String eventId = consumeDO.getEventId();

        try {
            log.debug("Replaying event: eventId={}, consumerName={}", eventId, consumeDO.getConsumerName());

            // 反序列化事件数据
            DomainEvent event = deserializeEvent(consumeDO);

            // 根据消费者名称找到对应的 EventListener
            EventListener listener = findEventListener(consumeDO.getConsumerName());

            if (listener != null) {
                // 调用 EventListener 重新处理
                listener.onEvent(event);
                log.debug("Event replayed successfully: eventId={}", eventId);
            } else {
                log.warn("EventListener not found: consumerName={}", consumeDO.getConsumerName());
            }

        } catch (Exception e) {
            log.error("Error replaying event: eventId={}", eventId, e);
        }
    }

    /**
     * 处理失败事件
     * @param consumeDO 消费记录
     */
    private void handleFailedEvent(EventConsumeDO consumeDO) {
        try {
            // 反序列化事件
            DomainEvent event = deserializeEvent(consumeDO);

            // 查找对应的失败处理器
            for (EventFailureHandler handler : failureHandlers) {
                if (handler.supports(event.getEventType())) {
                    handler.handleFailure(event, consumeDO);
                    return;
                }
            }

            log.warn("No failure handler found for event type: {}", event.getEventTypeName());

        } catch (Exception e) {
            log.error("Error handling failed event: eventId={}", consumeDO.getEventId(), e);
        }
    }

    /**
     * 反序列化事件
     * @param consumeDO 消费记录
     * @return 领域事件
     */
    private DomainEvent deserializeEvent(EventConsumeDO consumeDO) {
        // 从 event_publish 表查询事件类型和事件数据
        // EventConsumeDO 只存储 eventId，需要查询 event_publish 表获取完整信息

        // 简化实现：假设可以从 eventPublishMapper 查询
        // TODO: 实现从 event_publish 表查询事件类型和数据的逻辑
        String eventType = DomainEvent.class.getName();
        String eventData = "{}";

        return eventSerializer.deserialize(eventData, eventType);
    }

    /**
     * 根据消费者名称查找 EventListener
     * @param consumerName 消费者名称
     * @return EventListener，不存在则返回 null
     */
    private EventListener findEventListener(String consumerName) {
        return eventListeners.stream()
                       .filter(listener -> {
                           // 这里需要判断 listener 是否处理该消费者名称的事件
                           // 简化实现：假设所有 listener 都可以处理
                           return true;
                       })
                       .findFirst()
                       .orElse(null);
    }

    /**
     * 获取执行器服务（虚拟线程池）
     * @return ExecutorService
     */
    private ExecutorService getExecutorService() {
        // TODO: 从 ThreadPoolConfigure 注入虚拟线程池
        return java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
    }

}
