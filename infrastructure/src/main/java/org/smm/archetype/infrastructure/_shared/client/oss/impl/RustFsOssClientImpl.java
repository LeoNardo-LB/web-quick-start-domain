package org.smm.archetype.infrastructure._shared.client.oss.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.IdClient;
import org.smm.archetype.domain.common.file.FileMetadata;
import org.smm.archetype.domain.common.file.FileMetadata.Status;
import org.smm.archetype.infrastructure._shared.client.oss.AbstractOssClient;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.FileMetadataDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.FileMetadataMapper;
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
import java.util.List;

import static org.smm.archetype.infrastructure._shared.generated.repository.entity.table.FileMetadataDOTableDef.FILE_METADATA_DO;

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
    private final String bucket;

    /**
     * 构造 RustFS 对象存储服务
     * @param endpoint       RustFS服务器地址
     * @param accessKey      Access Key
     * @param secretKey      Secret Key
     * @param bucket         Bucket名称
     * @param metadataMapper 文件元数据 Mapper
     * @param idClient       ID 生成服务
     */
    public RustFsOssClientImpl(
            String endpoint,
            String accessKey,
            String secretKey,
            String bucket,
            FileMetadataMapper metadataMapper,
            IdClient idClient) {

        super(metadataMapper, idClient);
        this.bucket = bucket;

        // 创建 S3 Client（参考 RustFS 官方文档）
        this.s3Client = S3Client.builder()
                                .endpointOverride(URI.create(endpoint))
                                .region(Region.US_EAST_1) // RustFS 不校验 region
                                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
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
    protected String doUpload(byte[] contentBytes, String fileName, String contentType) throws Exception {
        // 生成 S3 key：yyyy/MM/timestamp-fileName
        String datePath = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM"));

        // 使用纳秒时间戳作为文件标识（与 LocalOssClientImpl 保持一致）
        String timestamp = String.valueOf(System.nanoTime());
        String key = datePath + "/" + timestamp + "-" + fileName;

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
    protected InputStream doDownload(String filePath) throws Exception {
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
    protected void doDelete(String filePath) throws Exception {
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
     * @param fileNamePattern 文件名模式（支持 SQL LIKE）
     * @return 文件列表
     */
    @Override
    protected List<FileMetadata> doSearchFiles(String fileNamePattern) {

        log.debug("Searching files in database: pattern={}", fileNamePattern);

        // 从数据库查询（RustFS 本身不支持元数据搜索）
        QueryWrapper query = QueryWrapper.create()
                                     .select()
                                     .from(FILE_METADATA_DO);

        // 如果有文件名模式，添加模糊查询
        if (fileNamePattern != null && !fileNamePattern.isBlank()) {
            String sqlPattern = fileNamePattern.replace("*", "%").replace("?", "_");
            query.where(FILE_METADATA_DO.PATH.like(sqlPattern));
        }

        List<FileMetadataDO> metadataDOList = metadataMapper.selectListByQuery(query);

        // 转换为 FileMetadata 领域对象
        return metadataDOList.stream()
                       .map(this::convertToFile)
                       .toList();
    }

    /**
     * 转换为 FileMetadata 领域对象
     */
    private FileMetadata convertToFile(FileMetadataDO metadataDO) {
        // 从path中提取文件名
        String fileName = metadataDO.getPath();
        if (fileName != null && fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            // 移除时间戳前缀（如果存在）
            if (fileName.contains("-")) {
                fileName = fileName.substring(fileName.indexOf("-") + 1);
            }
        }

        return FileMetadata.builder()
                       .setFileName(fileName)
                       .setFilePath(metadataDO.getPath())
                       .setFileUrl(metadataDO.getUrl())
                       .setMd5(metadataDO.getMd5())
                       .setContentType(metadataDO.getContentType())
                       .setFileSize(metadataDO.getSize())
                       .setStatus(Status.ACTIVE)
                       .setCreateTime(metadataDO.getCreateTime())
                       .setUpdateTime(metadataDO.getUpdateTime())
                       .build();
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
