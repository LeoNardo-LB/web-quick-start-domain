package org.smm.archetype.test.cases.integrationtest.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.test.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 环境特定配置集成测试
 *
 * <p>测试不同环境（dev、production、default/test）的日志配置是否正确应用
 */
@DisplayName("环境特定配置集成测试")
public class EnvironmentLoggingITest extends IntegrationTestBase {

    private final Environment environment;

    @Autowired
    EnvironmentLoggingITest(Environment environment) {
        this.environment = environment;
    }

    // @ParameterizedTest 注释，因为 Spring 上下文在测试类级别加载，
    // 在方法级别修改 profile 会导致多次加载失败
    @Test
    @DisplayName("should_UseCorrectLogLevel_When_ApplicationStarts")
    void should_UseCorrectLogLevel_When_ApplicationStarts() {
        // When & Then - integration profile 应该使用 INFO 级别
        String logLevel = environment.getProperty("logging.level.root", "INFO");
        assertThat(logLevel).isNotNull();
        assertThat(logLevel).isIn("INFO", "DEBUG");
    }

    @Test
    @DisplayName("should_VerifyProfileConfiguration_When_ApplicationStarts")
    void should_VerifyProfileConfiguration_When_ApplicationStarts() {
        // When & Then
        String logPath = environment.getProperty("logging.file.path");

        assertThat(logPath).isNotNull();
        // logPath 应该包含 .logs 目录（可能包含绝对路径前缀）
        assertThat(logPath).contains(".logs");
    }

    @Test
    @DisplayName("should_VerifyDomainLogLevel_When_ApplicationStarts")
    void should_VerifyDomainLogLevel_When_ApplicationStarts() {
        // When & Then
        String domainLogLevel = environment.getProperty("logging.level.org.smm.archetype.domain");

        assertThat(domainLogLevel).isNotNull();
        assertThat(domainLogLevel).isIn("DEBUG", "WARN", "INFO");
    }

    @Test
    @DisplayName("should_VerifyAppLogLevel_When_ApplicationStarts")
    void should_VerifyAppLogLevel_When_ApplicationStarts() {
        // When & Then
        String appLogLevel = environment.getProperty("logging.level.org.smm.archetype.app");

        assertThat(appLogLevel).isNotNull();
        assertThat(appLogLevel).isIn("DEBUG", "WARN", "INFO");
    }

    @Test
    @DisplayName("should_VerifyInfrastructureLogLevel_When_ApplicationStarts")
    void should_VerifyInfrastructureLogLevel_When_ApplicationStarts() {
        // When & Then
        String infraLogLevel = environment.getProperty("logging.level.org.smm.archetype.infrastructure");

        assertThat(infraLogLevel).isNotNull();
        assertThat(infraLogLevel).isIn("INFO", "DEBUG");
    }

    @Test
    @DisplayName("should_VerifyAdapterLogLevel_When_ApplicationStarts")
    void should_VerifyAdapterLogLevel_When_ApplicationStarts() {
        // When & Then
        String adapterLogLevel = environment.getProperty("logging.level.org.smm.archetype.adapter");

        assertThat(adapterLogLevel).isNotNull();
        assertThat(adapterLogLevel).isIn("INFO", "DEBUG");
    }

    @Test
    @DisplayName("should_VerifyThirdPartyLogLevels_When_ApplicationStarts")
    void should_VerifyThirdPartyLogLevels_When_ApplicationStarts() {
        // When & Then
        String springLogLevel = environment.getProperty("logging.level.org.springframework");
        String mybatisLogLevel = environment.getProperty("logging.level.org.mybatis");
        String mysqlLogLevel = environment.getProperty("logging.level.com.mysql.cj");
        String kafkaLogLevel = environment.getProperty("logging.level.org.apache.kafka");

        assertThat(springLogLevel).isNotNull();
        assertThat(mybatisLogLevel).isNotNull();
        assertThat(mysqlLogLevel).isNotNull();
        assertThat(kafkaLogLevel).isNotNull();

        // 验证第三方框架日志级别配置正确
        assertThat(springLogLevel).isIn("INFO", "WARN", "ERROR");
        assertThat(mysqlLogLevel).isEqualTo("WARN");
        assertThat(kafkaLogLevel).isEqualTo("WARN");
    }

}
