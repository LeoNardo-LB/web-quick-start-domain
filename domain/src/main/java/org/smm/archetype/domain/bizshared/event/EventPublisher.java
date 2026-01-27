package org.smm.archetype.domain.bizshared.event;

import org.smm.archetype.domain.bizshared.base.DomainEvent;

import java.util.List;

/**
 * 事件发布器（Event Publisher）接口
 *
 * <p>事件发布器用于将领域事件发布到订阅者，实现最终一致性。
 *
 * <p>职责：
 * <ul>
 *   <li>发布领域事件到消息总线</li>
 *   <li>处理事件订阅和分发</li>
 *   <li>保证事件的可靠传递</li>
 *   <li>支持异步发布</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public class OrderRepositoryImpl implements OrderRepository {
 *     private final EventStore eventStore;
 *     private final EventPublisher eventPublisher;
 *
 *     @Transactional
 *     public void save(Order order) {
 *         // 1. 保存事件到事件存储
 *         List<DomainEvent> events = order.getUncommittedEvents();
 *         eventStore.append(events);
 *
 *         // 2. 发布事件到消息总线
 *         // 注意：这里可以使用事务同步器，确保事务提交后再发布
 *         eventPublisher.publish(events);
 *
 *         // 3. 标记事件为已提交
 *         order.markEventsAsCommitted();
 *     }
 * }
 * }</pre>
 * @author Leonardo
 * @since 2025/12/30
 */
public interface EventPublisher {

    /**
     * 发布领域事件
     *
     * <p>发布策略：
     * <ul>
     *   <li>同步发布：立即发布，在事务提交前</li>
     *   <li>异步发布：使用消息队列，在事务提交后</li>
     *   <li>保证至少一次传递</li>
     * </ul>
     * @param events 领域事件列表
     */
    void publish(List<DomainEvent> events);

    /**
     * 发布单个领域事件
     * @param event 领域事件
     */
    default void publish(DomainEvent event) {
        publish(List.of(event));
    }

}
