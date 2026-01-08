package org.smm.archetype.infrastructure._shared.component.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventHandler;
import org.smm.archetype.domain._shared.event.EventPublisher;
import org.smm.archetype.domain._shared.event.EventStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 事件发布器实现
 *
 * <p>使用Spring的事件机制发布领域事件。
 * 支持同步和异步发布模式。
 * @author Leonardo
 * @since 2025/12/30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {

    private final List<EventHandler<DomainEvent>> eventHandlers;

    private final EventStore eventStore;

    @Override
    public void publish(List<DomainEvent> events) {
        log.debug("Publishing {} events", events.size());

        try {
            // 1. 持久化事件（可选）
            eventStore.append(events);

            // 2. 查找并调用所有匹配的处理器
            events.forEach(event -> eventHandlers.forEach(handler -> {
                if (handler.canHandle(event)) {
                    try {
                        handler.handle(event);
                        log.debug("Event processed by: {}", handler.getClass().getSimpleName());
                    } catch (Exception e) {
                        log.error("Error handling event: {} by handler: {}",
                                event.getClass().getSimpleName(), handler.getClass().getSimpleName(), e);
                        // 可以选择继续处理其他处理器，或者抛出异常
                    }
                }
            }));

            log.debug("Events published successfully");
        } catch (Exception e) {
            log.error("Failed to publish events", e);
            throw new RuntimeException("Event publish failed", e);
        }
    }

    /**
     * 异步发布事件（非接口方法）
     */
    public void publishAsync(DomainEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                publish(List.of(event));
            } catch (Exception e) {
                log.error("Async event publish failed: {}", event.getClass().getSimpleName(), e);
            }
        });
    }

}
