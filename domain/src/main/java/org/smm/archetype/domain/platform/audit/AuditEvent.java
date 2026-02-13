package org.smm.archetype.domain.platform.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

/**
 * 审计事件值对象
 *
 * <p>记录审计事件，用于满足合规性要求（GDPR、等保）。
 * <p>审计事件包括：
 * <ul>
 *   <li>用户登录/登出</li>
 *   <li>权限变更</li>
 *   <li>敏感数据删除</li>
 *   <li>配置修改</li>
 * </ul>
 */
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class AuditEvent {

    /**
     * 审计类型（操作类型）
     */
    String auditType;

    /**
     * 用户 ID
     */
    String userId;

    /**
     * 操作描述
     */
    String operation;

    /**
     * 操作的资源（如订单ID、配置项）
     */
    String resource;

    /**
     * 操作结果（SUCCESS/FAILURE）
     */
    String result;

    /**
     * 审计时间戳
     */
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 客户端 IP 地址
     */
    String clientIp;

    /**
     * 设备信息（如浏览器、操作系统）
     */
    String device;

    /**
     * 额外上下文信息（JSON 格式）
     */
    String context;

    /**
     * 创建用户登录审计事件
     * @param userId   用户 ID
     * @param clientIp 客户端 IP
     * @param device   设备信息
     * @return 审计事件
     */
    public static AuditEvent userLogin(String userId, String clientIp, String device) {
        return AuditEvent.builder()
                       .auditType("USER_LOGIN")
                       .userId(userId)
                       .operation("用户登录")
                       .resource("SESSION")
                       .result("SUCCESS")
                       .clientIp(clientIp)
                       .device(device)
                       .build();
    }

    /**
     * 创建用户登出审计事件
     * @param userId 用户 ID
     * @return 审计事件
     */
    public static AuditEvent userLogout(String userId) {
        return AuditEvent.builder()
                       .auditType("USER_LOGOUT")
                       .userId(userId)
                       .operation("用户登出")
                       .resource("SESSION")
                       .result("SUCCESS")
                       .build();
    }

    /**
     * 创建权限变更审计事件
     * @param userId    用户 ID
     * @param operation 操作类型（GRANT/REVOKE）
     * @param resource  资源
     * @return 审计事件
     */
    public static AuditEvent permissionChange(String userId, String operation, String resource) {
        return AuditEvent.builder()
                       .auditType("PERMISSION_CHANGE")
                       .userId(userId)
                       .operation("权限变更")
                       .resource(resource)
                       .result(operation)
                       .build();
    }

    /**
     * 创建数据删除审计事件
     * @param userId   用户 ID
     * @param resource 资源类型和 ID
     * @return 审计事件
     */
    public static AuditEvent dataDelete(String userId, String resource) {
        return AuditEvent.builder()
                       .auditType("DATA_DELETE")
                       .userId(userId)
                       .operation("数据删除")
                       .resource(resource)
                       .result("SUCCESS")
                       .build();
    }

    /**
     * 创建配置修改审计事件
     * @param userId     用户 ID
     * @param configItem 配置项
     * @param oldValue   修改前的值
     * @param newValue   修改后的值
     * @return 审计事件
     */
    public static AuditEvent configChange(String userId, String configItem, String oldValue, String newValue) {
        String context = String.format("{\"item\":\"%s\",\"oldValue\":\"%s\",\"newValue\":\"%s\"}",
                configItem, oldValue, newValue);
        return AuditEvent.builder()
                       .auditType("CONFIG_CHANGE")
                       .userId(userId)
                       .operation("配置修改")
                       .resource(configItem)
                       .result("SUCCESS")
                       .context(context)
                       .build();
    }

    /**
     * 获取审计日志消息
     * @return 日志消息
     */
    public String toLogMessage() {
        return String.format("[AUDIT] type=%s | userId=%s | operation=%s | resource=%s | result=%s | timestamp=%s | ip=%s | device=%s",
                auditType, userId, operation, resource, result, timestamp, clientIp, device);
    }

}
