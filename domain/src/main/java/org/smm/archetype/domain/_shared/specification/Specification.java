package org.smm.archetype.domain._shared.specification;

/**
 * 规格模式（Specification Pattern）接口
 *
 * <p>规格模式用于封装业务规则和断言，使得规则可以：
 * <ul>
 *   <li>可复用</li>
 *   <li>可组合</li>
 *   <li>可测试</li>
 *   <li>与业务概念对应</li>
 * </ul>
 *
 * <p>使用场景：
 * <ul>
 *   <li>复杂的业务规则验证</li>
 *   <li>查询条件的组合</li>
 *   <li>业务规则的动态组合</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 定义规格
 * public class CustomerCanPlaceOrderSpecification implements Specification<Customer> {
 *     private final OrderRepository orderRepository;
 *
 *     @Override
 *     public boolean isSatisfiedBy(Customer customer) {
 *         // 规则1: 客户必须是激活状态
 *         if (!customer.isActive()) {
 *             return false;
 *         }
 *
 *         // 规则2: 未支付的订单不超过3个
 *         List<Order> unpaidOrders = orderRepository.findUnpaidByCustomerId(customer.getId());
 *         if (unpaidOrders.size() >= 3) {
 *             return false;
 *         }
 *
 *         return true;
 *     }
 * }
 *
 * // 使用规格
 * Specification<Customer> spec = new CustomerCanPlaceOrderSpecification(orderRepository);
 * if (spec.isSatisfiedBy(customer)) {
 *     // 允许下单
 * } else {
 *     throw new BusinessException("Customer cannot place order");
 * }
 *
 * // 组合规格
 * Specification<Customer> activeSpec = new CustomerIsActiveSpecification();
 * Specification<Customer> creditSpec = new CustomerHasGoodCreditSpecification();
 * Specification<Customer> combined = activeSpec.and(creditSpec);
 * }</pre>
 * @param <T> 被规格检查的对象类型
 * @author Leonardo
 * @since 2025/12/30
 */
public interface Specification<T> {

    /**
     * 检查对象是否满足规格
     * @param candidate 被检查的对象
     * @return 如果满足规格返回true
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * 与运算：组合两个规格（AND）
     * @param other 另一个规格
     * @return 组合后的规格
     */
    default Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }

    /**
     * 或运算：组合两个规格（OR）
     * @param other 另一个规格
     * @return 组合后的规格
     */
    default Specification<T> or(Specification<T> other) {
        return new OrSpecification<>(this, other);
    }

    /**
     * 非运算：取反规格
     * @return 取反后的规格
     */
    default Specification<T> not() {
        return new NotSpecification<>(this);
    }

}
