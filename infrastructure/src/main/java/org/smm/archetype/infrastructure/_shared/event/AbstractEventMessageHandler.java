package org.smm.archetype.infrastructure._shared.event;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.ConsumeStatus;
import org.smm.archetype.domain._shared.event.EventMessageHandler;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventConsumeDO;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventPublishDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventConsumeMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 抽象事件消息处理器
 *
 * <p>提供事件消费的通用实现，包括：
 * <ul>
 *   <li>幂等性检查</li>
 *   <li>状态管理</li>
 *   <li>重试机制</li>
 *   <li>异常处理</li>
 * </ul>
 *
 * @param <T> 事件类型
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventMessageHandler<T extends DomainEvent> implements EventMessageHandler<T> {

    private final EventConsumeMapper eventConsumeMapper;

    /**
     * 处理事件消息（带幂等和状态管理）
     *
     * @param event 领域事件
     */
    @Transactional(rollbackFor = Exception.class)
    public void processWithIdempotency(T event) {
        String eventId = event.getEventId();
        String idempotentKey = getIdempotentKey(event);
        String consumerGroup = getConsumerGroup();
        String consumerName = getConsumerName();

        log.debug("Processing event: eventId={}, consumerGroup={}, consumerName={}",
                eventId, consumerGroup, consumerName);

        try {
            // 1. 检查是否已处理（幂等性检查）
            if (isAlreadyConsumed(eventId, idempotentKey, consumerGroup)) {
                log.info("Event already consumed, skipping: eventId={}", eventId);
                return;
            }

            // 2. 创建或更新消费记录（状态为READY）
            EventConsumeDO consumeDO = getOrCreateConsumeRecord(event, idempotentKey);

            // 3. 更新状态为CONSUMING（可选，如果需要区分处理中和已处理）
            consumeDO.setConsumeStatus(ConsumeStatus.RETRY.name());
            consumeDO.setConsumeTime(Instant.now());
            eventConsumeMapper.update(consumeDO);

            // 4. 执行业务逻辑
            handle(event);

            // 5. 业务逻辑执行成功，更新状态为CONSUMED
            consumeDO.setConsumeStatus(ConsumeStatus.CONSUMED.name());
            consumeDO.setCompleteTime(Instant.now());
            consumeDO.setRetryTimes(0);
            eventConsumeMapper.update(consumeDO);

            log.info("Event consumed successfully: eventId={}, consumerGroup={}",
                    eventId, consumerGroup);

        } catch (Exception e) {
            log.error("Failed to consume event: eventId={}", eventId, e);

            // 业务逻辑执行失败，更新状态为RETRY
            handleConsumeFailure(event, idempotentKey, e);
        }
    }

    /**
     * 检查事件是否已被消费
     */
    private boolean isAlreadyConsumed(String eventId, String idempotentKey, String consumerGroup) {
        Long count = eventConsumeMapper.selectCountByQuery(
                QueryWrapper.create()
                        .where("(event_id = ? OR idempotent_key = ?) AND consumer_group = ? AND consume_status = ?",
                                eventId, idempotentKey, consumerGroup, ConsumeStatus.CONSUMED.name())
        );

        return count > 0;
    }

    /**
     * 获取或创建消费记录
     */
    private EventConsumeDO getOrCreateConsumeRecord(T event, String idempotentKey) {
        // 先查询是否存在
        EventConsumeDO consumeDO = eventConsumeMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("event_id = ? AND consumer_group = ? AND consumer_name = ?",
                                event.getEventId(), getConsumerGroup(), getConsumerName())
        );

        if (consumeDO == null) {
            // 不存在，创建新记录
            consumeDO = createConsumeRecord(event, idempotentKey);
            eventConsumeMapper.insert(consumeDO);
        }

        return consumeDO;
    }

    /**
     * 创建消费记录
     */
    private EventConsumeDO createConsumeRecord(T event, String idempotentKey) {
        EventPublishDO eventPublishDO = findEventPublish(event.getEventId());

        EventConsumeDO consumeDO = new EventConsumeDO();
        consumeDO.setId(generateId());
        consumeDO.setEventId(event.getEventId());
        consumeDO.setIdempotentKey(idempotentKey);
        consumeDO.setConsumerGroup(getConsumerGroup());
        consumeDO.setConsumerName(getConsumerName());
        consumeDO.setConsumeStatus(ConsumeStatus.READY.name());
        consumeDO.setPriority(event.getPriority().name());
        consumeDO.setRetryTimes(0);
        consumeDO.setMaxRetryTimes(event.getMaxRetryTimes());

        if (eventPublishDO != null) {
            consumeDO.setMaxRetryTimes(eventPublishDO.getMaxRetryTimes());
        }

        return consumeDO;
    }

    /**
     * 查找事件发布记录
     */
    private EventPublishDO findEventPublish(String eventId) {
        // 这里需要注入 EventPublishMapper 或 EventStore
        // 暂时返回null，实际实现时需要注入
        return null;
    }

    /**
     * 生成ID
     */
    private Long generateId() {
        return System.currentTimeMillis();
    }

    /**
     * 处理消费失败
     */
    private void handleConsumeFailure(T event, String idempotentKey, Exception e) {
        EventConsumeDO consumeDO = eventConsumeMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("idempotent_key = ? AND consumer_group = ?",
                                idempotentKey, getConsumerGroup())
        );

        if (consumeDO != null) {
            int currentRetryTimes = consumeDO.getRetryTimes() != null ? consumeDO.getRetryTimes() : 0;
            int maxRetryTimes = consumeDO.getMaxRetryTimes() != null ? consumeDO.getMaxRetryTimes() : 3;

            if (currentRetryTimes < maxRetryTimes) {
                // 还可以重试
                consumeDO.setConsumeStatus(ConsumeStatus.RETRY.name());
                consumeDO.setRetryTimes(currentRetryTimes + 1);
                consumeDO.setNextRetryTime(calculateNextRetryTime(currentRetryTimes + 1));
                consumeDO.setErrorMessage(e.getMessage());
                eventConsumeMapper.update(consumeDO);

                log.warn("Event consumption failed, will retry: eventId={}, retryTimes={}/{}",
                        event.getEventId(), currentRetryTimes + 1, maxRetryTimes);
            } else {
                // 重试次数用尽，标记为失败
                consumeDO.setConsumeStatus(ConsumeStatus.FAILED.name());
                consumeDO.setErrorMessage("Max retry times exceeded: " + e.getMessage());
                eventConsumeMapper.update(consumeDO);

                log.error("Event consumption failed after max retries: eventId={}", event.getEventId());

                // 调用失败处理钩子
                onMaxRetriesExceeded(event, e);
            }
        }
    }

    /**
     * 计算下次重试时间
     * <p>使用指数退避策略
     * @param retryTimes 当前重试次数
     * @return 下次重试时间
     */
    private Instant calculateNextRetryTime(int retryTimes) {
        // 指数退避：1分钟, 2分钟, 4分钟, 8分钟, ...
        long delayMinutes = (long) Math.pow(2, retryTimes - 1);
        return Instant.now().plusSeconds(delayMinutes * 60);
    }

    /**
     * 重试次数用尽时的钩子方法
     * <p>子类可以重写此方法实现自定义的失败处理逻辑
     * @param event 领域事件
     * @param e 最后一次失败的异常
     */
    protected void onMaxRetriesExceeded(T event, Exception e) {
        log.error("Max retries exceeded for event: {}, type={}",
                event.getEventId(), event.getEventType());
        // 默认实现：记录日志，子类可以重写发送告警等
    }

}
