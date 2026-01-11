package org.smm.archetype.config.condition;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * 阿里云服务启用条件
 *
 * <p>检查阿里云服务配置是否完整有效。
 *
 * <p>条件：
 * <ul>
 *   <li>access-key-id 必须配置且非空</li>
 *   <li>access-key-id 不能是占位符（如 "your-access-key-id"）</li>
 *   <li>access-key-secret 必须配置且非空</li>
 *   <li>access-key-secret 不能是占位符（如 "your-access-key-secret"）</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/11
 */
public class AliyunEnabledCondition implements Condition {

    private static final String PLACEHOLDER_PREFIX = "your-";

    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();

        String accessKeyId = env.getProperty("aliyun.access-key-id");
        String accessKeySecret = env.getProperty("aliyun.access-key-secret");

        // 检查是否配置且非空
        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            return false;
        }

        // 检查是否是占位符
        return !accessKeyId.startsWith(PLACEHOLDER_PREFIX) && !accessKeySecret.startsWith(PLACEHOLDER_PREFIX);
    }

}
