package org.smm.archetype.infrastructure._shared.event;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.event.ConsumeStatus;
import org.smm.archetype.domain._shared.event.EventPriority;
import org.smm.archetype.domain._shared.event.EventStatus;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventConsumeDO;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventPublishDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventConsumeMapper;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 事件重试调度器
 *
 * <p>定时扫描需要重试的事件，按照优先级分配处理资源。
 *
 * <p>调度策略：
 * <ul>
 *   <li>高优先级事件：80%的处理资源</li>
 *   <li>低优先级事件：20%的处理资源</li>
 *   <li>按创建时间倒序排列（优先处理旧事件）</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventRetryScheduler {

    private final EventConsumeMapper eventConsumeMapper;
    private final EventPublishMapper eventPublishMapper;
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * 每次处理的批次大小
     */
    private static final int BATCH_SIZE = 50;

    /**
     * 定时重试任务
     *
     * <p>每分钟执行一次，扫描需要重试的事件
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void retryFailedEvents() {
        log.debug("Starting event retry scheduler...");

        try {
            // 1. 重试消费失败的事件
            retryFailedConsumingEvents();

            // 2. 重试发布失败的事件
            retryFailedPublishingEvents();

        } catch (Exception e) {
            log.error("Error in event retry scheduler", e);
        }

        log.debug("Event retry scheduler completed");
    }

    /**
     * 重试消费失败的事件
     */
    private void retryFailedConsumingEvents() {
        log.debug("Retrying failed consuming events...");

        // 1. 获取需要重试的事件（按优先级分配）
        List<EventConsumeDO> highPriorityEvents = getRetryEventsByPriority(EventPriority.HIGH);
        List<EventConsumeDO> lowPriorityEvents = getRetryEventsByPriority(EventPriority.LOW);

        // 2. 按 80:20 分配处理资源
        int highCount = (int) (BATCH_SIZE * 0.8);
        int lowCount = BATCH_SIZE - highCount;

        // 3. 处理高优先级事件
        processRetryEvents(highPriorityEvents, highCount);

        // 4. 处理低优先级事件
        processRetryEvents(lowPriorityEvents, lowCount);
    }

    /**
     * 重试发布失败的事件
     */
    private void retryFailedPublishingEvents() {
        log.debug("Retrying failed publishing events...");

        List<EventPublishDO> failedEvents = eventPublishMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status = ?", EventStatus.CREATED.name())
                        .orderBy("occurred_on", false) // 倒序，优先处理旧事件
                        .limit(BATCH_SIZE)
        );

        log.info("Found {} failed publishing events to retry", failedEvents.size());

        for (EventPublishDO eventDO : failedEvents) {
            try {
                kafkaEventPublisher.republish(eventDO);
            } catch (Exception e) {
                log.error("Error republishing event: eventId={}", eventDO.getEventId(), e);
            }
        }
    }

    /**
     * 根据优先级获取需要重试的事件
     * @param priority 优先级
     * @return 事件列表
     */
    private List<EventConsumeDO> getRetryEventsByPriority(EventPriority priority) {
        return eventConsumeMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("consume_status = ? AND priority = ? AND next_retry_time <= ?",
                                ConsumeStatus.RETRY.name(),
                                priority.name(),
                                Instant.now())
                        .orderBy("create_time", false) // 倒序，优先处理旧事件
                        .limit(BATCH_SIZE)
        );
    }

    /**
     * 处理重试事件
     * @param events 事件列表
     * @param maxCount 最大处理数量
     */
    private void processRetryEvents(List<EventConsumeDO> events, int maxCount) {
        int count = Math.min(events.size(), maxCount);
        if (count == 0) {
            return;
        }

        log.info("Processing {} retry events (priority={})", count,
                events.isEmpty() ? "UNKNOWN" : events.get(0).getPriority());

        for (int i = 0; i < count; i++) {
            EventConsumeDO eventDO = events.get(i);
            try {
                // TODO: 这里需要调用对应的 EventHandler 重新处理事件
                // 暂时记录日志
                log.info("Retrying event: eventId={}, retryTimes={}/{}",
                        eventDO.getEventId(),
                        eventDO.getRetryTimes(),
                        eventDO.getMaxRetryTimes());

                // 检查是否超过最大重试次数
                if (eventDO.getRetryTimes() >= eventDO.getMaxRetryTimes()) {
                    // 标记为失败
                    markAsFailed(eventDO);
                } else {
                    // 更新重试时间和次数
                    updateRetryInfo(eventDO);
                }

            } catch (Exception e) {
                log.error("Error processing retry event: eventId={}", eventDO.getEventId(), e);
            }
        }
    }

    /**
     * 标记事件为失败
     */
    private void markAsFailed(EventConsumeDO eventDO) {
        EventConsumeDO update = new EventConsumeDO();
        update.setConsumeStatus(ConsumeStatus.FAILED.name());
        update.setErrorMessage("Max retry times exceeded");

        eventConsumeMapper.updateByQuery(
                update,
                QueryWrapper.create().where("id = ?", eventDO.getId())
        );

        log.warn("Event marked as failed: eventId={}", eventDO.getEventId());

        // TODO: 调用失败处理钩子（根据事件类型执行不同操作）
    }

    /**
     * 更新重试信息
     */
    private void updateRetryInfo(EventConsumeDO eventDO) {
        EventConsumeDO update = new EventConsumeDO();
        update.setNextRetryTime(calculateNextRetryTime(eventDO.getRetryTimes() + 1));

        eventConsumeMapper.updateByQuery(
                update,
                QueryWrapper.create().where("id = ?", eventDO.getId())
        );
    }

    /**
     * 计算下次重试时间
     * <p>使用指数退避策略 + 随机抖动
     * @param retryTimes 当前重试次数
     * @return 下次重试时间
     */
    private Instant calculateNextRetryTime(int retryTimes) {
        // 指数退避：1分钟, 2分钟, 4分钟, 8分钟, ...
        long delayMinutes = (long) Math.pow(2, retryTimes - 1);

        // 添加随机抖动（±20%），避免惊群效应
        double jitter = ThreadLocalRandom.current().nextDouble(0.8, 1.2);
        delayMinutes = (long) (delayMinutes * jitter);

        return Instant.now().plusSeconds(delayMinutes * 60);
    }

}
