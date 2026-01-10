package org.smm.archetype.infrastructure.common.log;

import com.mybatisflex.core.query.QueryWrapper;
import org.smm.archetype.domain.common.log.Log;
import org.smm.archetype.domain.common.log.LogAnno;
import org.smm.archetype.domain.common.log.LogDataAccessor;
import org.smm.archetype.domain.common.log.handler.stringify.StringifyHandler;
import org.smm.archetype.domain.common.log.handler.stringify.StringifyType;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.LogDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.LogMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.smm.archetype.infrastructure._shared.generated.repository.entity.table.LogDOTableDef.LOG_DO;

/**
 * 日志数据访问器实现
 *
 * <p>注意：Log不是聚合根，而是独立实体，因此使用DataAccessor而不是Repository。
 * @author Leonardo
 * @since 2025/12/30
 */
public class LogDataAccessorImpl implements LogDataAccessor {

    private final LogMapper logMapper;

    private final Map<StringifyType, StringifyHandler> stringifyHandlerMap;

    /**
     * 构造函数，注入依赖
     */
    public LogDataAccessorImpl(LogMapper logMapper, List<StringifyHandler> stringifyHandlers) {
        this.logMapper = logMapper;
        this.stringifyHandlerMap = stringifyHandlers.stream()
                                           .collect(Collectors.toMap(StringifyHandler::getStringifyType, h -> h));
    }

    @Override
    public Log save(Log log) {
        // 转换为DO并保存
        LogDO logDO = toDataObject(log);
        logMapper.insert(logDO);

        // 使用builder重建Log对象并设置生成的ID
        return Log.builder()
                       .setId(logDO.getId())
                       .setLogAnno(log.getLogAnno())
                       .setSignature(log.getSignature())
                       .setArgs(log.getArgs())
                       .setResult(log.getResult())
                       .setThreadName(log.getThreadName())
                       .setError(log.getError())
                       .setStartTime(log.getStartTime())
                       .setEndTime(log.getEndTime())
                       .setCreateTime(logDO.getCreateTime())
                       .setUpdateTime(logDO.getUpdateTime())
                       .setCreateUser(logDO.getCreateUser())
                       .setUpdateUser(logDO.getUpdateUser())
                       .build();
    }

    @Override
    public Optional<Log> findById(Long id) {
        LogDO logDO = logMapper.selectOneById(id);
        return Optional.ofNullable(logDO).map(this::toDomainObject);
    }

    @Override
    public void deleteById(Long id) {
        logMapper.deleteById(id);
    }

    @Override
    public List<Log> findAll() {
        List<LogDO> logDOList = logMapper.selectAll();
        return logDOList.stream()
                       .map(this::toDomainObject)
                       .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return logMapper.selectCountByQuery(QueryWrapper.create());
    }

    @Override
    public List<Log> findByCustomerId(Long customerId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .where(LOG_DO.BUSINESS_TYPE.eq(customerId.toString()))
                                            .orderBy(LOG_DO.CREATE_TIME.desc());

        List<LogDO> logDOList = logMapper.selectListByQuery(queryWrapper);
        return logDOList.stream()
                       .map(this::toDomainObject)
                       .collect(Collectors.toList());
    }

    @Override
    public List<Log> findByOperation(String operation) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .where(LOG_DO.METHOD.like("%" + operation + "%"))
                                            .orderBy(LOG_DO.CREATE_TIME.desc());

        List<LogDO> logDOList = logMapper.selectListByQuery(queryWrapper);
        return logDOList.stream()
                       .map(this::toDomainObject)
                       .collect(Collectors.toList());
    }

    @Override
    public List<Log> findByTimeRange(java.time.Instant startTime, java.time.Instant endTime) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .where(LOG_DO.START_TIME.ge(startTime).and(LOG_DO.START_TIME.le(endTime)))
                                            .orderBy(LOG_DO.CREATE_TIME.desc());

        List<LogDO> logDOList = logMapper.selectListByQuery(queryWrapper);
        return logDOList.stream()
                       .map(this::toDomainObject)
                       .collect(Collectors.toList());
    }

    @Override
    public List<Log> findBySuccess(boolean success) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .where(success ? LOG_DO.EXCEPTION.isNull() : LOG_DO.EXCEPTION.isNotNull())
                                            .orderBy(LOG_DO.CREATE_TIME.desc());

        List<LogDO> logDOList = logMapper.selectListByQuery(queryWrapper);
        return logDOList.stream()
                       .map(this::toDomainObject)
                       .collect(Collectors.toList());
    }

    @Override
    public List<Log> findByBusinessType(String businessType) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .where(LOG_DO.BUSINESS_TYPE.eq(businessType))
                                            .orderBy(LOG_DO.CREATE_TIME.desc());

        List<LogDO> logDOList = logMapper.selectListByQuery(queryWrapper);
        return logDOList.stream()
                       .map(this::toDomainObject)
                       .collect(Collectors.toList());
    }

    @Override
    public List<Log> findByThreadName(String threadName) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .where(LOG_DO.THREAD_NAME.eq(threadName))
                                            .orderBy(LOG_DO.CREATE_TIME.desc());

        List<LogDO> logDOList = logMapper.selectListByQuery(queryWrapper);
        return logDOList.stream()
                       .map(this::toDomainObject)
                       .collect(Collectors.toList());
    }

    /**
     * 领域对象转数据对象
     */
    private LogDO toDataObject(Log log) {
        LogDO logDO = new LogDO();

        // 基础字段
        logDO.setId(log.getId());
        logDO.setCreateTime(log.getCreateTime());
        logDO.setUpdateTime(log.getUpdateTime());
        logDO.setCreateUser(log.getCreateUser());
        logDO.setUpdateUser(log.getUpdateUser());

        // 业务字段
        LogAnno logAnno = log.getLogAnno();
        if (logAnno != null) {
            logDO.setBusinessType(logAnno.value());
        }

        if (log.getSignature() != null) {
            logDO.setMethod(log.getSignature().toLongString());
        }

        logDO.setThreadName(log.getThreadName());

        // 序列化参数和结果
        StringifyHandler handler = null;
        if (logAnno != null) {
            handler = stringifyHandlerMap.get(logAnno.stringify());
        }
        if (handler == null) {
            // 使用默认的JSON序列化
            handler = stringifyHandlerMap.values().stream()
                              .findFirst()
                              .orElseThrow(() -> new IllegalStateException("No StringifyHandler available"));
        }

        logDO.setArgString(handler.stringify(log.getArgs()));
        logDO.setResultString(handler.stringify(log.getResult()));

        // 异常信息
        Optional.ofNullable(log.getError()).ifPresent(e -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logDO.setException(sw.toString());
        });

        logDO.setStartTime(log.getStartTime());
        logDO.setEndTime(log.getEndTime());

        return logDO;
    }

    /**
     * 数据对象转领域对象
     */
    private Log toDomainObject(LogDO logDO) {
        return Log.builder()
                       .setId(logDO.getId())
                       .setCreateTime(logDO.getCreateTime())
                       .setUpdateTime(logDO.getUpdateTime())
                       .setCreateUser(logDO.getCreateUser())
                       .setUpdateUser(logDO.getUpdateUser())
                       .setThreadName(logDO.getThreadName())
                       .setStartTime(logDO.getStartTime())
                       .setEndTime(logDO.getEndTime())
                       .build();
    }

}
