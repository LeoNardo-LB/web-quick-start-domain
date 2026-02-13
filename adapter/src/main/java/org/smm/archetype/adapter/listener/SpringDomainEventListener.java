package org.smm.archetype.adapter.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.event.EventDispatcher;
import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.domain.shared.event.Source;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring事件监听器，监听Spring事件总线并委托给EventDispatcher处理。
 */
@Slf4j
@RequiredArgsConstructor
public class SpringDomainEventListener {

    private final EventDispatcher eventDispatcher;

    /**
     * 处理Spring事件。
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
