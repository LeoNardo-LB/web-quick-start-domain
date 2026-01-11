package org.smm.archetype.config.condition;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * 阿里云邮件服务启用条件
 *
 * <p>检查阿里云邮件服务配置是否完整有效。
 *
 * <p>条件：
 * <ul>
 *   <li>middleware.email.type 必须是 aliyun</li>
 *   <li>middleware.email.aliyun.access-key-id 必须配置且非空</li>
 *   <li>middleware.email.aliyun.access-key-id 不能是占位符（如 "your-access-key-id"）</li>
 *   <li>middleware.email.aliyun.access-key-secret 必须配置且非空</li>
 *   <li>middleware.email.aliyun.access-key-secret 不能是占位符（如 "your-access-key-secret"）</li>
 * </ul>
 * @author Leonardo
 * @since 2026-01-11
 */
public class AliyunEmailEnabledCondition implements Condition {

    private static final String PLACEHOLDER_PREFIX = "your-";

    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();

        // 检查服务类型
        String type = env.getProperty("middleware.email.type");
        if (!"aliyun".equals(type)) {
            return false;
        }

        String accessKeyId = env.getProperty("middleware.email.aliyun.access-key-id");
        String accessKeySecret = env.getProperty("middleware.email.aliyun.access-key-secret");

        // 检查是否配置且非空
        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            return false;
        }

        // 检查是否是占位符
        return !accessKeyId.startsWith(PLACEHOLDER_PREFIX) && !accessKeySecret.startsWith(PLACEHOLDER_PREFIX);
    }

}
