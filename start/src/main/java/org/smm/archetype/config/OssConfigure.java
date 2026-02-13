package org.smm.archetype.config;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.config.properties.OssProperties;
import org.smm.archetype.domain.shared.client.OssClient;
import org.smm.archetype.domain.platform.file.FileDomainService;
import org.smm.archetype.domain.platform.file.FileRepository;
import org.smm.archetype.infrastructure.shared.client.oss.LocalOssClientImpl;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.FileBusinessMapper;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.FileMetadataMapper;
import org.smm.archetype.infrastructure.platform.file.FileBusinessConverter;
import org.smm.archetype.infrastructure.platform.file.FileDomainServiceImpl;
import org.smm.archetype.infrastructure.platform.file.FileMetaConverter;
import org.smm.archetype.infrastructure.platform.file.FileRepositoryImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对象存储服务配置类，支持本地存储和RustFS对象存储。
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
     * 存储路径：用户文件夹/.project/${spring.application.name}/oss
     * 使用 NIO FileChannel.transferTo 实现零拷贝，提高性能。
     * 条件：当不存在 RustFsOssClientImpl Bean 时才创建
     * @param metadataMapper 文件元数据 Mapper
     * @return 本地对象存储服务实现
     */
    @Bean
    public OssClient localObjectStorageService(FileMetadataMapper metadataMapper) {
        try {
            OssProperties.Local local = properties.getLocal();
            log.info("Initializing Local Object Storage Service: basePath={}, zeroCopy={}",
                    local.getBasePath(), local.isZeroCopy());
            return new LocalOssClientImpl(
                    local.getBasePath(),
                    local.isZeroCopy(),
                    metadataMapper
            );
        } catch (Exception e) {
            log.error("Failed to initialize Local Object Storage Service", e);
            throw new RuntimeException("Failed to initialize Local Object Storage Service", e);
        }
    }

    // ==================== 外部中间件 Bean（RustFS） ====================
    // /**
    //  * @param metadataMapper 文件元数据 Mapper
    //  * @return RustFS 对象存储服务实现
    //  */
    // @Bean
    // @Primary
    // @ConditionalOnBooleanProperty("middleware.object-storage.rustfs")
    // public OssClient rustfsObjectStorageService(FileMetadataMapper metadataMapper) {
    //     try {
    //         OssProperties.RustFs rustfs = properties.getRustfs();
    //         log.info("Initializing RustFS Object Storage Service: endpoint={}, bucket={}",
    //                 rustfs.getEndpoint(),
    //                 rustfs.getBucket());
    //
    //         return new RustFsOssClientImpl(
    //                 rustfs.getEndpoint(),
    //                 rustfs.getAccessKey(),
    //                 rustfs.getSecretKey(),
    //                 rustfs.getBucket(),
    //                 metadataMapper
    //         );
    //     } catch (Exception e) {
    //         log.error("Failed to initialize RustFS Object Storage Service", e);
    //         throw new RuntimeException("Failed to initialize RustFS Object Storage Service", e);
    //     }
    // }

    // ==================== 文件仓储 Bean ====================

    /**
     * 通用文件仓储
     * 负责FileBusiness和FileMeta的持久化操作
     * @param businessMapper        业务文件Mapper
     * @param metadataMapper        文件元数据Mapper
     * @param fileBusinessConverter 业务文件转换器
     * @param fileMetaConverter     文件元数据转换器
     * @return 通用文件仓储实现
     */
    @Bean
    public FileRepository commonFileRepository(
            final FileBusinessMapper businessMapper,
            final FileMetadataMapper metadataMapper,
            final FileBusinessConverter fileBusinessConverter,
            final FileMetaConverter fileMetaConverter) {
        return new FileRepositoryImpl(businessMapper, metadataMapper, fileBusinessConverter, fileMetaConverter);
    }

    /**
     * 通用文件服务
     * 整合OssClient、FileRepository，提供完整的文件管理功能
     * @param ossClient      对象存储服务
     * @param fileRepository 通用文件仓储
     * @return 通用文件服务实现
     */
    @Bean
    public FileDomainService commonFileService(
            final OssClient ossClient,
            final FileRepository fileRepository) {
        return new FileDomainServiceImpl(ossClient, fileRepository);
    }

}
