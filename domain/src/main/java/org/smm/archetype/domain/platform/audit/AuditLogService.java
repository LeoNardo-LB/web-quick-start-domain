package org.smm.archetype.domain.platform.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 审计日志服务
 *
 * <p>负责记录审计事件到 audit.log 文件，满足合规性要求。
 * <p>审计日志服务特点：
 * <ul>
 *   <li>使用独立的 logger（AUDIT_LOGGER）</li>
 *   <li>日志级别为 INFO</li>
 *   <li>日志文件保留 180 天</li>
 *   <li>不可篡改（仅追加）</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {

    /**
     * 审计日志 Logger
     * <p>使用独立的 logger 记录审计事件
     */
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT_LOGGER");

    /**
     * 记录审计事件
     * @param event 审计事件
     */
    public void log(AuditEvent event) {
        if (event == null) {
            log.warn("[AUDIT] 审计事件为空，忽略记录");
            return;
        }

        // 记录到审计日志文件
        AUDIT_LOGGER.info(event.toLogMessage());

        // 同时记录到主日志（便于调试）
        log.info("[AUDIT] {} | {} | {}", event.getAuditType(), event.getUserId(), event.getOperation());
    }

    /**
     * 记录用户登录事件
     * @param userId   用户 ID
     * @param clientIp 客户端 IP
     * @param device   设备信息
     */
    public void logUserLogin(String userId, String clientIp, String device) {
        AuditEvent event = AuditEvent.userLogin(userId, clientIp, device);
        log(event);
    }

    /**
     * 记录用户登出事件
     * @param userId 用户 ID
     */
    public void logUserLogout(String userId) {
        AuditEvent event = AuditEvent.userLogout(userId);
        log(event);
    }

    /**
     * 记录权限变更事件
     * @param userId    用户 ID
     * @param operation 操作类型（GRANT/REVOKE）
     * @param resource  资源
     */
    public void logPermissionChange(String userId, String operation, String resource) {
        AuditEvent event = AuditEvent.permissionChange(userId, operation, resource);
        log(event);
    }

    /**
     * 记录数据删除事件
     * @param userId   用户 ID
     * @param resource 资源
     */
    public void logDataDelete(String userId, String resource) {
        AuditEvent event = AuditEvent.dataDelete(userId, resource);
        log(event);
    }

    /**
     * 记录配置修改事件
     * @param userId     用户 ID
     * @param configItem 配置项
     * @param oldValue   修改前的值
     * @param newValue   修改后的值
     */
    public void logConfigChange(String userId, String configItem, String oldValue, String newValue) {
        AuditEvent event = AuditEvent.configChange(userId, configItem, oldValue, newValue);
        log(event);
    }

}
