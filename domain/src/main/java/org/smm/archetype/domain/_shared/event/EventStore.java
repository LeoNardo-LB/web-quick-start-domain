package org.smm.archetype.domain._shared.event;

import org.smm.archetype.domain._shared.base.DomainEvent;

import java.util.List;

/**
 * 事件存储（Event Store）接口
 *
 * <p>事件存储用于持久化领域事件，实现事件溯源（Event Sourcing）。
 *
 * <p>职责：
 * <ul>
 *   <li>持久化领域事件</li>
 *   <li>按聚合根ID检索事件流</li>
 *   <li>支持事件重放</li>
 *   <li>保证事件的顺序性</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public class OrderRepositoryImpl implements OrderRepository {
 *     private final EventStore eventStore;
 *
 *     @Override
 *     public void save(Order order) {
 *         // 1. 保存领域事件到事件存储
 *         List<DomainEvent> events = order.getUncommittedEvents();
 *         eventStore.append(events);
 *
 *         // 2. 标记事件为已提交
 *         order.markEventsAsCommitted();
 *
 *         // 3. （可选）更新聚合根的快照
 *         snapshotStore.saveSnapshot(order);
 *     }
 *
 *     @Override
 *     public Order findById(Long id) {
 *         // 1. 从事件存储加载事件流
 *         List<DomainEvent> events = eventStore.getEvents(id);
 *
 *         // 2. 重放事件重建聚合根
 *         Order order = new Order();
 *         for (DomainEvent event : events) {
 *             order.apply(event);
 *         }
 *
 *         return order;
 *     }
 * }
 * }</pre>
 * @author Leonardo
 * @since 2025/12/30
 */
public interface EventStore {

    /**
     * 追加领域事件到事件存储
     *
     * <p>此方法应该保证：
     * <ul>
     *   <li>事务性</li>
     *   <li>原子性</li>
     *   <li>顺序性</li>
     * </ul>
     * @param events 领域事件列表
     */
    void append(List<DomainEvent> events);

    /**
     * 追加单个领域事件
     * @param event 领域事件
     */
    default void append(DomainEvent event) {
        append(List.of(event));
    }

    /**
     * 获取聚合根的所有事件
     * @param aggregateId 聚合根ID
     * @return 事件流（按发生时间排序）
     */
    List<DomainEvent> getEvents(String aggregateId);

    /**
     * 获取聚合根从指定版本后的事件
     * @param aggregateId 聚合根ID
     * @param fromVersion 起始版本号
     * @return 事件流（按发生时间排序）
     */
    List<DomainEvent> getEvents(String aggregateId, long fromVersion);

}
