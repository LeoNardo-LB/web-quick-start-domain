package org.smm.archetype.config;

import org.smm.archetype.domain.common.log.LogDataAccessor;
import org.smm.archetype.domain.common.log.handler.persistence.PersistenceHandler;
import org.smm.archetype.domain.common.log.handler.stringify.StringifyHandler;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.LogMapper;
import org.smm.archetype.infrastructure.common.log.DbPersistenceHandler;
import org.smm.archetype.infrastructure.common.log.FilePersistenceHandler;
import org.smm.archetype.infrastructure.common.log.JdkStringifyHandler;
import org.smm.archetype.infrastructure.common.log.JsonStringifyHandler;
import org.smm.archetype.infrastructure.common.log.LogAspect;
import org.smm.archetype.infrastructure.common.log.LogDataAccessorImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Infrastructure层日志相关配置
 *
 * <p>负责创建日志切面、持久化、序列化等相关的Bean。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class LogConfigure {

    /**
     * JSON序列化处理器
     *
     * <p>将日志对象序列化为JSON字符串。
     * @return JSON序列化处理器
     */
    @Bean
    public JsonStringifyHandler jsonStringifyHandler() {
        return new JsonStringifyHandler();
    }

    /**
     * JDK序列化处理器
     *
     * <p>使用JDK默认方式序列化日志对象。
     * @return JDK序列化处理器
     */
    @Bean
    public JdkStringifyHandler jdkStringifyHandler() {
        return new JdkStringifyHandler();
    }

    /**
     * 数据库持久化处理器
     *
     * <p>将日志保存到数据库。
     * @param logDataAccessor 日志数据访问器
     * @return 数据库持久化处理器
     */
    @Bean
    public DbPersistenceHandler dbPersistenceHandler(final LogDataAccessor logDataAccessor) {
        return new DbPersistenceHandler(logDataAccessor);
    }

    /**
     * 文件持久化处理器
     *
     * <p>将日志写入文件。
     * @param stringifyHandlers   所有字符串化处理器
     * @param jdkStringifyHandler JDK字符串化处理器（作为默认）
     * @return 文件持久化处理器
     */
    @Bean
    public FilePersistenceHandler filePersistenceHandler(List<StringifyHandler> stringifyHandlers,
                                                         JdkStringifyHandler jdkStringifyHandler) {
        return new FilePersistenceHandler(stringifyHandlers, jdkStringifyHandler);
    }

    /**
     * 日志数据访问器实现
     *
     * <p>提供日志的查询、删除等功能。
     * @param logMapper         日志Mapper
     * @param stringifyHandlers 所有字符串化处理器
     * @return 日志数据访问器实现
     */
    @Bean
    public LogDataAccessor logDataAccessor(LogMapper logMapper, List<StringifyHandler> stringifyHandlers) {
        return new LogDataAccessorImpl(logMapper, stringifyHandlers);
    }

    /**
     * 日志切面
     *
     * <p>拦截需要记录日志的方法，自动收集和持久化日志信息。
     * @param persistenceHandlers 所有持久化处理器
     * @param stringifyHandlers   所有字符串化处理器
     * @return 日志切面
     */
    @Bean
    public LogAspect logAspect(List<PersistenceHandler> persistenceHandlers, List<StringifyHandler> stringifyHandlers) {
        return new LogAspect(persistenceHandlers, stringifyHandlers);
    }

}
