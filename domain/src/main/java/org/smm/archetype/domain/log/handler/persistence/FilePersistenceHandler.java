package org.smm.archetype.domain.log.handler.persistence;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smm.archetype.domain.log.Log;
import org.smm.archetype.domain.log.LogDto;
import org.smm.archetype.domain.log.handler.stringify.JdkStringifyHandler;
import org.smm.archetype.domain.log.handler.stringify.StringifyHandler;
import org.smm.archetype.domain.log.handler.stringify.StringifyType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文件持久化处理器
 *
 * 实现将日志信息以文件形式持久化的处理器，通过SLF4J将日志信息输出到日志文件中。
 * 支持多种对象字符串化策略，可根据配置选择合适的字符串化方式。
 */
@Component
public class FilePersistenceHandler implements PersistenceHandler {

    /**
     * 字符串化处理器映射表
     *
     * 存储不同字符串化类型的处理器实例，用于根据配置的字符串化类型执行相应的对象转换操作。
     */
    private final Map<StringifyType, StringifyHandler> stringifyHandlerMap;

    /**
     * JDK默认字符串化处理器
     *
     * 作为默认的字符串化处理器，在未找到匹配的字符串化类型处理器时使用。
     */
    private final JdkStringifyHandler JDKStringifyHandler;

    /**
     * 构造函数
     *
     * 初始化字符串化处理器映射表和默认的JDK字符串化处理器。
     * @param stringifyHandlers   字符串化处理器列表
     * @param JDKStringifyHandler JDK默认字符串化处理器
     */
    public FilePersistenceHandler(List<StringifyHandler> stringifyHandlers, JdkStringifyHandler JDKStringifyHandler) {
        this.stringifyHandlerMap = stringifyHandlers.stream().collect(Collectors.toMap(StringifyHandler::getStringifyType, h -> h));
        this.JDKStringifyHandler = JDKStringifyHandler;
    }

    /**
     * 获取持久化类型
     *
     * 返回文件持久化类型，用于标识此处理器支持的持久化方式。
     * @return 持久化类型枚举值 PersistenceType.FILE
     */
    @Override
    public PersistenceType getPersistenceType() {
        return PersistenceType.FILE;
    }

    /**
     * 执行文件持久化操作
     *
     * 将日志信息格式化后通过SLF4J输出到日志文件中。根据是否有异常信息决定使用info级别还是error级别记录日志。
     * 日志内容包括业务名称、方法签名、执行耗时、线程名称、方法参数、返回值和异常信息等。
     * @param LogDto 日志数据传输对象，包含待持久化的日志信息
     */
    @Override
    public void persist(LogDto LogDto) {
        // 输出实际的类
        Class<?> declaringClass = LogDto.getSignature().getMethod().getDeclaringClass();
        Logger logger = LoggerFactory.getLogger(declaringClass);

        Log log = LogDto.getLog();
        StringifyType stringify = log.stringify();
        MethodSignature signature = LogDto.getSignature();
        Object[] args = LogDto.getArgs();
        Object result = LogDto.getResult();
        Throwable error = LogDto.getError();

        StringifyHandler handler = Optional.ofNullable(stringifyHandlerMap.get(stringify)).orElse(JDKStringifyHandler);
        StringBuilder builder = new StringBuilder();
        // 业务名称
        if (StringUtils.isNotBlank(log.value())) {
            builder.append("Biz:[").append(log.value()).append("]; ");
        }
        // 方法
        builder.append("Method:[").append(signature.toShortString()).append("]; ");
        // 耗时
        builder.append("Cost:[").append(Duration.between(LogDto.getStartTime(), LogDto.getEndTime()).toMillis()).append("ms]; ");
        // 线程名称
        builder.append("Thread:[").append(LogDto.getThreadName()).append("]; ");
        // 入参
        if (args != null && args.length > 0) {
            builder.append("Args:[").append(handler.stringify(args)).append("]; ");
        }
        // 返回值
        if (result != null) {
            builder.append("BaseResult:[").append(handler.stringify(result)).append("]; ");
        }
        if (error == null) {
            logger.info(builder.toString());
        } else {
            builder.append("Exception:[").append(error.getMessage()).append("]; ");
            logger.error(builder.toString());
        }
    }

}
