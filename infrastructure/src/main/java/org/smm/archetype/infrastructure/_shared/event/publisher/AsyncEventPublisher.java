package org.smm.archetype.infrastructure._shared.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步事件发布器装饰器
 *
 * <p>使用Spring的@Async注解实现异步事件发布，
 * 复用已配置的线程池，提高事件发布性能。
 *
 * <p>工作流程：
 * <ol>
 *   <li>接收到事件列表</li>
 *   <li>使用IO线程池异步调用实际的EventPublisher</li>
 *   <li>返回CompletableFuture，支持链式调用</li>
 *   <li>异常处理和日志记录</li>
 * </ol>
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncEventPublisher implements EventPublisher {

    private final EventPublisher delegate;

    /**
     * 异步发布领域事件
     *
     * <p>使用IO线程池执行事件发布，不阻塞主线程。
     * @param events 领域事件列表
     */
    @Async("ioTaskExecutor")
    @Override
    public void publish(List<DomainEvent> events) {
        log.debug("Async publishing {} events", events.size());

        try {
            delegate.publish(events);
            log.debug("Async events published successfully");
        } catch (Exception e) {
            log.error("Failed to publish events asynchronously", e);
            throw e;
        }
    }

    /**
     * 异步发布单个领域事件
     * @param event 领域事件
     */
    @Async("ioTaskExecutor")
    @Override
    public void publish(DomainEvent event) {
        log.debug("Async publishing event: {}", event.getEventTypeName());

        try {
            delegate.publish(event);
            log.debug("Async event published successfully: {}", event.getEventTypeName());
        } catch (Exception e) {
            log.error("Failed to publish event asynchronously: {}", event.getEventTypeName(), e);
            throw e;
        }
    }

    /**
     * 异步发布并返回Future
     *
     * <p>支持链式调用和结果处理。
     * @param events 领域事件列表
     * @return CompletableFuture
     */
    @Async("ioTaskExecutor")
    public CompletableFuture<Void> publishAsync(List<DomainEvent> events) {
        try {
            delegate.publish(events);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

}
