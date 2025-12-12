// package org.smm.archetype.domain.log.handler.persistence;
//
// import org.smm.archetype.domain.log.Log;
// import org.smm.archetype.domain.log.LogDto;
// import org.smm.archetype.domain.log.handler.stringify.JdkStringifyHandler;
// import org.smm.archetype.domain.log.handler.stringify.StringifyHandler;
// import org.smm.archetype.domain.log.handler.stringify.StringifyType;
// import org.springframework.stereotype.Component;
//
// import java.time.Duration;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import java.util.stream.Collectors;
//
// /**
//  * @author Leonardo
//  * @since 2025/7/15
//  * 数据库持久化
//  */
// @Component
// public class DbPersistenceHandler implements PersistenceHandler {
//
//     private final LogRepository logRepository;
//
//     private final Map<StringifyType, StringifyHandler> stringifyHandlerMap;
//
//     public DbPersistenceHandler(LogRepository logRepository, List<StringifyHandler> stringifyHandlers) {
//         this.stringifyHandlerMap = stringifyHandlers.stream().collect(Collectors.toMap(StringifyHandler::getStringifyType, h -> h));
//         this.logRepository = logRepository;
//     }
//
//     @Override
//     public PersistenceType getPersistenceType() {
//         return PersistenceType.DB;
//     }
//
//     @Override
//     public void persist(LogDto LogDto) {
//         // 选择handler
//         Log log = LogDto.getLog();
//         StringifyType stringify = log.stringify();
//         StringifyHandler handler = Optional.ofNullable(stringifyHandlerMap.get(stringify)).orElse(new JdkStringifyHandler());
//         // 构建日志并保存
//         LogDo logDo = new LogDo();
//         logDo.setBiz(log.value());
//         logDo.setMethod(org.smm.archetype.domain.log.LogDto.getSignature().toLongString());
//         logDo.setArgString(handler.stringify(org.smm.archetype.domain.log.LogDto.getArgs()));
//         logDo.setResultString(handler.stringify(org.smm.archetype.domain.log.LogDto.getResult()));
//         logDo.setThreadName(org.smm.archetype.domain.log.LogDto.getThreadName());
//         Optional.ofNullable(LogDto.getError()).ifPresent(e -> logDo.setException(e.toString()));
//         logDo.setTimeCost(Duration.between(org.smm.archetype.domain.log.LogDto.getStartTime(), org.smm.archetype.domain.log.LogDto
//         .getEndTime()).toMillis());
//         logDo.setStartTime(org.smm.archetype.domain.log.LogDto.getStartTime());
//         logDo.setEndTime(org.smm.archetype.domain.log.LogDto.getEndTime());
//         logRepository.save(logDo);
//     }
//
// }
