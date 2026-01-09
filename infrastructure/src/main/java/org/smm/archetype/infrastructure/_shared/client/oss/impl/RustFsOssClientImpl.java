package org.smm.archetype.infrastructure._shared.client.oss.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.IdClient;
import org.smm.archetype.domain.common.file.File;
import org.smm.archetype.infrastructure._shared.client.oss.AbstractOssClient;
import org.smm.archetype.infrastructure._shared.generated.entity.FileBusinessDO;
import org.smm.archetype.infrastructure._shared.generated.entity.FileMetadataDO;
import org.smm.archetype.infrastructure._shared.generated.mapper.FileBusinessMapper;
import org.smm.archetype.infrastructure._shared.generated.mapper.FileMetadataMapper;
import org.smm.archetype.infrastructure.common.file.config.OssProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import static org.smm.archetype.infrastructure._shared.generated.entity.table.FileBusinessDOTableDef.FILE_BUSINESS_DO;
import static org.smm.archetype.infrastructure._shared.generated.entity.table.FileMetadataDOTableDef.FILE_METADATA_DO;

/**
 * RustFS 对象存储服务实现
 *
 * <p>基于 AWS S3 SDK v2 实现，RustFS 100% 兼容 S3 协议。
 *
 * <p>关键配置：
 * <ul>
 *   <li>forcePathStyle: true - RustFS 必须启用 Path-Style 访问</li>
 *   <li>endpointOverride: http://localhost:9000 - RustFS 服务地址</li>
 *   <li>region: US_EAST_1 - 任意值，RustFS 不校验</li>
 * </ul>
 * @author Leonardo
 * @since 2026-01-10
 */
@Slf4j
public class RustFsOssClientImpl extends AbstractOssClient {

    private final S3Client s3Client;
    private final String   bucket;

    /**
     * 构造 RustFS 对象存储服务
     * @param properties     对象存储配置
     * @param metadataMapper 文件元数据 Mapper
     * @param businessMapper 文件业务 Mapper
     * @param idClient       ID 生成服务
     */
    public RustFsOssClientImpl(
            OssProperties properties,
            FileMetadataMapper metadataMapper,
            FileBusinessMapper businessMapper,
            IdClient idClient) {

        super(properties, metadataMapper, businessMapper, idClient);

        OssProperties.RustFs rustfsConfig = properties.getRustfs();
        this.bucket = rustfsConfig.getBucket();

        // 创建 S3 Client（参考 RustFS 官方文档）
        this.s3Client = S3Client.builder()
                                .endpointOverride(URI.create(rustfsConfig.getEndpoint()))
                                .region(Region.US_EAST_1) // RustFS 不校验 region
                                .credentialsProvider(
                                        StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(
                                                        rustfsConfig.getAccessKey(),
                                                        rustfsConfig.getSecretKey()
                                                )
                                        )
                                )
                                .forcePathStyle(true) // 关键配置！RustFS 需启用 Path-Style
                                .build();

