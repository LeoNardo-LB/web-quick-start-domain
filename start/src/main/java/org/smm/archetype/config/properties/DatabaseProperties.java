package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据库配置属性
 *
 * <p>配置MyBatis日志等数据库相关参数。
 * @author Leonardo
 * @since 2026/1/10
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "middleware.database")
public class DatabaseProperties {

    /**
     * MyBatis日志实现类
     *
     * <p>可选值：
     * <ul>
     *   <li>StdOutImpl：控制台输出（开发环境）</li>
     *   <li>Slf4jImpl：SLF4J输出（生产环境推荐）</li>
     *   <li>NoLoggingImpl：不输出日志</li>
     *   <li>Jdk14LoggingImpl：JDK1.4日志</li>
     *   <li>Log4j2Impl：Log4j2日志</li>
     * </ul>
     *
     * <p>默认：StdOutImpl
     */
    private String logImpl = "StdOutImpl";

    /**
     * 获取日志实现类全限定名
     * @return 类名
     */
    public String getLogImplClassName() {
        return switch (logImpl) {
            case "StdOutImpl" -> "org.apache.ibatis.logging.stdout.StdOutImpl";
            case "Slf4jImpl" -> "org.apache.ibatis.logging.slf4j.Slf4jImpl";
            case "NoLoggingImpl" -> "org.apache.ibatis.logging.nologging.NoLoggingImpl";
            case "Jdk14LoggingImpl" -> "org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl";
            case "Log4j2Impl" -> "org.apache.ibatis.logging.log4j2.Log4j2Impl";
            default -> "org.apache.ibatis.logging.stdout.StdOutImpl";
        };
    }

}
