package org.smm.archetype.domain.common.log;

import org.smm.archetype.domain._shared.base.DataAccessor;

import java.time.Instant;
import java.util.List;

/**
 * 日志数据访问器接口
 *
 * <p>注意：Log不是聚合根，而是独立实体，因此使用DataAccessor而不是Repository。
 *
 * <p>Repository vs DataAccessor:
 * <ul>
 *   <li>Repository - 用于聚合根，维护一致性边界，发布领域事件</li>
 *   <li>DataAccessor - 用于独立实体，简单的CRUD操作，不发布事件</li>
 * </ul>
 *
 * <p>使用场景：
 * <ul>
 *   <li>日志记录</li>
 *   <li>审计跟踪</li>
 *   <li>操作记录</li>
 * </ul>
 * @author Leonardo
 * @since 2025/12/30
 */
public interface LogDataAccessor extends DataAccessor<Log> {

    /**
     * 根据客户ID查询日志
     * @param customerId 客户ID
     * @return 日志列表
     */
    List<Log> findByCustomerId(Long customerId);

    /**
     * 根据操作类型查询日志
     * @param operation 操作类型
     * @return 日志列表
     */
    List<Log> findByOperation(String operation);

    /**
     * 根据时间范围查询日志
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志列表
     */
    List<Log> findByTimeRange(Instant startTime, Instant endTime);

    /**
     * 根据是否成功查询日志
     * @param success 是否成功（true=无异常，false=有异常）
     * @return 日志列表
     */
    List<Log> findBySuccess(boolean success);

    /**
     * 根据业务类型查询日志
     * @param businessType 业务类型
     * @return 日志列表
     */
    List<Log> findByBusinessType(String businessType);

    /**
     * 根据线程名称查询日志
     * @param threadName 线程名称
     * @return 日志列表
     */
    List<Log> findByThreadName(String threadName);

}
