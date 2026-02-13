package org.smm.archetype.test.cases.integrationtest.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.test.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 日志配置集成测试
 *
 * <p>测试日志配置在真实 Spring 上下文中的行为，包括：
 * <ul>
 *   <li>日志文件输出到正确位置</li>
 *   <li>日志目录创建验证</li>
 *   <li>日志文件生成验证</li>
 * </ul>
 */
@TestPropertySource(properties = "logging.file.path=.logs")
@DisplayName("日志配置集成测试")
public class LoggingConfigurationITest extends IntegrationTestBase {

    private final Environment environment;

    @Autowired
    LoggingConfigurationITest(Environment environment) {
        this.environment = environment;
    }

    @Test
    @DisplayName("should_ReadLogPathFromConfiguration_When_ApplicationStarts")
    void should_ReadLogPathFromConfiguration_When_ApplicationStarts() {
        // When & Then
        String logPath = environment.getProperty("logging.file.path");
        assertThat(logPath).isNotNull();
        assertThat(logPath).isEqualTo(".logs");
    }

    @Test
    @DisplayName("should_CreateLogDirectory_When_ApplicationStarts")
    void should_CreateLogDirectory_When_ApplicationStarts() {
        // Given
        String logPath = environment.getProperty("logging.file.path", ".logs");
        Path logDir = Path.of(logPath);

        // When & Then
        assertThat(logDir).exists();
        assertThat(Files.isDirectory(logDir)).isTrue();
    }

    @Test
    @DisplayName("should_BeWritable_When_LogDirectoryCreated")
    void should_BeWritable_When_LogDirectoryCreated() {
        // Given
        String logPath = environment.getProperty("logging.file.path", ".logs");
        Path logDir = Path.of(logPath);

        // When & Then
        assertThat(Files.isWritable(logDir)).isTrue();
    }

    @Test
    @DisplayName("should_LogFilesExist_When_ApplicationRuns")
    void should_LogFilesExist_When_ApplicationRuns() {
        // Given
        String logPath = environment.getProperty("logging.file.path", ".logs");
        Path logDir = Path.of(logPath);

        // When & Then - 等待应用启动后检查日志文件
        File appLogFile = logDir.resolve("app.log").toFile();
        File currentLogFile = logDir.resolve("current.log").toFile();
        File errorLogFile = logDir.resolve("error.log").toFile();

        // 至少应该存在一个日志文件（取决于配置）
        assertThat(appLogFile.exists() || currentLogFile.exists() || errorLogFile.exists())
                .isTrue();
    }

}