        // 确保 bucket 存在
        ensureBucketExists();
    }

    /**
     * 确保 bucket 存在，不存在则创建
     */
    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.debug("RustFS bucket already exists: {}", bucket);
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("RustFS bucket created: {}", bucket);
        } catch (Exception e) {
            log.warn("Failed to check/create bucket: {}, assuming it exists", bucket, e);
        }
    }

    @Override
    protected String doUpload(byte[] contentBytes, String fileName, String contentType, String fileId) throws Exception {
        // 生成 S3 key：yyyy/MM/fileId-fileName
        String datePath = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM"));
        String key = datePath + "/" + fileId + "-" + fileName;

        // 上传文件到 RustFS
        PutObjectRequest putRequest = PutObjectRequest.builder()
                                              .bucket(bucket)
                                              .key(key)
                                              .contentType(contentType)
                                              .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(contentBytes));

        log.debug("File uploaded to RustFS: bucket={}, key={}, size={}", bucket, key, contentBytes.length);
        return key;
    }

    @Override
    protected InputStream doDownload(String filePath, String fileId) throws Exception {
        // filePath 就是 S3 key
        GetObjectRequest getRequest = GetObjectRequest.builder()
                                              .bucket(bucket)
                                              .key(filePath)
                                              .build();

        byte[] bytes = s3Client.getObject(getRequest).readAllBytes();
        log.debug("File downloaded from RustFS: bucket={}, key={}, size={}", bucket, filePath, bytes.length);
        return new ByteArrayInputStream(bytes);
    }

    @Override
    protected void doDelete(String filePath, String fileId) throws Exception {
        // filePath 就是 S3 key
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                                                    .bucket(bucket)
                                                    .key(filePath)
                                                    .build();

        s3Client.deleteObject(deleteRequest);
        log.debug("File deleted from RustFS: bucket={}, key={}", bucket, filePath);
    }

    @Override
    protected String doGenerateUrl(String filePath, long expireSeconds) throws Exception {
        // TODO: 需要添加 s3-presigner 依赖才能实现预签名 URL 功能
        // 参考文档：https://docs.rustfs.com.cn/developer/sdk/java.html
        throw new UnsupportedOperationException("Presigned URL generation requires s3-presigner dependency");
    }

    @Override
    protected boolean doExists(String filePath) throws Exception {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                                                    .bucket(bucket)
                                                    .key(filePath)
                                                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    protected long doGetFileSize(String filePath) throws Exception {
        HeadObjectRequest headRequest = HeadObjectRequest.builder()
                                                .bucket(bucket)
                                                .key(filePath)
                                                .build();

        HeadObjectResponse response = s3Client.headObject(headRequest);
        return response.contentLength();
    }

    /**
     * 搜索文件（RustFS 不支持元数据搜索，只能从数据库查询）
     * @param fileNamePattern    文件名模式（支持 SQL LIKE）
     * @param businessEntityType 业务实体类型
     * @param businessId         业务ID
     * @return 文件列表
     */
    @Override
    protected java.util.List<org.smm.archetype.domain.common.file.File> doSearchFiles(
            String fileNamePattern,
            org.smm.archetype.domain.common.file.File.FileBusinessEntityType businessEntityType,
            String businessId) {

        log.debug("Searching files in database: pattern={}, businessType={}, businessId={}",
                fileNamePattern, businessEntityType, businessId);

        // 从数据库查询（RustFS 本身不支持元数据搜索）
        // 查询文件业务关联表
        java.util.List<FileBusinessDO> businessDOList = businessMapper.selectListByCondition(
                FILE_BUSINESS_DO.BUSINESS_ID.eq(businessId)
                        .and(FILE_BUSINESS_DO.TYPE.eq(businessEntityType.name()))
        );

        if (businessDOList.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 获取文件ID列表
        java.util.List<String> fileIds = businessDOList.stream()
                                                 .map(FileBusinessDO::getFileId)
                                                 .toList();

        // 查询文件元数据表
        var metadataCondition = FILE_METADATA_DO.FILE_ID.in(fileIds);
        java.util.List<FileMetadataDO> metadataDOList = metadataMapper.selectListByCondition(metadataCondition);

        // 按文件名过滤
        if (fileNamePattern != null && !fileNamePattern.isBlank()) {
            final String pattern = fileNamePattern;
            metadataDOList = metadataDOList.stream()
                                     .filter(metadata -> {
                                         // 从 businessDOList 中找到对应的 FileBusinessDO
                                         return businessDOList.stream()
                                                        .filter(b -> b.getFileId().equals(metadata.getFileId()))
                                                        .anyMatch(b -> b.getName() != null && b.getName().matches(".*" + pattern + ".*"));
                                     })
                                     .toList();
        }

        // 组装结果
        return metadataDOList.stream()
                       .map(metadata -> {
                           FileBusinessDO business = businessDOList.stream()
                                                             .filter(b -> b.getFileId().equals(metadata.getFileId()))
                                                             .findFirst()
                                                             .orElse(null);

                           if (business == null) {
                               return null;
                           }

                           return convertToFile(metadata, business);
                       })
                       .filter(java.util.Objects::nonNull)
                       .toList();
    }

    /**
     * 转换为 File 领域对象
     */
    private File convertToFile(FileMetadataDO metadataDO, FileBusinessDO businessDO) {

        File.FileBuilder fileBuilder = File.builder()
                                               .setFileId(metadataDO.getFileId())
                                               .setFilePath(metadataDO.getPath())
                                               .setFileUrl(metadataDO.getUrl())
                                               .setMd5(metadataDO.getMd5())
                                               .setContentType(metadataDO.getContentType())
                                               .setFileSize(metadataDO.getSize())
                                               .setStatus(File.FileStatus.ACTIVE)
                                               .setCreateTime(metadataDO.getCreateTime())
                                               .setUpdateTime(metadataDO.getUpdateTime());

        // 如果有业务关联信息，则设置
        if (businessDO != null) {
            fileBuilder.setFileName(businessDO.getName())
                    .setFileBusiness(File.FileBusiness.builder()
                                             .setBusinessEntityType(toBusinessEntityType(businessDO.getType()))
                                             .setBusinessId(businessDO.getBusinessId())
                                             .setUsageType(toUsageType(businessDO.getUsage()))
                                             .setOrder(businessDO.getSort())
                                             .build());
        }

        return fileBuilder.build();
    }

    /**
     * 转换业务实体类型
     */
    private File.FileBusinessEntityType toBusinessEntityType(String type) {
        try {
            return File.FileBusinessEntityType.valueOf(type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 转换使用类型
     */
    private File.UsageType toUsageType(String usage) {
        try {
            return File.UsageType.valueOf(usage);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 关闭资源
     */
    public void close() {
        try {
            if (s3Client != null) {
                s3Client.close();
            }
        } catch (Exception e) {
            log.warn("Failed to close S3 client", e);
        }
    }

}
