package org.smm.archetype.config;

import org.smm.archetype.domain.platform.audit.AuditLogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 审计日志配置类
 *
 * <p>负责注册审计日志服务的 Spring Bean。
 * <p>⚠️ 配置类命名规范：必须遵循 `{Aggregate}Configure` 格式
 */
@Configuration
public class AuditLoggingConfigure {

    /**
     * 审计日志服务
     *
     * <p>提供审计事件记录功能，包括：
     * <ul>
     *   <li>用户登录/登出</li>
     *   <li>权限变更</li>
     *   <li>数据删除</li>
     *   <li>配置修改</li>
     * </ul>
     * @return AuditLogService 实例
     */
    @Bean
    public AuditLogService auditLogService() {
        return new AuditLogService();
    }

}
