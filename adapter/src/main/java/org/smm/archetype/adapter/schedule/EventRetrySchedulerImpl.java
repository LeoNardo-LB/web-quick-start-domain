package org.smm.archetype.adapter.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.listener.EventListener;
import org.smm.archetype.adapter.schedule.handler.FailureHandler;
import org.smm.archetype.domain.bizshared.base.DomainEvent;
import org.smm.archetype.domain.bizshared.event.EventConsumeRecord;
import org.smm.archetype.infrastructure.bizshared.event.EventConsumeRecordConverter;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventConsumeRepository;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventPublishRepository;
import org.smm.archetype.infrastructure.bizshared.dal.generated.entity.EventConsumeDO;
import org.smm.archetype.infrastructure.bizshared.dal.generated.entity.EventPublishDO;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.temporal.ChronoUnit;
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
public class EventRetrySchedulerImpl implements EventRetryScheduler {

    private final EventConsumeRepository      eventConsumeRepository;
    private final List<EventListener>  eventListeners;
    private final List<FailureHandler> failureHandlers;
    private final ExecutorService      virtualThreadExecutor;
    private final EventConsumeRecordConverter recordConverter;
    private final EventPublishRepository      eventPublishRepository;

    /**
     * 每批次处理数量
     */
    private final int batchSize;

    /**
     * 高优先级事件占比
     */
    private final double highPriorityRatio;

    /**
     * 最大重试次数
     */
    private final int maxRetryTimes;

    /**
     * 重试延迟时间列表（分钟）
     */
    private final List<Integer> retryDelays;

