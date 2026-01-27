package org.smm.archetype.domain.bizshared.base;

import java.util.List;
import java.util.Optional;

/**
 * 只读数据访问器接口
 *
 * <p>用于只读实体的数据访问，防止误修改。
 *
 * <p>使用场景：
 * <ul>
 *   <li>审计日志</li>
 *   <li>历史记录</li>
 *   <li>报表数据</li>
 *   <li>其他只读实体</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public interface AuditLogDataAccessor extends ReadOnlyDataAccessor<AuditLog> {
 *     List<AuditLog> findByOperation(String operation);
 *     List<AuditLog> findByTimeRange(Instant start, Instant end);
 * }
 * }</pre>
 * @param <T> 实体类型
 * @author Leonardo
 * @since 2025/12/30
 */
public interface ReadOnlyDataAccessor<T extends Entity> {

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
