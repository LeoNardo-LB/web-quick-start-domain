package org.smm.archetype.adapter.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.event.EventDispatcher;
import org.smm.archetype.domain.bizshared.event.Event;
import org.smm.archetype.domain.bizshared.event.Source;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring 事件监听器
 *
 * <p>监听 Spring 事件总线中的事件，并委托给 EventDispatcher 处理。
 *
 * <p>职责：
 * <ul>
 *   <li>作为 Spring 事件的入口</li>
 *   <li>将事件委托给 EventDispatcher 进行统一处理</li>
 * </ul>
 *
 * <p>事件的完整生命周期（幂等检查、状态流转、重试等）由 EventDispatcher 统一控制。
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@RequiredArgsConstructor
public class SpringDomainEventListener {

    private final EventDispatcher eventDispatcher;

    /**
     * 处理 Spring 事件
     *
     * <p>使用 @Async 异步处理，避免阻塞事件发布者。
     * @param event 事件
     */
    @EventListener
    @Async("virtualTaskExecutor")
    public void onEvent(Event<?> event) {
        log.debug("已接收 Spring 事件: eventId={}, type={}",
                event.getEid(), event.getClass().getSimpleName());

        // 忽略非 DOMAIN 源的事件
        if (event.getType().getSource() != Source.DOMAIN) {
            log.debug("Event source is not DOMAIN, ignored");
            return;
        }

        // 委托给 EventDispatcher 处理（首次消费，isRetry=false）
        eventDispatcher.dispatch(event, false);
    }

}
