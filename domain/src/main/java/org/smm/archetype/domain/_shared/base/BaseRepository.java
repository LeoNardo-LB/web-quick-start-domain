package org.smm.archetype.domain._shared.base;

import java.util.List;
import java.util.Optional;

/**
 * 仓储（Repository）接口
 *
 * <p>仓储特征：
 * <ul>
 *   <li>像集合一样管理聚合根</li>
 *   <li>隐藏数据访问细节</li>
 *   <li>只对聚合根提供仓储</li>
 *   <li>提供领域语言的查询方法</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 定义订单仓储接口
 * public interface OrderRepository extends Repository<Order, Long> {
 *     // 基本操作由父接口提供
 *
 *     // 领域特定的查询方法
 *     List<Order> findByCustomerId(CustomerId customerId);
 *     Optional<Order> findByOrderNumber(OrderNumber orderNumber);
 * }
 * }</pre>
 * @param <T> 聚合根类型
 * @author Leonardo
 * @since 2025/12/30
 */
public interface BaseRepository<T extends AggregateRoot> {

    /**
     * 保存聚合根
     *
     * <p>根据聚合根的状态自动判断是新增还是更新。
     * 同时负责发布聚合根中的领域事件。
     * @param aggregate 聚合根
     * @return 保存后的聚合根
     */
    T save(T aggregate);

    /**
     * 根据ID查找聚合根
     * @param id 聚合根ID
     * @return 聚合根（如果存在）
     */
    Optional<T> findById(Long id);

    /**
     * 根据ID查找聚合根，如果不存在则抛出异常
     * @param id 聚合根ID
     * @return 聚合根
     */
    default T getById(Long id) {
        return findById(id).orElseThrow(() ->
                                                new IllegalArgumentException("Aggregate not found with id: " + id)
        );
    }

    /**
     * 删除聚合根
     * @param id 聚合根ID
     */
    void deleteById(Long id);

    /**
     * 查找所有聚合根
     * @return 所有聚合根列表
     */
    List<T> findAll();

    /**
     * 检查聚合根是否存在
     * @param id 聚合根ID
     * @return 如果存在返回true
     */
    default boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    /**
     * 统计聚合根数量
     * @return 聚合根总数
     */
    long count();

}