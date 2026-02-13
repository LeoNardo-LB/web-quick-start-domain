package org.smm.archetype.adapter.schedule;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.event.EventDispatcher;
import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.domain.shared.event.Type;
import org.smm.archetype.infrastructure.shared.event.persistence.EventConsumeRecord;
import org.smm.archetype.infrastructure.shared.event.persistence.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 事件重试调度器实现，定时扫描并重试RETRYING状态的事件。
 */
@Slf4j
public class EventRetrySchedulerImpl implements EventRetryScheduler {

    private final EventRepository eventRepository;
    private final EventDispatcher eventDispatcher;
    private final ExecutorService virtualThreadExecutor;

    /**
     * 每批次处理数量
     */
    private final int batchSize;

    /**
     * 构造器
     *
     * @param eventRepository       事件仓储
     * @param eventDispatcher       事件分发器
     * @param virtualThreadExecutor 虚拟线程池
     * @param batchSize             每批次处理数量
     */
    public EventRetrySchedulerImpl(
            EventRepository eventRepository,
            EventDispatcher eventDispatcher,
            ExecutorService virtualThreadExecutor,
            int batchSize) {
        this.eventRepository = eventRepository;
        this.eventDispatcher = eventDispatcher;
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.batchSize = batchSize;
    }

    /**
     * 定时调度方法。
     */
    @Override
    @Scheduled(cron = "${middleware.domain-event.retry.cron}")
    public void scheduleRetry() {
        log.debug("事件重试调度器已启动");

        try {
            processRetryEvents();
        } catch (Exception e) {
            log.error("事件重试调度器异常", e);
        }

        log.debug("事件重试调度器已完成");
    }

    /**
     * 处理待重试事件
     */
    private void processRetryEvents() {
        // 查询 RETRYING 状态且到达重试时间的事件（返回 EventConsumeRecord）
        List<EventConsumeRecord> records = eventRepository.findRetryConsumeEvents(batchSize);

        if (records.isEmpty()) {
            log.debug("无待重试事件");
            return;
        }

        log.info("发现 {} 个待重试事件", records.size());

        // 使用虚拟线程池并发处理
        List<CompletableFuture<Void>> futures = records.stream()
                                                        .map(record -> CompletableFuture.runAsync(
                                                                () -> replayEvent(record), virtualThreadExecutor))
                                                        .toList();

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("已完成 {} 个事件的重试", records.size());
    }

    /**
     * 重放事件
     *
     * @param record 消费记录
     */
    private void replayEvent(EventConsumeRecord record) {
        String eventId = record.getEid();
        Type eventType = record.getType();

        try {
            log.debug("正在重放事件: eventId={}, type={}", eventId, eventType);

            // 使用 Type 枚举反序列化事件载荷（只是 payload，不是整个 Event）
            Object payload = eventType.deserialize(record.getPayload());

            // 使用 record 中的元信息 + payload 重建 Event
            // Event 已改为值对象，不包含 status 字段，状态由 EventConsumeRecord 维护
            Event<?> event = Event.builder()
                                     .setEid(record.getEid())
                                     .setType(record.getType())
                                     .setMaxRetryTimes(record.getMaxRetryTimes())
                                     .setPayload(payload)
                                     .build();

            // 委托给 EventDispatcher 处理（重试模式）
            eventDispatcher.dispatch(event, true);

            log.debug("事件已重放: eventId={}", eventId);

        } catch (Exception e) {
            log.error("重放事件异常: eventId={}, type={}", eventId, eventType, e);
            // 异常已在 EventDispatcher 中处理，这里仅记录日志
        }
    }

}
