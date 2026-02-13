package org.smm.archetype.domain.shared.base;

import java.util.List;
import java.util.Optional;

/**
 * 数据访问器接口，用于非聚合根实体的持久化。
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
