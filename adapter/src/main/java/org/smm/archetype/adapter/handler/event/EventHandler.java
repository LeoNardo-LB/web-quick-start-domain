package org.smm.archetype.adapter.handler.event;

import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventType;

/**
 * 事件处理器（Event Handler）接口
 *
 * <p>事件处理器用于处理领域事件，实现业务逻辑。
 *
 * <p>设计原则：
 * <ul>
 *   <li>每个处理器处理一种事件类型</li>
 *   <li>处理器应该是幂等的</li>
 *   <li>处理器应该处理异常</li>
 *   <li>处理器可以是异步的</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 处理订单创建事件
 * @Component
 * public class OrderCreatedEventHandler implements EventHandler<OrderCreatedEvent> {
 *     private final NotificationService notificationService;
 *
 *     @Override
 *     public void handle(OrderCreatedEvent event) {
 *         // 发送通知
 *         notificationService.sendOrderCreatedNotification(event.getOrderId());
 *     }
 *
 *     @Override
 *     public boolean canHandle(DomainEvent event) {
 *         return event instanceof OrderCreatedEvent;
 *     }
 *
 *     @Override
 *     public EventType getEventType() {
 *         return EventType.ORDER_CREATED;
 *     }
 * }
 * }</pre>
 * @param <T> 事件类型
 * @author Leonardo
 * @since 2025/12/30
 */
public interface EventHandler<T extends DomainEvent> {

    /**
     * 处理领域事件
     * @param event 领域事件
     */
    void handle(T event);

    /**
     * 判断是否可以处理该事件
     * @param event 领域事件
     * @return 如果可以处理返回true
     */
    boolean canHandle(DomainEvent event);

    /**
     * 获取支持的事件类型
     * @return 事件类型枚举
     */
    EventType getEventType();

    /**
     * 获取处理器优先级
     * @return 优先级数值，默认0
     */
    default int getPriority() {
        return 0;
    }

}
