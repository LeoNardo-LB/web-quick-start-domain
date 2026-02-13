// package org.smm.archetype.infrastructure.shared.client.oss;
//
// import lombok.extern.slf4j.Slf4j;
// import org.smm.archetype.infrastructure.shared.dal.generated.mapper.FileMetadataMapper;
// import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
// import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
// import software.amazon.awssdk.core.sync.RequestBody;
// import software.amazon.awssdk.regions.Region;
// import software.amazon.awssdk.services.s3.S3Client;
// import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
// import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
// import software.amazon.awssdk.services.s3.model.GetObjectRequest;
// import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
// import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
// import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
// import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
// import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
// import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//
// import java.io.ByteArrayInputStream;
// import java.io.InputStream;
// import java.net.URI;
//
// /**
//  * RustFS OSS实现，基于AWS S3 SDK。
//  *
//  * <p>职责划分：
//  * <ul>
//  *   <li>外部能力：RustFS S3 API 操作（上传、下载、删除等）</li>
//  *   <li>数据库操作：委托给父类 AbstractOssClient 处理</li>
//  * </ul>
//  */
// @Slf4j
// public class RustFsOssClientImpl extends AbstractOssClient {
//
//     private final S3Client s3Client;
//     private final String bucket;
//
//     /**
//      * 上传文件到RustFS，返回S3 key。
//      */
//     public RustFsOssClientImpl(
//             String endpoint,
//             String accessKey,
//             String secretKey,
//             String bucket,
//             FileMetadataMapper metadataMapper) {
//
//         super(metadataMapper);
//         this.bucket = bucket;
//
//         // 创建 S3 Client（参考 RustFS 官方文档）
//         this.s3Client = S3Client.builder()
//                                 .endpointOverride(URI.create(endpoint))
//                                 .region(Region.US_EAST_1) // RustFS 不校验 region
//                                 .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
//                                 .forcePathStyle(true) // 关键配置！RustFS 需启用 Path-Style
//                                 .build();
//
//         // 确保 bucket 存在
//         ensureBucketExists();
//     }
//
//     /**
//      * 确保bucket存在，不存在则创建。
//      */
//     private void ensureBucketExists() {
//         try {
//             s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
//             log.debug("RustFS bucket already exists: {}", bucket);
//         } catch (NoSuchBucketException e) {
//             s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
//             log.info("RustFS bucket created: {}", bucket);
//         } catch (Exception e) {
//             log.warn("Failed to check/create bucket: {}, assuming it exists", bucket, e);
//         }
//     }
//
//     // ==================== 外部能力实现（子类职责） ====================
//
//     @Override
//     protected String doUpload(byte[] contentBytes, String fileName, String contentType) {
//         // 生成 S3 key：yyyy/MM/timestamp-fileName
//         String datePath = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM"));
//
//         // 使用纳秒时间戳作为文件标识（与 LocalOssClientImpl 保持一致）
//         String timestamp = String.valueOf(System.nanoTime());
//         String key = datePath + "/" + timestamp + "-" + fileName;
//
//         // 上传文件到 RustFS
//         PutObjectRequest putRequest = PutObjectRequest.builder()
//                                               .bucket(bucket)
//                                               .key(key)
//                                               .contentType(contentType)
//                                               .build();
//
//         s3Client.putObject(putRequest, RequestBody.fromBytes(contentBytes));
//
//         log.debug("File uploaded to RustFS: bucket={}, key={}, size={}", bucket, key, contentBytes.length);
//         return key;
//     }
//
//     @Override
//     protected InputStream doDownload(String filePath) throws Exception {
//         // filePath 就是 S3 key
//         GetObjectRequest getRequest = GetObjectRequest.builder()
//                                               .bucket(bucket)
//                                               .key(filePath)
//                                               .build();
//
//         byte[] bytes = s3Client.getObject(getRequest).readAllBytes();
//         log.debug("File downloaded from RustFS: bucket={}, key={}, size={}", bucket, filePath, bytes.length);
//         return new ByteArrayInputStream(bytes);
//     }
//
//     @Override
//     protected void doDelete(String filePath) {
//         // filePath 就是 S3 key
//         DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
//                                                     .bucket(bucket)
//                                                     .key(filePath)
//                                                     .build();
//
//         s3Client.deleteObject(deleteRequest);
//         log.debug("File deleted from RustFS: bucket={}, key={}", bucket, filePath);
//     }
//
//     @Override
//     protected String doGenerateUrl(String filePath, long expireSeconds) {
//         // 生成 S3 预签名 URL
//         //
//         // 注意：预签名 URL 需要添加 s3-presigner 依赖
//         // 依赖配置：
//         // <dependency>
//         //     <groupId>software.amazon.awssdk</groupId>
//         //     <artifactId>s3-presigner</artifactId>
//         //     <version>${aws.java.sdk.version}</version>
//         // </dependency>
//         //
//         // 参考文档：https://docs.rustfs.com.cn/developer/sdk/java.html
//
//         if (expireSeconds == 0) {
//             // 永久有效 URL（需要 bucket 设置为公开访问）
//             // 格式: http://endpoint:port/bucket/key
//             return buildPublicUrl(filePath);
//         } else {
//             // 临时有效 URL（需要预签名）
//             // 由于当前项目未引入 s3-presigner 依赖，提供降级方案
//             log.warn("预签名 URL 功能需要 s3-presigner 依赖，返回公开 URL 作为降级方案");
//
//             // 降级方案：返回公开 URL（假设 bucket 已配置为公开访问）
//             // 注意：如果 bucket 未公开访问，此 URL 将无法访问
//             return buildPublicUrl(filePath);
//         }
//     }
//
//     /**
//      * 构建公开访问 URL
//      * @param filePath 文件路径（S3 key）
//      * @return 公开访问 URL
//      */
//     private String buildPublicUrl(String filePath) {
//         // 从 S3Client 获取 endpoint
//         String endpoint = s3Client.serviceClientConfiguration().endpointOverride().toString();
//
//         // 构建公开 URL: {endpoint}/{bucket}/{filePath}
//         // 注意：如果 endpoint 以 / 结尾，需要去掉
//         String url = endpoint;
//         if (!url.endsWith("/")) {
//             url += "/";
//         }
//         url += bucket + "/" + filePath;
//
//         log.debug("生成公开访问 URL: path={}, url={}", filePath, url);
//         return url;
//     }
//
//     @Override
//     protected boolean doExists(String filePath) {
//         try {
//             HeadObjectRequest headRequest = HeadObjectRequest.builder()
//                                                     .bucket(bucket)
//                                                     .key(filePath)
//                                                     .build();
//
//             s3Client.headObject(headRequest);
//             return true;
//         } catch (NoSuchKeyException e) {
//             return false;
//         }
//     }
//
//     @Override
//     protected long doGetFileSize(String filePath) {
//         HeadObjectRequest headRequest = HeadObjectRequest.builder()
//                                                 .bucket(bucket)
//                                                 .key(filePath)
//                                                 .build();
//
//         HeadObjectResponse response = s3Client.headObject(headRequest);
//         return response.contentLength();
//     }
//
// }
