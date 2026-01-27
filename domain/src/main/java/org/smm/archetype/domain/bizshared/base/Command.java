package org.smm.archetype.domain.bizshared.base;

/**
 * 命令（Command）标记接口
 *
 * <p>命令特征：
 * <ul>
 *   <li>表示意图</li>
 *   <li>使用动词命名（如CreateOrder、UpdateCustomer）</li>
 *   <li>通常会导致状态改变</li>
 *   <li>返回值通常是void或简单标识</li>
 * </ul>
 *
 * <p>CQRS原则：
 * <ul>
 *   <li>命令与查询分离</li>
 *   <li>命令不应该有返回值（除了标识）</li>
 *   <li>命令应该幂等</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public class CreateOrderCommand implements Command {
 *     private final CustomerId customerId;
 *     private final List<OrderItem> items;
 *     private final String deliveryAddress;
 *
 *     // 构造函数、getter等
 * }
 *
 * public class CancelOrderCommand implements Command {
 *     private final Long orderId;
 *     private final String reason;
 *
 *     // 构造函数、getter等
 * }
 * }</pre>
 * @author Leonardo
 * @since 2025/12/30
 */
public interface Command {
    // 标记接口
}
