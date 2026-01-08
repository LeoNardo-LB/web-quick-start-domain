package org.smm.archetype.domain._shared.base;

import java.util.List;
import java.util.Optional;

/**
 * 数据访问器接口
 *
 * <p>用于访问非聚合根的实体数据。
 *
 * <p>与Repository的区别：
 * <ul>
 *   <li>Repository - 专门用于聚合根，维护一致性边界</li>
 *   <li>DataAccessor - 用于独立实体或只读实体</li>
 * </ul>
 *
 * <p>使用场景：
 * <ul>
 *   <li>日志实体（Log）</li>
 *   <li>审计记录</li>
 *   <li>统计信息</li>
 *   <li>配置数据</li>
 *   <li>其他不属于任何聚合的实体</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>不包含业务逻辑</li>
 *   <li>简单的CRUD操作</li>
 *   <li>不发布领域事件</li>
 *   <li>不维护一致性边界</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public interface LogDataAccessor extends DataAccessor<Log> {
 *     List<Log> findByCustomerId(Long customerId);
 *     List<Log> findByTimeRange(Instant start, Instant end);
 * }
 * }</pre>
 * @param <T> 实体类型
 * @author Leonardo
 * @since 2025/12/30
 */
public interface DataAccessor<T extends Entity> {

    /**
     * 保存实体
     * @param entity 实体
     * @return 保存后的实体
     */
    T save(T entity);

    /**
     * 批量保存
     * @param entities 实体列表
     * @return 保存后的实体列表
     */
    default List<T> saveAll(List<T> entities) {
        return entities.stream()
                       .map(this::save)
                       .toList();
    }

    /**
     * 根据ID查找
     * @param id 实体ID
     * @return 实体（如果存在）
     */
    Optional<T> findById(Long id);

    /**
     * 根据ID查找，如果不存在则抛出异常
     * @param id 实体ID
     * @return 实体
     */
    default T getById(Long id) {
        return findById(id).orElseThrow(() ->
                                                new IllegalArgumentException("Entity not found with id: " + id)
        );
    }

    /**
     * 删除实体
     * @param id 实体ID
     */
    void deleteById(Long id);

    /**
     * 查找所有
     * @return 所有实体
     */
    List<T> findAll();

    /**
     * 检查是否存在
     * @param id 实体ID
     * @return 如果存在返回true
     */
    default boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    /**
     * 统计数量
     * @return 实体总数
     */
    long count();

}
