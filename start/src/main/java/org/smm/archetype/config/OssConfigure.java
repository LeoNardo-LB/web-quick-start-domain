package org.smm.archetype.config;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.config.properties.OssProperties;
import org.smm.archetype.domain._shared.client.IdClient;
import org.smm.archetype.domain._shared.client.OssClient;
import org.smm.archetype.domain.common.file.CommonFileRepository;
import org.smm.archetype.infrastructure._shared.client.oss.impl.LocalOssClientImpl;
import org.smm.archetype.infrastructure._shared.client.oss.impl.RustFsOssClientImpl;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.FileBusinessMapper;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.FileMetadataMapper;
import org.smm.archetype.infrastructure.common.file.CommonFileRepositoryImpl;
import org.smm.archetype.infrastructure.common.file.CommonFileServiceImpl;
import org.smm.archetype.infrastructure.common.file.FileBusinessConverter;
import org.smm.archetype.infrastructure.common.file.FileMetaConverter;
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

    private final OssProperties properties;

    public OssConfigure(OssProperties properties) {
        this.properties = properties;
    }

    // ==================== 本地组件 Bean（默认实现） ====================

    /**
     * 本地组件：本地对象存储服务（默认实现，兜底方案）
     *
     * <p>存储路径：用户文件夹/.project/${spring.application.name}/oss
     *
     * <p>使用 NIO FileChannel.transferTo 实现零拷贝，提高性能。
     * @param metadataMapper 文件元数据 Mapper
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
            FileMetadataMapper metadataMapper,
            IdClient idClient) {
        try {
            OssProperties.Local local = properties.getLocal();
            log.info("Initializing Local Object Storage Service: basePath={}, zeroCopy={}",
                    local.getBasePath(), local.isZeroCopy());
            return new LocalOssClientImpl(
                    local.getBasePath(),
                    local.isZeroCopy(),
                    metadataMapper,
                    idClient
            );
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
     * @param metadataMapper 文件元数据 Mapper
     * @param idClient       ID 生成服务
     * @return RustFS 对象存储服务实现
     */
    @Bean
    @Primary
    @ConditionalOnProperty(
            prefix = "middleware.object-storage",
            name = "type",
            havingValue = "rustfs"
    )
    public OssClient rustfsObjectStorageService(
            FileMetadataMapper metadataMapper,
            IdClient idClient) {
        try {
            OssProperties.RustFs rustfs = properties.getRustfs();
            log.info("Initializing RustFS Object Storage Service: endpoint={}, bucket={}",
                    rustfs.getEndpoint(),
                    rustfs.getBucket());

            return new RustFsOssClientImpl(
                    rustfs.getEndpoint(),
                    rustfs.getAccessKey(),
                    rustfs.getSecretKey(),
                    rustfs.getBucket(),
                    metadataMapper,
                    idClient
            );
        } catch (Exception e) {
            log.error("Failed to initialize RustFS Object Storage Service", e);
            throw new RuntimeException("Failed to initialize RustFS Object Storage Service", e);
        }
    }

    // ==================== 文件仓储 Bean ====================

    /**
     * 通用文件仓储
     *
     * <p>负责FileBusiness和FileMeta的持久化操作
     * @param businessMapper        业务文件Mapper
     * @param metadataMapper        文件元数据Mapper
     * @param fileBusinessConverter 业务文件转换器
     * @param fileMetaConverter     文件元数据转换器
     * @return 通用文件仓储实现
     */
    @Bean
    public CommonFileRepository commonFileRepository(
            final FileBusinessMapper businessMapper,
            final FileMetadataMapper metadataMapper,
            final FileBusinessConverter fileBusinessConverter,
            final FileMetaConverter fileMetaConverter) {
        return new CommonFileRepositoryImpl(businessMapper, metadataMapper, fileBusinessConverter, fileMetaConverter);
    }

    /**
     * 通用文件服务
     *
     * <p>整合OssClient、CommonFileRepository，提供完整的文件管理功能
     * @param ossClient            对象存储服务
     * @param commonFileRepository 通用文件仓储
     * @return 通用文件服务实现
     */
    @Bean
    public org.smm.archetype.domain.common.file.CommonFileService commonFileService(
            final OssClient ossClient,
            final CommonFileRepository commonFileRepository) {
        return new CommonFileServiceImpl(ossClient, commonFileRepository);
    }

}
