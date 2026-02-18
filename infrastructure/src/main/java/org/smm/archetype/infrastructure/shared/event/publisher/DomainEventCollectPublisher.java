package org.smm.archetype.infrastructure.shared.event.publisher;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.event.Action;
import org.smm.archetype.domain.shared.event.DomainEventPublisher;
import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.domain.shared.event.Source;
import org.smm.archetype.domain.shared.event.Status;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.EventDO;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.EventMapper;
import org.smm.archetype.infrastructure.shared.util.context.ScopedThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;

/**
 * 抽象事件发布器
 *
提供事件发布的通用流程模板，处理事件持久化和状态管理。
 *
发布流程：
 * <ol>
 *   <li>持久化事件到数据库（状态为 CREATED）</li>
 *   <li>调用 doPublish 方法进行实际发布</li>
 *   <li>根据发布结果更新状态（PUBLISHED 或保持 CREATED）</li>
 * </ol>


 */
@Slf4j
public abstract class DomainEventCollectPublisher implements DomainEventPublisher {

    /**
     * 标记当前线程是否已注册事务同步，避免重复注册
     */
    private static final ThreadLocal<Boolean> SYNC_REGISTERED = ThreadLocal.withInitial(() -> Boolean.FALSE);
    protected final EventMapper eventMapper;

    @Value("${spring.application.name}")
    private String appName;

    protected DomainEventCollectPublisher(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    @Override
    public final void publish(@NonNull Event<?> event) {
        List<Event<?>> events = ScopedThreadContext.getDomainEvents();
        events.add(event);
        log.debug("已添加事件: {}", event);

        // 注册事务同步，在事务提交后发布事件（仅首次注册）
        if (TransactionSynchronizationManager.isSynchronizationActive() && !SYNC_REGISTERED.get()) {
            SYNC_REGISTERED.set(Boolean.TRUE);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        publishEvents();
                    } finally {
                        SYNC_REGISTERED.remove();
                    }
                }

                @Override
                public void afterCompletion(int status) {
                    // 事务完成后（无论成功或失败）清理标志
                    SYNC_REGISTERED.remove();
                }
            });
        } else if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 如果没有活跃的事务同步，直接发布
            publishEvents();
        }
    }

    /**
     * 批量发布收集的事件
     */
    private void publishEvents() {
        List<Event<?>> events = ScopedThreadContext.getDomainEvents();
        if (events.isEmpty()) {
            return;
        }

        log.info("正在发布 {} 个事件", events.size());

        events.forEach(event -> {
            try {
                // 1. 持久化事件到数据库
                saveEvent(event);

                // 2. 调用子类实现的发布逻辑
                doPublish(event);

                // 3. 更新状态为SUCCESS
                updateEventStatus(event.getEid(), Status.SUCCESS);

                log.debug("事件已发布: eventId={}", event.getEid());
            } catch (Exception e) {
                log.error("发布事件失败: eventId={}", event.getEid(), e);
                // 如果发布失败，则更新状态为 RETRYING
                updateEventStatus(event.getEid(), Status.RETRYING);
            }
        });

        // 清空已发布的事件
        events.clear();
    }

    /**
     * 保存事件到数据库
     * @param event 事件
     */
    protected void saveEvent(Event<?> event) {
        EventDO eventDO = new EventDO();
        eventDO.setEid(event.getEid());
        eventDO.setAction(Action.PUBLISH.name());
        eventDO.setSource(Source.DOMAIN.name());
        eventDO.setType(event.getType().name());
        eventDO.setStatus(Status.CREATED.name());
        eventDO.setPayload(event.getType().serialize(event.getPayload()));
        eventDO.setExecutor(System.getProperty("user.name"));
        eventDO.setExecutorGroup(appName);
        eventDO.setMessage("创建消息");
        eventDO.setMaxRetryTimes(event.getMaxRetryTimes());
        eventDO.setNextRetryTime(Instant.now());

        log.debug("事件已保存: eventId={}", event.getEid());
    }

    /**
     * 更新事件状态
     * @param eventId 事件ID
     * @param status  新状态
     */
    protected void updateEventStatus(String eventId, Status status) {
        try {
            EventDO eventDO = new EventDO();
            eventDO.setStatus(status.name());
            eventMapper.update(eventDO, Wrappers.<EventDO>lambdaUpdate().eq(EventDO::getEid, eventId));
            log.debug("事件状态已更新: eventId={}, status={}", eventId, status);
        } catch (Exception e) {
            log.error("更新事件状态失败: eventId={}, status={}", eventId, status, e);
        }
    }

    /**
     * 执行实际的事件发布逻辑
     *
    由子类实现具体的发布方式（Kafka、Spring事件等）。
     * @param event 事件
     * @throws Exception 发布异常
     */
    protected abstract void doPublish(Event<?> event) throws Exception;

}
