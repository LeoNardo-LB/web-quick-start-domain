package org.smm.archetype.test.cases.unittest.domain.platform.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smm.archetype.domain.platform.audit.AuditEvent;
import org.smm.archetype.domain.platform.audit.AuditLogService;
import org.smm.archetype.test.support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 审计日志服务单元测试
 *
 * <p>测试审计事件记录功能，包括：
 * <ul>
 *   <li>用户登录/登出</li>
 *   <li>权限变更</li>
 *   <li>数据删除</li>
 *   <li>配置修改</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("审计日志单元测试")
public class AuditLogServiceUTest extends UnitTestBase {

    @Test
    @DisplayName("should_CreateUserLoginEvent_When_LogUserLogin")
    void should_CreateUserLoginEvent_When_LogUserLogin() {
        // Given
        AuditLogService service = new AuditLogService();

        // When & Then - 测试不抛出异常
        service.logUserLogin("user123", "192.168.1.100", "Chrome/120.0");
    }

    @Test
    @DisplayName("should_CreateUserLogoutEvent_When_LogUserLogout")
    void should_CreateUserLogoutEvent_When_LogUserLogout() {
        // Given
        AuditLogService service = new AuditLogService();

        // When & Then
        service.logUserLogout("user123");
    }

    @Test
    @DisplayName("should_CreatePermissionChangeEvent_When_LogPermissionChange")
    void should_CreatePermissionChangeEvent_When_LogPermissionChange() {
        // Given
        AuditLogService service = new AuditLogService();

        // When & Then
        service.logPermissionChange("user123", "GRANT", "ORDER_MANAGE");
    }

    @Test
    @DisplayName("should_CreateDataDeleteEvent_When_LogDataDelete")
    void should_CreateDataDeleteEvent_When_LogDataDelete() {
        // Given
        AuditLogService service = new AuditLogService();

        // When & Then
        service.logDataDelete("user123", "ORDER:456");
    }

    @Test
    @DisplayName("should_CreateConfigChangeEvent_When_LogConfigChange")
    void should_CreateConfigChangeEvent_When_LogConfigChange() {
        // Given
        AuditLogService service = new AuditLogService();

        // When & Then
        service.logConfigChange("user123", "log.level", "INFO", "DEBUG");
    }

    @Test
    @DisplayName("should_CreateAuditEvent_When_LogEvent")
    void should_CreateAuditEvent_When_LogEvent() {
        // Given
        AuditLogService service = new AuditLogService();
        AuditEvent event = AuditEvent.builder()
                                   .auditType("TEST")
                                   .userId("user123")
                                   .operation("测试操作")
                                   .resource("TEST_RESOURCE")
                                   .result("SUCCESS")
                                   .build();

        // When & Then
        service.log(event);
    }

    @Test
    @DisplayName("should_IgnoreNullEvent_When_LogNullEvent")
    void should_IgnoreNullEvent_When_LogNullEvent() {
        // Given
        AuditLogService service = new AuditLogService();

        // When & Then - 不应抛出异常
        service.log(null);
    }

    @Test
    @DisplayName("should_GenerateCorrectLogMessage_When_AuditEventCreated")
    void should_GenerateCorrectLogMessage_When_AuditEventCreated() {
        // Given
        AuditEvent event = AuditEvent.userLogin("user123", "192.168.1.100", "Chrome");

        // When
        String message = event.toLogMessage();

        // Then
        assertThat(message).contains("[AUDIT]");
        assertThat(message).contains("type=USER_LOGIN");
        assertThat(message).contains("userId=user123");
        assertThat(message).contains("ip=192.168.1.100");
        assertThat(message).contains("device=Chrome");
    }

    @Test
    @DisplayName("should_CreateAllEventTypes_When_LogMultipleEvents")
    void should_CreateAllEventTypes_When_LogMultipleEvents() {
        // Given
        AuditLogService service = new AuditLogService();

        // When
        service.logUserLogin("user123", "192.168.1.100", "Chrome");
        service.logUserLogout("user123");
        service.logPermissionChange("user123", "GRANT", "ORDER_MANAGE");
        service.logDataDelete("user123", "ORDER:456");
        service.logConfigChange("user123", "log.level", "INFO", "DEBUG");
    }

}

