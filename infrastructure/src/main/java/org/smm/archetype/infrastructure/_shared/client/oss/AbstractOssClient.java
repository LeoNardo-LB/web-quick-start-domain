package org.smm.archetype.infrastructure._shared.client.oss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.IdClient;
import org.smm.archetype.domain._shared.client.OssClient;
import org.smm.archetype.domain.common.file.File;
import org.smm.archetype.infrastructure._shared.generated.entity.FileBusinessDO;
import org.smm.archetype.infrastructure._shared.generated.entity.FileMetadataDO;
import org.smm.archetype.infrastructure._shared.generated.mapper.FileBusinessMapper;
import org.smm.archetype.infrastructure._shared.generated.mapper.FileMetadataMapper;
import org.smm.archetype.infrastructure.common.file.config.OssProperties;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

import static org.smm.archetype.infrastructure._shared.generated.entity.table.FileMetadataDOTableDef.FILE_METADATA_DO;

/**
 * 对象存储服务抽象基类
 *
 * <p>实现模板方法模式，提供通用的文件操作流程：
 * <ul>
 *   <li>参数验证</li>
 *   <li>调用扩展点（由子类实现）</li>
 *   <li>持久化元数据到数据库</li>
 *   <li>日志记录</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/10
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOssClient implements OssClient {

    /**
     * 对象存储配置
     */
    protected final OssProperties properties;

    /**
     * 文件元数据 Mapper
     */
    protected final FileMetadataMapper metadataMapper;

    /**
     * 文件业务关联 Mapper
     */
    protected final FileBusinessMapper businessMapper;

    /**
     * ID 生成服务
     */
    protected final IdClient idClient;

    // ==================== OssClient 接口实现（模板方法） ====================

    @Override
    public final String upload(InputStream inputStream, String fileName, String contentType) {
        log.info("Uploading file: fileName={}, contentType={}", fileName, contentType);

        try {
            // 1. 参数验证
            if (inputStream == null) {
                throw new IllegalArgumentException("InputStream cannot be null");
            }
            if (fileName == null || fileName.isBlank()) {
                throw new IllegalArgumentException("FileName cannot be null or blank");
            }

            // 2. 生成 fileId
            String fileId = idClient.generateId(IdClient.Type.FILE);
            log.debug("Generated fileId: {}", fileId);

            // 3. 计算文件大小和 MD5
            byte[] contentBytes = inputStream.readAllBytes();
            long fileSize = contentBytes.length;
            String md5 = calculateMd5(contentBytes);

            // 4. 调用扩展点（由子类实现）
            String filePath = doUpload(contentBytes, fileName, contentType, fileId);
            log.debug("File uploaded to: {}", filePath);

            // 5. 持久化元数据到 file_metadata 表
            saveFileMetadata(fileId, fileName, md5, contentType, fileSize, filePath);

            log.info("File uploaded successfully: fileName={}, fileId={}, filePath={}", fileName, fileId, filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Failed to upload file: fileName={}", fileName, e);
            throw new RuntimeException("File upload failed: " + fileName, e);
        }
    }

    @Override
    public final InputStream download(String filePath) {
        log.info("Downloading file: filePath={}", filePath);

        try {
            // 1. 参数验证
            if (filePath == null || filePath.isBlank()) {
                throw new IllegalArgumentException("FilePath cannot be null or blank");
            }

            // 2. 从数据库查询文件元数据
            FileMetadataDO metadata = metadataMapper.selectOneByQuery(
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .select()
                            .from(FILE_METADATA_DO)
                            .where(FILE_METADATA_DO.PATH.eq(filePath))
            );

            if (metadata == null) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }

            // 3. 调用扩展点（由子类实现）
            InputStream inputStream = doDownload(filePath, metadata.getFileId());
            log.info("File downloaded successfully: filePath={}, fileId={}", filePath, metadata.getFileId());

            return inputStream;

        } catch (Exception e) {
            log.error("Failed to download file: filePath={}", filePath, e);
            throw new RuntimeException("File download failed: " + filePath, e);
        }
    }

    @Override
    public final void delete(String filePath) {
        log.info("Deleting file: filePath={}", filePath);

        try {
            // 1. 参数验证
            if (filePath == null || filePath.isBlank()) {
                throw new IllegalArgumentException("FilePath cannot be null or blank");
            }

            // 2. 从数据库查询文件元数据
            FileMetadataDO metadata = metadataMapper.selectOneByQuery(
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .select()
                            .from(FILE_METADATA_DO)
                            .where(FILE_METADATA_DO.PATH.eq(filePath))
            );

            if (metadata == null) {
                log.warn("File not found for deletion: filePath={}", filePath);
                return;
            }

            // 3. 调用扩展点（由子类实现）
            doDelete(filePath, metadata.getFileId());

            // 4. TODO: 标记删除（如果需要软删除功能，可以在这里实现）

            log.info("File deleted successfully: filePath={}, fileId={}", filePath, metadata.getFileId());

        } catch (Exception e) {
            log.error("Failed to delete file: filePath={}", filePath, e);
            throw new RuntimeException("File delete failed: " + filePath, e);
        }
    }

    @Override
    public final String generateUrl(String filePath, long expireSeconds) {
        log.debug("Generating URL: filePath={}, expireSeconds={}", filePath, expireSeconds);

        try {
            // 1. 参数验证
            if (filePath == null || filePath.isBlank()) {
                throw new IllegalArgumentException("FilePath cannot be null or blank");
            }

            // 2. 调用扩展点（由子类实现）
            String url = doGenerateUrl(filePath, expireSeconds);
            log.debug("URL generated successfully: filePath={}, url={}", filePath, url);

            return url;

        } catch (Exception e) {
            log.error("Failed to generate URL: filePath={}", filePath, e);
            throw new RuntimeException("URL generation failed: " + filePath, e);
        }
    }

    @Override
    public final List<File> searchFiles(String fileNamePattern, File.FileBusinessEntityType businessEntityType, String businessId) {
        log.debug("Searching files: fileNamePattern={}, businessEntityType={}, businessId={}",
                fileNamePattern, businessEntityType, businessId);

        try {
            // 1. 参数验证
            if (businessEntityType == null) {
                throw new IllegalArgumentException("BusinessEntityType cannot be null");
            }
            if (businessId == null || businessId.isBlank()) {
                throw new IllegalArgumentException("BusinessId cannot be null or blank");
            }

            // 2. 调用扩展点（由子类实现）
            List<File> files = doSearchFiles(fileNamePattern, businessEntityType, businessId);
            log.debug("Found {} files", files.size());

            return files;

        } catch (Exception e) {
            log.error("Failed to search files: businessEntityType={}, businessId={}", businessEntityType, businessId, e);
            throw new RuntimeException("File search failed", e);
        }
    }

    @Override
    public final boolean exists(String filePath) {
        log.debug("Checking file existence: filePath={}", filePath);

        try {
            // 1. 参数验证
            if (filePath == null || filePath.isBlank()) {
                return false;
            }

            // 2. 调用扩展点（由子类实现）
            boolean exists = doExists(filePath);
            log.debug("File exists: filePath={}, exists={}", filePath, exists);

            return exists;

        } catch (Exception e) {
            log.error("Failed to check file existence: filePath={}", filePath, e);
            return false;
        }
    }

    @Override
    public final long getFileSize(String filePath) {
        log.debug("Getting file size: filePath={}", filePath);

        try {
            // 1. 参数验证
            if (filePath == null || filePath.isBlank()) {
                throw new IllegalArgumentException("FilePath cannot be null or blank");
            }

            // 2. 调用扩展点（由子类实现）
            long size = doGetFileSize(filePath);
            log.debug("File size: filePath={}, size={}", filePath, size);

            return size;

        } catch (Exception e) {
            log.error("Failed to get file size: filePath={}", filePath, e);
            throw new RuntimeException("Failed to get file size: " + filePath, e);
        }
    }

    // ==================== 扩展点（由子类实现） ====================

    /**
     * 上传文件（扩展点）
     * @param contentBytes 文件内容字节数组
     * @param fileName     文件名
     * @param contentType  MIME 类型
     * @param fileId       文件唯一标识
     * @return 文件存储路径
     * @throws Exception 上传失败
     */
    protected abstract String doUpload(byte[] contentBytes, String fileName, String contentType, String fileId) throws Exception;

    /**
     * 下载文件（扩展点）
     * @param filePath 文件存储路径
     * @param fileId   文件唯一标识
     * @return 文件输入流
     * @throws Exception 下载失败
     */
    protected abstract InputStream doDownload(String filePath, String fileId) throws Exception;

    /**
     * 删除文件（扩展点）
     * @param filePath 文件存储路径
     * @param fileId   文件唯一标识
     * @throws Exception 删除失败
     */
    protected abstract void doDelete(String filePath, String fileId) throws Exception;

    /**
     * 生成访问 URL（扩展点）
     * @param filePath      文件存储路径
     * @param expireSeconds 过期时间（秒），0 表示永久有效
     * @return 访问 URL
     * @throws Exception URL 生成失败
     */
    protected abstract String doGenerateUrl(String filePath, long expireSeconds) throws Exception;

    /**
     * 搜索文件（扩展点）
     * @param fileNamePattern    文件名模式（支持通配符）
     * @param businessEntityType 业务实体类型
     * @param businessId         业务 ID
     * @return 文件列表
     * @throws Exception 搜索失败
     */
    protected abstract List<File> doSearchFiles(String fileNamePattern, File.FileBusinessEntityType businessEntityType, String businessId)
            throws Exception;

    /**
     * 检查文件是否存在（扩展点）
     * @param filePath 文件存储路径
     * @return true-存在，false-不存在
     * @throws Exception 检查失败
     */
    protected abstract boolean doExists(String filePath) throws Exception;

    /**
     * 获取文件大小（扩展点）
     * @param filePath 文件存储路径
     * @return 文件大小（字节）
     * @throws Exception 获取失败
     */
    protected abstract long doGetFileSize(String filePath) throws Exception;

    // ==================== 辅助方法 ====================

    /**
     * 持久化文件元数据到 file_metadata 表
     * @param fileId      文件唯一标识
     * @param fileName    文件名
     * @param md5         文件 MD5
     * @param contentType MIME 类型
     * @param size        文件大小（字节）
     * @param path        文件存储路径
     */
    protected void saveFileMetadata(String fileId, String fileName, String md5, String contentType, long size, String path) {
        FileMetadataDO metadata = FileMetadataDO.builder()
                                          .fileId(fileId)
                                          .md5(md5)
                                          .contentType(contentType)
                                          .size(size)
                                          .path(path)
                                          .build();

        metadataMapper.insert(metadata);
        log.debug("File metadata saved: fileId={}", fileId);
    }

    /**
     * 持久化文件业务关联到 file_business 表
     * @param fileId             文件唯一标识
     * @param businessName       业务文件名
     * @param businessEntityType 业务实体类型
     * @param businessId         业务 ID
     * @param usageType          使用场景
     */
    protected void saveFileBusiness(String fileId, String businessName, File.FileBusinessEntityType businessEntityType,
                                    String businessId, File.UsageType usageType) {
        FileBusinessDO business = FileBusinessDO.builder()
                                          .fileId(fileId)
                                          .name(businessName)
                                          .type(businessEntityType.name())
                                          .businessId(businessId)
                                          .usage(usageType.name())
                                          .sort(0)
                                          .build();

        businessMapper.insert(business);
        log.debug("File business saved: fileId={}, businessId={}", fileId, businessId);
    }

    /**
     * 计算 MD5 哈希值
     * @param data 字节数组
     * @return MD5 哈希值（32 位十六进制字符串）
     */
    protected String calculateMd5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to calculate MD5", e);
            throw new RuntimeException("MD5 calculation failed", e);
        }
    }

}
