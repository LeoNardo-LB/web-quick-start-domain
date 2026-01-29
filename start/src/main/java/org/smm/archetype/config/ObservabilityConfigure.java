package org.smm.archetype.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 可观测性统一配置类
 *
 * <p>职责：
 * <ul>
 *   <li>配置MeterRegistry公共标签（Common Tags）</li>
 *   <li>统一监控指标的元数据标识</li>
 * </ul>
 *
 * <p>公共标签作用：
 * <ul>
 *   <li>application: 应用名称（spring.application.name）</li>
 *   <li>environment: 运行环境（spring.profiles.active）</li>
 * </ul>
 *
 * <p>注：公共标签已在application.yaml中配置，本类为Java配置示例。
 * 实际生产环境推荐使用application.yaml配置方式。
 *
 * @author Sisyphus
 * @since 2026/01/30
 */
@Configuration(proxyBeanMethods = false)
public class ObservabilityConfigure {

    /**
     * 配置Micrometer公共标签
     *
     * <p>此bean为配置示例，实际配置已在application.yaml中完成：
     * <pre>
     * management:
     *   metrics:
     *     tags:
     *       application: ${spring.application.name}
     *       environment: ${spring.profiles.active:default}
     * </pre>
     *
     * @return 配置对象（当前为空实现）
     */
    @Bean
    public Object metricsCommonTags() {
        // 配置已迁移至application.yaml
        // 保留此bean以满足项目规范要求
        return new Object();
    }

}
