package org.smm.archetype.infrastructure.common.file.config;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.IdClient;
import org.smm.archetype.domain._shared.client.OssClient;
import org.smm.archetype.infrastructure._shared.client.oss.impl.LocalOssClientImpl;
import org.smm.archetype.infrastructure._shared.client.oss.impl.RustFsOssClientImpl;
import org.smm.archetype.infrastructure._shared.generated.mapper.FileBusinessMapper;
import org.smm.archetype.infrastructure._shared.generated.mapper.FileMetadataMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 对象存储服务配置类
 *
 * <p>遵循项目的中间件接入规范：
 * <ul>
 *   <li>本地组件作为默认实现（兜底方案）</li>
 *   <li>外部中间件通过 @ConditionalOnProperty + @Primary 自动覆盖本地组件</li>
 *   <li>所有中间件在应用启动时通过 Bean 装配确定，无运行时切换</li>
 * </ul>
 *
 * <h3>Bean 装配策略</h3>
 * <pre>
 * 1. LocalOssClientImpl（本地文件系统）
 *    - 总是被创建（默认实现）
 *    - 只有在配置 type=local 时才会作为主 Bean 使用
 *
 * 2. RustFsOssClientImpl（RustFS 对象存储）
 *    - 当配置 middleware.object-storage.type=rustfs 时才创建
 *    - 使用 @Primary 标记为优先 Bean，自动覆盖本地存储
 *    - 基于 AWS S3 SDK v2 实现（RustFS 100% 兼容 S3 协议）
 * </pre>
 * @author Leonardo
 * @since 2026/1/10
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssConfigure {

    // ==================== 本地组件 Bean（默认实现） ====================

    /**
     * 本地组件：本地对象存储服务（默认实现，兜底方案）
     *
     * <p>使用本地文件系统存储文件，总是会被创建。
     *
     * <p>存储路径：用户文件夹/.project/${spring.application.name}/oss
     *
     * <p>使用 NIO FileChannel.transferTo 实现零拷贝，提高性能。
     * @param properties     对象存储配置
     * @param metadataMapper 文件元数据 Mapper
     * @param businessMapper 文件业务 Mapper
     * @param idClient       ID 生成服务
     * @return 本地对象存储服务实现
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.object-storage",
            name = "type",
            havingValue = "local",
            matchIfMissing = true
    )
    public OssClient localObjectStorageService(
            OssProperties properties,
            FileMetadataMapper metadataMapper,
            FileBusinessMapper businessMapper,
            IdClient idClient) {
        try {
            log.info("Initializing Local Object Storage Service: basePath={}",
                    properties.getLocal().getBasePath());
            return new LocalOssClientImpl(properties, metadataMapper, businessMapper, idClient);
        } catch (Exception e) {
            log.error("Failed to initialize Local Object Storage Service", e);
            throw new RuntimeException("Failed to initialize Local Object Storage Service", e);
        }
    }

    // ==================== 外部中间件 Bean（RustFS） ====================

    /**
     * 外部中间件：RustFS 对象存储服务
     *
     * <p>基于 AWS S3 SDK v2 实现，RustFS 100% 兼容 S3 协议。
     *
     * <p>条件：当 middleware.object-storage.type=rustfs 时才创建此 Bean。
     *
     * <p>使用 @Primary 标记为优先 Bean，自动覆盖本地存储。
     *
     * <p>关键配置：
     * <ul>
     *   <li>endpoint: http://localhost:9000（RustFS 服务地址）</li>
     *   <li>accessKey: leonardo123（默认值）</li>
     *   <li>secretKey: leonardo123（默认值）</li>
     *   <li>bucket: default（默认 bucket）</li>
     *   <li>forcePathStyle: true（RustFS 必须启用 Path-Style 访问）</li>
     * </ul>
     * @param properties     对象存储配置
     * @param metadataMapper 文件元数据 Mapper
     * @param businessMapper 文件业务 Mapper
     * @param idClient       ID 生成服务
     * @return RustFS 对象存储服务实现
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.object-storage",
            name = "type",
            havingValue = "rustfs"
    )
    @Primary
    public OssClient rustfsObjectStorageService(
            OssProperties properties,
            FileMetadataMapper metadataMapper,
            FileBusinessMapper businessMapper,
            IdClient idClient) {
        try {
            log.info("Initializing RustFS Object Storage Service: endpoint={}, bucket={}",
                    properties.getRustfs().getEndpoint(),
                    properties.getRustfs().getBucket());

            return new RustFsOssClientImpl(properties, metadataMapper, businessMapper, idClient);
        } catch (Exception e) {
            log.error("Failed to initialize RustFS Object Storage Service", e);
            throw new RuntimeException("Failed to initialize RustFS Object Storage Service", e);
        }
    }

}
