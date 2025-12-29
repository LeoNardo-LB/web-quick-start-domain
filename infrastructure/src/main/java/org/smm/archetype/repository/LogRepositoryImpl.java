package org.smm.archetype.repository;

import org.smm.archetype.domain.log.Log;
import org.smm.archetype.domain.log.LogAnno;
import org.smm.archetype.domain.log.LogRepository;
import org.smm.archetype.domain.log.handler.stringify.JdkStringifyHandler;
import org.smm.archetype.domain.log.handler.stringify.StringifyHandler;
import org.smm.archetype.domain.log.handler.stringify.StringifyType;
import org.smm.archetype.repository.entity.LogDO;
import org.smm.archetype.repository.mapper.LogMapper;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/29
 */
@Component
public class LogRepositoryImpl implements LogRepository {

    private final LogMapper logMapper;

    private final Map<StringifyType, StringifyHandler> stringifyHandlerMap;

    public LogRepositoryImpl(LogMapper logMapper, List<StringifyHandler> stringifyHandlers) {
        this.logMapper = logMapper;
        this.stringifyHandlerMap = stringifyHandlers.stream().collect(Collectors.toMap(StringifyHandler::getStringifyType, h -> h));
    }

    @Override
    public void insert(Log log) {
        // 选择 handler
        LogAnno logAnno = log.getLogAnno();
        StringifyType stringify = logAnno.stringify();
        StringifyHandler handler = Optional.ofNullable(stringifyHandlerMap.get(stringify)).orElse(new JdkStringifyHandler());

        // 构建日志并保存
        LogDO logDO = new LogDO();
        // logDO.setCreateTime(Instant.now());
        // logDO.setUpdateTime(Instant.now());
        // logDO.setCreateUser("1");
        // logDO.setUpdateUser("2");

        logDO.setBiz(logAnno.value());
        logDO.setMethod(log.getSignature().toLongString());
        logDO.setArgString(handler.stringify(log.getArgs()));
        logDO.setResultString(handler.stringify(log.getResult()));
        logDO.setThreadName(log.getThreadName());
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
