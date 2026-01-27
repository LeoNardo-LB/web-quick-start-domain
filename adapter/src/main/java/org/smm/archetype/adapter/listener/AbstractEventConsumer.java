package org.smm.archetype.adapter.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.schedule.handler.EventHandler;
import org.smm.archetype.domain.bizshared.base.DomainEvent;
import org.smm.archetype.domain.bizshared.event.ConsumeStatus;
import org.smm.archetype.adapter.schedule.RetryStrategy;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventConsumeRepository;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventPublishRepository;
import org.smm.archetype.infrastructure.bizshared.dal.generated.entity.EventConsumeDO;
import org.smm.archetype.infrastructure.bizshared.dal.generated.entity.EventPublishDO;

import java.time.Instant;
import java.util.List;

/**
 * 抽象事件消费器
 *
 * <p>定义事件消费的通用流程模板，管理 EventConsumeDO 的生命周期。
 *
 * <p>核心功能：
 * <ul>
 *   <li>幂等性控制</li>
 *   <li>状态流转管理</li>
 *   <li>重试机制（可插拔的RetryStrategy）</li>
 *   <li>异常处理</li>
 * </ul>
 *
 * <p>状态流转：
 * <ul>
 *   <li>READY → CONSUMED：消费成功</li>
 *   <li>READY → RETRY：消费失败，可重试</li>
 *   <li>RETRY → CONSUMED：重试成功</li>
 *   <li>RETRY → FAILED：达到最大重试次数</li>
 * </ul>
 *
 * <p>重试策略：
 * <ul>
 *   <li>默认：指数退避（ExponentialBackoffRetryStrategy）</li>
 *   <li>外部调度：ExternalSchedulerRetryStrategy（支持XXL-JOB、PowerJob等）</li>
 *   <li>自定义：实现RetryStrategy接口</li>
 * </ul>
 *
 * @param <T> 领域事件类型
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventConsumer<T extends DomainEvent> implements EventConsumer<T> {

    protected final EventConsumeRepository          eventConsumeRepository;
    protected final EventPublishRepository eventPublishRepository;
    protected final List<EventHandler<DomainEvent>> eventHandlers;
    protected final RetryStrategy                    retryStrategy;

    /**
     * 获取消费者组名称
     * @return 消费者组名称
     */
    protected abstract String getConsumerGroup();

    /**
     * 获取消费者名称
     * @return 消费者名称
     */
    protected abstract String getConsumerName();

    /**
     * 消费领域事件（模板方法）
     * @param event 领域事件
     */
    public void consume(T event) {
        String eventId = event.getEventId();
        String idempotentKey = generateIdempotentKey(event);
        String consumerGroup = getConsumerGroup();
        String consumerName = getConsumerName();

        log.debug("Consuming event: eventId={}, consumerGroup={}, consumerName={}",
                eventId, consumerGroup, consumerName);

        try {
            // 1. 检查幂等性
            if (checkIdempotent(idempotentKey, consumerGroup)) {
                log.info("Event already consumed or in retry: eventId={}, idempotentKey={}", eventId, idempotentKey);
                return;
            }

            // 2. 创建消费记录（状态为 READY）
            EventConsumeDO consumeDO = createConsumeRecord(event, idempotentKey, consumerGroup, consumerName);

            // 3. 执行业务逻辑
            doConsume(event, consumeDO);

            // 4. 业务逻辑执行成功，更新状态为 CONSUMED
            handleSuccess(consumeDO);

            log.info("Event consumed successfully: eventId={}, consumerGroup={}", eventId, consumerGroup);

        } catch (Exception e) {
            log.error("Failed to consume event: eventId={}", eventId, e);

            // 5. 业务逻辑执行失败，更新状态为 RETRY 或 FAILED
            handleFailure(event, idempotentKey, consumerGroup, consumerName, e);
        }
    }

    /**
     * 执行实际的事件消费逻辑（由子类实现）
     * @param event     领域事件
     * @param consumeDO 消费记录
     * @throws Exception 消费异常
     */
    protected void doConsume(T event, EventConsumeDO consumeDO) throws Exception {
        // 调用对应的 EventHandler 处理事件
        for (EventHandler<DomainEvent> handler : eventHandlers) {
            if (handler.canHandle(event)) {
                log.debug("Delegating to handler: eventId={}, handler={}",
                        event.getEventId(), handler.getClass().getSimpleName());
                handler.handle(event);
                return;
            }
        }

        log.warn("No handler found for event: eventId={}, type={}", event.getEventId(), event.getEventTypeName());
    }

    /**
     * 检查幂等性
     * @param idempotentKey 幂等键
     * @param consumerGroup 消费者组
     * @return true-已消费，false-未消费
     */
    private boolean checkIdempotent(String idempotentKey, String consumerGroup) {
        EventConsumeDO existing = eventConsumeRepository.findByIdempotentKey(idempotentKey);

        if (existing == null) {
            return false;
        }

        // 判断是否已成功消费
        if (ConsumeStatus.CONSUMED.name().equals(existing.getConsumeStatus())) {
            log.debug("Event already consumed: idempotentKey={}", idempotentKey);
            return true;
        }

        // 判断是否正在重试
        if (ConsumeStatus.RETRY.name().equals(existing.getConsumeStatus())) {
            log.warn("Event is in retry status: idempotentKey={}", idempotentKey);
            return true;
        }

        return false;
    }

    /**
     * 创建消费记录
     * @param event         领域事件
     * @param idempotentKey 幂等键
     * @param consumerGroup 消费者组
     * @param consumerName  消费者名称
     * @return EventConsumeDO
     */
    private EventConsumeDO createConsumeRecord(T event, String idempotentKey, String consumerGroup, String consumerName) {
        // 查询事件发布记录获取最大重试次数
        Integer maxRetryTimes = getMaxRetryTimes(event.getEventId());

        EventConsumeDO consumeDO = new EventConsumeDO();
        consumeDO.setId(generateId());
        consumeDO.setEventId(event.getEventId());
        consumeDO.setIdempotentKey(idempotentKey);
        consumeDO.setConsumerGroup(consumerGroup);
        consumeDO.setConsumerName(consumerName);
        consumeDO.setConsumeStatus(ConsumeStatus.READY.name());
        consumeDO.setPriority(event.getPriority().name());
        consumeDO.setRetryTimes(0);
        consumeDO.setMaxRetryTimes(maxRetryTimes != null ? maxRetryTimes : event.getMaxRetryTimes());
        consumeDO.setCreateTime(Instant.now());
        consumeDO.setVersion(0L);

        eventConsumeRepository.insert(consumeDO);

        log.debug("Consume record created: eventId={}, status=READY", event.getEventId());
        return consumeDO;
    }

    /**
     * 处理消费成功
     * @param consumeDO 消费记录
     */
    private void handleSuccess(EventConsumeDO consumeDO) {
        consumeDO.setConsumeStatus(ConsumeStatus.CONSUMED.name());
        consumeDO.setCompleteTime(Instant.now());
        consumeDO.setRetryTimes(0);

        boolean updated = eventConsumeRepository.updateStatusWithVersion(consumeDO);

        if (updated) {
            log.debug("Consume record updated to CONSUMED: eventId={}", consumeDO.getEventId());
        } else {
            log.warn("Failed to update consume record (version conflict): eventId={}", consumeDO.getEventId());
        }
    }

    /**
     * 处理消费失败
     *
     * <p>使用注入的RetryStrategy计算下次重试时间和判断是否应该重试。
     *
     * @param event         领域事件
     * @param idempotentKey 幂等键
     * @param consumerGroup 消费者组
     * @param consumerName  消费者名称
     * @param e             异常
     */
    private void handleFailure(T event, String idempotentKey, String consumerGroup, String consumerName, Exception e) {
        EventConsumeDO consumeDO = eventConsumeRepository.findByEventIdAndConsumer(
                event.getEventId(), consumerGroup, consumerName);

        if (consumeDO == null) {
            log.error("Consume record not found: eventId={}", event.getEventId());
            return;
        }

        int currentRetryTimes = consumeDO.getRetryTimes() != null ? consumeDO.getRetryTimes() : 0;
        int maxRetryTimes = consumeDO.getMaxRetryTimes() != null ? consumeDO.getMaxRetryTimes() : 3;

        // 使用RetryStrategy判断是否应该重试
        if (retryStrategy.shouldRetry(currentRetryTimes, maxRetryTimes)) {
            // 还可以重试，使用RetryStrategy计算下次重试时间
            consumeDO.setConsumeStatus(ConsumeStatus.RETRY.name());
            consumeDO.setRetryTimes(currentRetryTimes + 1);
            consumeDO.setNextRetryTime(retryStrategy.calculateNextRetryTime(currentRetryTimes + 1));
            consumeDO.setErrorMessage(e.getMessage());

            eventConsumeRepository.updateStatusWithVersion(consumeDO);

            log.warn("Event consumption failed, will retry: eventId={}, retryTimes={}/{}",
                    event.getEventId(), currentRetryTimes + 1, maxRetryTimes);
        } else {
            // 重试次数用尽，标记为失败
            consumeDO.setConsumeStatus(ConsumeStatus.FAILED.name());
            consumeDO.setErrorMessage("Max retry times exceeded: " + e.getMessage());

            eventConsumeRepository.updateStatusWithVersion(consumeDO);

            log.error("Event consumption failed after max retries: eventId={}", event.getEventId());

            // 调用失败处理钩子
            onMaxRetriesExceeded(event, consumeDO, e);
        }
    }

    /**
     * 重试次数用尽时的钩子方法
     *
     * <p>子类可以重写此方法实现自定义的失败处理逻辑。
     * @param event     领域事件
     * @param consumeDO 消费记录
     * @param e         最后一次失败的异常
     */
    protected void onMaxRetriesExceeded(T event, EventConsumeDO consumeDO, Exception e) {
        log.error("Max retries exceeded for event: {}, type={}",
                event.getEventId(), event.getEventTypeName());
        // 默认实现：记录日志，子类可以重写发送告警等
    }

    /**
     * 生成幂等键
     *
     * <p>格式：{eventType}_{aggregateId}_{eventId}_{consumerGroup}
     * @param event 领域事件
     * @return 幂等键
     */
    private String generateIdempotentKey(T event) {
        return String.format("%s_%s_%s_%s",
                event.getEventTypeName(),
                event.getAggregateId(),
                event.getEventId(),
                getConsumerGroup());
    }

    /**
     * 生成 ID
     * @return ID
     */
    private Long generateId() {
        return System.currentTimeMillis();
    }

    /**
     * 获取最大重试次数
     * @param eventId 事件 ID
     * @return 最大重试次数，不存在则返回 null
     */
    private Integer getMaxRetryTimes(String eventId) {
        EventPublishDO eventDO = eventPublishRepository.findByEventId(eventId);

        return eventDO != null ? eventDO.getMaxRetryTimes() : null;
    }

}
