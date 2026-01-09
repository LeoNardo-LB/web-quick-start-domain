package org.smm.archetype.app._shared.base;

/**
 * 应用服务基类
 *
 * <p>应用服务特征：
 * <ul>
 *   <li>协调领域对象完成业务用例</li>
 *   <li>处理事务边界</li>
 *   <li>调用领域服务和仓储</li>
 *   <li>不包含业务逻辑</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>薄应用服务，胖领域模型</li>
 *   <li>每个方法对应一个用例</li>
 *   <li>方法名表达业务意图</li>
 *   <li>使用事务管理一致性</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * @Service
 * public class OrderApplicationService extends ApplicationService {
 *
 *     private final OrderRepository orderRepository;
 *     private final CustomerRepository customerRepository;
 *     private final PaymentService paymentService;
 *
 *     @Transactional
 *     public Long createOrder(CreateOrderCommand command) {
 *         // 1. 验证客户存在
 *         Customer customer = customerRepository.getById(command.getCustomerId());
 *
 *         // 2. 创建订单（领域逻辑）
 *         Order order = Order.create(command.getCustomerId(), command.getItems());
 *
 *         // 3. 保存订单
 *         orderRepository.save(order);
 *
 *         // 4. 返回结果
 *         return order.getId();
 *     }
 *
 *     @Transactional
 *     public void cancelOrder(CancelOrderCommand command) {
 *         // 1. 加载订单
 *         Order order = orderRepository.getById(command.getOrderId());
 *
 *         // 2. 取消订单（领域逻辑）
 *         order.cancel(command.getReason());
 *
 *         // 3. 保存变更
 *         orderRepository.save(order);
 *     }
 * }
 * }</pre>
 * @author Leonardo
 * @since 2025/12/30
 */
public abstract class ApplicationService {

    /**
     * 执行写操作（命令）
     *
     * <p>写操作默认需要事务。
     * 子类方法应该使用@Transactional注解。
     * @param action 写操作逻辑
     * @param <T>    返回类型
     * @return 操作结果
     */
    protected <T> T execute(RunnableWithResult<T> action) {
        return action.run();
    }

    /**
     * 执行读操作（查询）
     *
     * <p>读操作不需要事务。
     * 可以使用只读事务优化性能。
     * @param action 查询逻辑
     * @param <T>    返回类型
     * @return 查询结果
     */
    protected <T> T query(RunnableWithResult<T> action) {
        return action.run();
    }

    /**
     * 可返回结果的Runnable
     * @param <T> 返回类型
     */
    @FunctionalInterface
    public interface RunnableWithResult<T> {

        T run();

    }

}