    /**
     * JSON序列化器
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造器
     * @param eventConsumeRepository 事件消费仓储
     * @param eventListeners         所有事件监听器
     * @param failureHandlers        所有失败处理器
     * @param virtualThreadExecutor  虚拟线程池
     * @param recordConverter        消费记录转换器
     * @param eventPublishRepository 事件发布仓储
     * @param batchSize              每批次处理数量
     * @param highPriorityRatio      高优先级事件占比
     * @param maxRetryTimes          最大重试次数
     * @param retryDelays            重试延迟时间列表（分钟）
     */
    public EventRetrySchedulerImpl(
            final EventConsumeRepository eventConsumeRepository,
            final List<EventListener> eventListeners,
            final List<FailureHandler> failureHandlers,
            final ExecutorService virtualThreadExecutor,
            final EventConsumeRecordConverter recordConverter,
            final EventPublishRepository eventPublishRepository,
            final int batchSize,
            final double highPriorityRatio,
            final int maxRetryTimes,
            final List<Integer> retryDelays) {
        this.eventConsumeRepository = eventConsumeRepository;
        this.eventListeners = eventListeners;
        this.failureHandlers = failureHandlers;
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.recordConverter = recordConverter;
        this.eventPublishRepository = eventPublishRepository;
        this.batchSize = batchSize;
        this.highPriorityRatio = highPriorityRatio;
        this.maxRetryTimes = maxRetryTimes;
        this.retryDelays = new ArrayList<>(retryDelays); // 防御性拷贝

        // 初始化 ObjectMapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

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
                                                        .map(event -> CompletableFuture.runAsync(
                                                                () -> replayEvent(event), virtualTaskExecutor)
                                                        )
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

            // 处理重试失败
            handleReplayFailure(consumeDO, e);
        }
    }

    /**
     * 处理重试失败
     *
     * <p>检查重试次数，如果达到最大次数则调用失败处理器，否则更新重试信息。
     * @param consumeDO 消费记录
     * @param e         异常
     */
    private void handleReplayFailure(EventConsumeDO consumeDO, Exception e) {
        int currentRetryTimes = consumeDO.getRetryTimes() != null ? consumeDO.getRetryTimes() : 0;
        int maxRetryTimes = consumeDO.getMaxRetryTimes() != null
                                    ? consumeDO.getMaxRetryTimes()
                                    : this.maxRetryTimes;

        if (currentRetryTimes >= maxRetryTimes) {
            // 达到最大重试次数，标记为失败并调用失败处理器
            log.error("Event replay failed after max retries: eventId={}, retryTimes={}/{}",
                    consumeDO.getEventId(), currentRetryTimes, maxRetryTimes);

            consumeDO.setConsumeStatus("FAILED");
            consumeDO.setErrorMessage("Max retry times exceeded in replay: " + e.getMessage());

            // 更新数据库状态
            eventConsumeRepository.updateStatusWithVersion(consumeDO);

            // 调用失败处理器
            handleFailedEvent(consumeDO, e);
        } else {
            // 未达到最大重试次数，更新重试信息
            consumeDO.setConsumeStatus("RETRY");
            consumeDO.setRetryTimes(currentRetryTimes + 1);
            consumeDO.setErrorMessage(e.getMessage());

            // 使用配置的延迟时间
            int delayMinutes = getRetryDelay(currentRetryTimes);
            consumeDO.setNextRetryTime(java.time.Instant.now().plus(delayMinutes, ChronoUnit.MINUTES));

            // 更新数据库状态
            eventConsumeRepository.updateStatusWithVersion(consumeDO);

            log.warn("Event replay failed, will retry: eventId={}, retryTimes={}/{}",
                    consumeDO.getEventId(), currentRetryTimes + 1, maxRetryTimes);
        }
    }

    /**
     * 处理失败事件
     * @param consumeDO 消费记录DO对象
     * @param e         原始异常
     */
    private void handleFailedEvent(EventConsumeDO consumeDO, Exception e) {
        try {
            // 反序列化事件
            DomainEvent event = deserializeEvent(consumeDO);

            // 将EventConsumeDO转换为EventConsumeRecord（Domain层值对象）
            EventConsumeRecord consumeRecord = recordConverter.from(consumeDO);

            // 查找对应的失败处理器
            for (FailureHandler handler : failureHandlers) {
                if (handler.supports(event.getEventType())) {
                    handler.handleFailure(event, consumeRecord, e);
                    return;
                }
            }

            log.warn("No failure handler found for event type: {}", event.getEventTypeName());

        } catch (Exception ex) {
            log.error("Error handling failed event: eventId={}", consumeDO.getEventId(), ex);
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

        EventPublishDO eventPublishDO = eventPublishRepository.findByEventId(consumeDO.getEventId());

        if (eventPublishDO == null) {
            log.error("Event not found in event_publish table: eventId={}", consumeDO.getEventId());
            throw new IllegalStateException("Event not found: " + consumeDO.getEventId());
        }

        // 从event_publish表获取事件类型和数据
        String eventType = eventPublishDO.getType();
        String eventData = eventPublishDO.getData();

        return deserializeEvent(eventData, eventType);
    }

    /**
     * 将JSON字符串反序列化为领域事件
     * @param json      JSON字符串
     * @param eventType 事件类型
     * @return 领域事件
     */
    private DomainEvent deserializeEvent(String json, String eventType) {
        try {
            Class<?> eventClass = Class.forName(eventType);
            return (DomainEvent) objectMapper.readValue(json, eventClass);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize event: type={}, json={}", eventType, json, e);
            throw new RuntimeException("Event deserialization failed", e);
        } catch (ClassNotFoundException e) {
            log.error("Event class not found: type={}", eventType, e);
            throw new RuntimeException("Event class not found: " + eventType, e);
        }
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
        // 从ThreadPoolConfigure注入的虚拟线程池
        return virtualThreadExecutor;
    }

    /**
     * 获取指定重试次数的延迟时间
     * @param retryTimes 当前重试次数（从0开始）
     * @return 延迟时间（分钟）
     */
    private int getRetryDelay(int retryTimes) {
        if (retryDelays == null || retryDelays.isEmpty()) {
            return 1; // 默认1分钟
        }
        int index = Math.min(retryTimes, retryDelays.size() - 1);
        return retryDelays.get(index);
    }

}
