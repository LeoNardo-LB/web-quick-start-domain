package org.smm.archetype.domain.shared.base;

import java.util.List;
import java.util.Optional;

/**
 * 仓储接口，提供聚合根的持久化操作。
 */
public interface BaseRepository<T extends AggregateRoot> {

/**
     * 保存聚合根。
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
        return findById(id).orElseThrow(() -> new IllegalArgumentException("Aggregate not found with id: " + id));
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