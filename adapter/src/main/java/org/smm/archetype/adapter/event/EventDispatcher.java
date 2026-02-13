package org.smm.archetype.adapter.event;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.schedule.RetryStrategy;
import org.smm.archetype.domain.shared.event.Action;
import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.domain.shared.event.Status;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.EventDO;
import org.smm.archetype.infrastructure.shared.event.EventRecordConverter;
import org.smm.archetype.infrastructure.shared.event.persistence.EventConsumeRecord;
import org.smm.archetype.infrastructure.shared.event.persistence.EventRepository;

import java.time.Instant;
import java.util.List;

/**
 * 事件分发器，统一控制事件消费的完整生命周期。
 */
@Slf4j
public class EventDispatcher {

    private static final String ACTION_CONSUME          = Action.CONSUME.name();
    private static final long   DELETE_TIME_NOT_DELETED = 0L;

    private final EventRepository       eventRepository;
    private final List<EventHandler<?>> eventHandlers;
    private final RetryStrategy         retryStrategy;
    private final List<FailureHandler>  failureHandlers;
    private final EventRecordConverter  recordConverter;

    private final String executorGroup;
    private final int    defaultMaxRetryTimes;

    /**
     * 构造器
     * @param eventRepository      事件仓储
     * @param eventHandlers        事件处理器列表
     * @param retryStrategy        重试策略
     * @param failureHandlers      失败处理器列表
     * @param recordConverter      记录转换器
     * @param executorGroup        执行者组（消费者组）
     * @param defaultMaxRetryTimes 默认最大重试次数
     */
    public EventDispatcher(
            EventRepository eventRepository,
            List<EventHandler<?>> eventHandlers,
            RetryStrategy retryStrategy,
            List<FailureHandler> failureHandlers,
            EventRecordConverter recordConverter,
            String executorGroup,
            int defaultMaxRetryTimes) {
        this.eventRepository = eventRepository;
        this.eventHandlers = eventHandlers;
        this.retryStrategy = retryStrategy;
        this.failureHandlers = failureHandlers;
        this.recordConverter = recordConverter;
        this.executorGroup = executorGroup;
        this.defaultMaxRetryTimes = defaultMaxRetryTimes;
    }

/**
     * 分发事件。
     * @param event 领域事件
     * @param isRetry 是否为重试
     */
    public void dispatch(Event<?> event, boolean isRetry) {
        String eventId = event.getEid();
        String executor = getExecutor();

        log.debug("Dispatching event: eventId={}, executorGroup={}, isRetry={}",
                eventId, executorGroup, isRetry);

        EventDO eventDO = null;

        try {
            if (isRetry) {
                // 重试场景：获取已有记录
                eventDO = getExistingRecord(eventId);
                if (eventDO == null) {
                    log.error("Retry record not found: eventId={}", eventId);
                    return;
                }
            } else {
                // 首次消费场景：幂等检查 + 创建记录
                if (checkIdempotent(eventId)) {
                    log.info("Event already consumed or in retry: eventId={}", eventId);
                    return;
                }
                eventDO = createConsumeRecord(event);
            }

            // 执行业务逻辑
            doDispatch(event);

            // 处理成功
            handleSuccess(eventDO);

            log.info("Event dispatched successfully: eventId={}, executorGroup={}",
                    eventId, executorGroup);

        } catch (Exception e) {
            log.error("Failed to dispatch event: eventId={}", eventId, e);

            // 处理失败
            if (eventDO != null) {
                handleFailure(event, eventDO, e);
            }
        }
    }

    /**
     * 检查幂等性
     *
    基于 eid + action + executor_group + delete_time 进行幂等检查。
     * @param eventId 事件ID
     * @return true-已处理（幂等），false-未处理
     */
    private boolean checkIdempotent(String eventId) {
        EventDO existing = eventRepository.findByIdempotentKey(
                eventId, ACTION_CONSUME, executorGroup, DELETE_TIME_NOT_DELETED);

        if (existing == null) {
            return false;
        }

        String status = existing.getStatus();

        // 已成功消费
        if (Status.SUCCESS.name().equals(status)) {
            log.debug("Event already consumed: eventId={}", eventId);
            return true;
        }

        // 正在重试中
        if (Status.RETRYING.name().equals(status)) {
            log.debug("Event is in retry status: eventId={}", eventId);
            return true;
        }

        // FAILED 状态允许重新处理（人工介入后可能需要重试）
        return false;
    }

    /**
     * 获取已有的消费记录（重试场景）
     * @param eventId 事件ID
     * @return 消费记录
     */
    private EventDO getExistingRecord(String eventId) {
        return eventRepository.findByIdempotentKey(
                eventId, ACTION_CONSUME, executorGroup, DELETE_TIME_NOT_DELETED);
    }

    /**
     * 创建消费记录
     * @param event 领域事件
     * @return 创建的消费记录
     */
    private EventDO createConsumeRecord(Event<?> event) {
        Integer maxRetryTimes = event.getMaxRetryTimes();
        if (maxRetryTimes == null) {
            maxRetryTimes = defaultMaxRetryTimes;
        }

        EventDO eventDO = new EventDO();
        eventDO.setEid(event.getEid());
        eventDO.setAction(ACTION_CONSUME);
        eventDO.setType(event.getType() != null ? event.getType().name() : null);
        eventDO.setStatus(Status.RETRYING.name());
        eventDO.setExecutorGroup(executorGroup);
        eventDO.setExecutor(getExecutor());
        eventDO.setRetryTimes(0);
        eventDO.setMaxRetryTimes(maxRetryTimes);
        eventDO.setDeleteTime(DELETE_TIME_NOT_DELETED);

        eventRepository.insert(eventDO);

        log.debug("Consume record created: eventId={}, status=RETRYING", event.getEid());
        return eventDO;
    }

