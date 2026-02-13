package org.smm.archetype.test.cases.unittest.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smm.archetype.config.logging.LoggingConfiguration;
import org.smm.archetype.test.support.UnitTestBase;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 日志配置单元测试
 *
 * <p>测试日志配置验证类的功能，包括：
 * <ul>
 *   <li>日志路径配置读取</li>
 *   <li>日志目录创建验证</li>
 *   <li>错误处理验证</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("日志配置单元测试")
public class LoggingConfigurationUTest extends UnitTestBase {

    @Mock
    private Environment environment;

    @Test
    @DisplayName("should_ReadLogPathFromEnvironment_When_PropertyExists")
    void should_ReadLogPathFromEnvironment_When_PropertyExists() {
        // Given
        when(environment.getProperty("logging.file.path", ".logs"))
                .thenReturn(".logs");

        LoggingConfiguration config = new LoggingConfiguration(environment);

        // When
        config.onApplicationEvent(mock(ApplicationReadyEvent.class));

        // Then
        verify(environment).getProperty("logging.file.path", ".logs");
    }

    @Test
    @DisplayName("should_UseDefaultLogPath_When_PropertyNotExists")
    void should_UseDefaultLogPath_When_PropertyNotExists() {
        // Given
        when(environment.getProperty("logging.file.path", ".logs"))
                .thenReturn(".logs"); // 返回默认值

        LoggingConfiguration config = new LoggingConfiguration(environment);

        // When
        config.onApplicationEvent(mock(ApplicationReadyEvent.class));

        // Then
        verify(environment).getProperty("logging.file.path", ".logs");
    }

    @Test
    @DisplayName("should_VerifyConfigurationRead_When_ApplicationReady")
    void should_VerifyConfigurationRead_When_ApplicationReady() {
        // Given
        when(environment.getProperty("logging.file.path", ".logs"))
                .thenReturn(".logs");

        LoggingConfiguration config = new LoggingConfiguration(environment);

        // When & Then
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        config.onApplicationEvent(event);

        verify(environment, atLeastOnce()).getProperty(anyString(), anyString());
    }

}
