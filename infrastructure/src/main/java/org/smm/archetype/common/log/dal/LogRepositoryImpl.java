package org.smm.archetype.common.log.dal;

import org.smm.archetype.common.log.dal.entity.LogDO;
import org.smm.archetype.common.log.dal.mapper.LogMapper;
import org.smm.archetype.common.log.Log;
import org.smm.archetype.common.log.Log.LogBuilder;
import org.smm.archetype.common.log.LogAnno;
import org.smm.archetype.common.log.handler.stringify.StringifyHandler;
import org.smm.archetype.common.log.handler.stringify.StringifyType;
import org.smm.archetype.shared.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 日志仓储实现
 *
 * 负责日志的持久化操作和对象转换，使用 LogConverter 完成领域对象到 DO 的映射
 * @author Leonardo
 * @since 2025/12/29
 */
@Repository
public class LogRepositoryImpl implements BaseRepository<Log> {

    private final LogMapper logMapper;

    private final Map<StringifyType, StringifyHandler> stringifyHandlerMap;

    public LogRepositoryImpl(LogMapper logMapper, List<StringifyHandler> stringifyHandlers) {
        this.logMapper = logMapper;
        this.stringifyHandlerMap = stringifyHandlers.stream().collect(Collectors.toMap(StringifyHandler::getStringifyType, h -> h));
    }

    public void insert(Log log) {
        LogBuilder<?, ?> builder = Log.builder();// 选择 handler
        LogAnno logAnno = log.getLogAnno();
        // 构建日志并保存
        LogDO logDO = new LogDO();
        logDO.setBusinessType(logAnno.value());
        logDO.setMethod(log.getSignature().toLongString());
        logDO.setThreadName(log.getThreadName());
        StringifyHandler handler = stringifyHandlerMap.get(logAnno.stringify());
        logDO.setArgString(handler.stringify(log.getArgs()));
        logDO.setResultString(handler.stringify(log.getResult()));
        Optional.ofNullable(log.getError()).ifPresent(e -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logDO.setException(sw.toString());
        });
        logDO.setStartTime(log.getStartTime());
        logDO.setEndTime(log.getEndTime());
        // 执行保存
        logMapper.insert(logDO);
    }

}