    /**
     * 执行事件分发
     *
    遍历所有 EventHandler，找到能处理的 Handler 并调用。
     * @param event 领域事件
     * @throws Exception 处理异常
     */
    @SuppressWarnings("unchecked")
    private void doDispatch(Event<?> event) throws Exception {
        Event<Object> rawEvent = (Event<Object>) event;

        for (EventHandler<?> handler : eventHandlers) {
            Event<?> typedEvent = handler.canHandle(rawEvent);
            if (typedEvent != null) {
                log.debug("Delegating to handler: eventId={}, handler={}",
                        event.getEid(), handler.getClass().getSimpleName());

                // 调用 handle 方法，传入 payload
                Object payload = typedEvent.getPayload();
                ((EventHandler<Object>) handler).handle(payload);
                return;
            }
        }

        log.warn("No handler found for event: eventId={}, type={}", event.getEid(), event.getType());
    }

    /**
     * 处理成功
     * @param eventDO 消费记录
     */
    private void handleSuccess(EventDO eventDO) {
        eventDO.setStatus(Status.SUCCESS.name());
        eventDO.setMessage(null);
        eventDO.setNextRetryTime(null);

        boolean updated = eventRepository.updateStatusWithVersion(eventDO);

        if (updated) {
            log.debug("Consume record updated to SUCCESS: eventId={}", eventDO.getEid());
        } else {
            log.warn("Failed to update consume record (version conflict): eventId={}", eventDO.getEid());
        }
    }

    /**
     * 处理失败
     *
    根据当前重试次数判断是继续重试还是标记为最终失败。
     * @param event   领域事件
     * @param eventDO 消费记录
     * @param e       异常
     */
    private void handleFailure(Event<?> event, EventDO eventDO, Exception e) {
        int currentRetryTimes = eventDO.getRetryTimes() != null ? eventDO.getRetryTimes() : 0;
        int maxRetryTimes = eventDO.getMaxRetryTimes() != null
                                    ? eventDO.getMaxRetryTimes()
                                    : defaultMaxRetryTimes;

        // 使用 RetryStrategy 判断是否应该重试
        if (retryStrategy.shouldRetry(currentRetryTimes, maxRetryTimes)) {
            // 还可以重试
            int nextRetryTimes = currentRetryTimes + 1;
            Instant nextRetryTime = retryStrategy.calculateNextRetryTime(nextRetryTimes);

            eventDO.setStatus(Status.RETRYING.name());
            eventDO.setRetryTimes(nextRetryTimes);
            eventDO.setNextRetryTime(nextRetryTime);
            eventDO.setMessage(truncateMessage(e.getMessage()));

            eventRepository.updateStatusWithVersion(eventDO);

            log.warn("Event dispatch failed, will retry: eventId={}, retryTimes={}/{}",
                    event.getEid(), nextRetryTimes, maxRetryTimes);
        } else {
            // 重试次数用尽，标记为失败
            eventDO.setStatus(Status.FAILED.name());
            eventDO.setMessage("Max retry times exceeded: " + truncateMessage(e.getMessage()));
            eventDO.setNextRetryTime(null);

            eventRepository.updateStatusWithVersion(eventDO);

            log.error("Event dispatch failed after max retries: eventId={}", event.getEid());

            // 调用失败处理器
            handleMaxRetriesExceeded(event, eventDO, e);
        }
    }

    /**
     * 处理最终失败（达到最大重试次数）
     * @param event   事件
     * @param eventDO 消费记录
     * @param e       异常
     */
    private void handleMaxRetriesExceeded(Event<?> event, EventDO eventDO, Exception e) {
        // 转换为 Domain 层值对象
        EventConsumeRecord consumeRecord = recordConverter.from(eventDO);

        // 调用失败处理器
        for (FailureHandler handler : failureHandlers) {
            try {
                if (handler.supports(event.getType())) {
                    handler.handleFailure(event, consumeRecord, e);
                    log.info("Failure handler invoked: eventId={}, handler={}",
                            event.getEid(), handler.getClass().getSimpleName());
                    return;
                }
            } catch (Exception ex) {
                log.error("Failure handler error: eventId={}, handler={}",
                        event.getEid(), handler.getClass().getSimpleName(), ex);
            }
        }

        log.warn("No failure handler found for event type: eventId={}, type={}",
                event.getEid(), event.getType());
    }

    /**
     * 获取执行者标识
     * @return 执行者标识（当前使用主机名+线程名）
     */
    private String getExecutor() {
        try {
            String hostName = java.net.InetAddress.getLocalHost().getHostName();
            return hostName + "-" + Thread.currentThread().getName();
        } catch (Exception e) {
            return "unknown-" + Thread.currentThread().getName();
        }
    }

    /**
     * 截断消息（避免过长）
     * @param message 原始消息
     * @return 截断后的消息
     */
    private String truncateMessage(String message) {
        if (message == null) {
            return null;
        }
        final int maxLength = 500;
        return message.length() > maxLength
                       ? message.substring(0, maxLength) + "..."
                       : message;
    }

}
