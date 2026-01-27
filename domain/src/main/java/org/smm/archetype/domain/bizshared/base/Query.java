package org.smm.archetype.domain.bizshared.base;

/**
 * 查询（Query）标记接口
 *
 * <p>查询特征：
 * <ul>
 *   <li>表示数据检索需求</li>
 *   <li>不会修改状态</li>
 *   <li>可以有复杂的返回值</li>
 *   <li>可以多次调用而不影响系统</li>
 * </ul>
 *
 * <p>CQRS原则：
 * <ul>
 *   <li>查询与命令分离</li>
 *   <li>查询可以返回复杂的DTO</li>
 *   <li>查询性能可以独立优化</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public class GetOrderQuery implements Query {
 *     private final Long orderId;
 *
 *     // 构造函数、getter等
 * }
 *
 * public class SearchOrdersQuery implements Query {
 *     private final CustomerId customerId;
 *     private final OrderStatus status;
 *     private final LocalDate startDate;
 *     private final LocalDate endDate;
 *     private final int pageSize;
 *     private final int pageNumber;
 *
 *     // 构造函数、getter等
 * }
 * }</pre>
 * @author Leonardo
 * @since 2025/12/30
 */
public interface Query {
    // 标记接口
}
