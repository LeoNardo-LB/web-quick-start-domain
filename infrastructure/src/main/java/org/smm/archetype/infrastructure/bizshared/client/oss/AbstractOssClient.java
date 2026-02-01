package org.smm.archetype.infrastructure.bizshared.client.oss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.client.OssClient;
import org.smm.archetype.domain.common.file.FileMetadata;
import org.smm.archetype.infrastructure.bizshared.dal.generated.entity.FileMetadataDO;
import org.smm.archetype.infrastructure.bizshared.dal.generated.mapper.FileMetadataMapper;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

import static org.smm.archetype.infrastructure.bizshared.dal.generated.entity.table.FileMetadataDOTableDef.FILE_METADATA_DO;

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
     * 文件元数据 Mapper
     */
    protected final FileMetadataMapper metadataMapper;

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

            // 2. 计算文件大小和 MD5
            byte[] contentBytes = inputStream.readAllBytes();
            long fileSize = contentBytes.length;
            String md5 = calculateMd5(contentBytes);

            // 3. 调用扩展点（由子类实现）
            String filePath = doUpload(contentBytes, fileName, contentType);
            log.debug("File uploaded to: {}", filePath);

            // 4. 持久化元数据到 file_metadata 表
            saveFileMetadata(fileName, md5, contentType, fileSize, filePath);

            log.info("File uploaded successfully: fileName={}, filePath={}", fileName, filePath);
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
            InputStream inputStream = doDownload(filePath);
            log.info("File downloaded successfully: filePath={}", filePath);

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
                log.warn("FileMetadata not found for deletion: filePath={}", filePath);
                return;
            }

            // 3. 调用扩展点（由子类实现）
            doDelete(filePath);

            // 4. TODO: 标记删除（如果需要软删除功能，可以在这里实现）

            log.info("File deleted successfully: filePath={}", filePath);

        } catch (Exception e) {
            log.error("Failed to delete file: filePath={}", filePath, e);
            throw new RuntimeException("FileMetadata delete failed: " + filePath, e);
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
    public final List<FileMetadata> searchFiles(String fileNamePattern) {
        log.debug("Searching files: fileNamePattern={}", fileNamePattern);

        try {

            // 1. 调用扩展点（由子类实现）
            List<FileMetadata> fileMetadata = doSearchFiles(fileNamePattern);
            log.debug("Found {} fileMetadata", fileMetadata.size());

            return fileMetadata;

        } catch (Exception e) {
            log.error("Failed to search files: {}", fileNamePattern, e);
            throw new RuntimeException("FileMetadata search failed", e);
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
            log.debug("FileMetadata exists: filePath={}, exists={}", filePath, exists);

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
            log.debug("FileMetadata size: filePath={}, size={}", filePath, size);

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
     * @return 文件存储路径
     * @throws Exception 上传失败
     */
    protected abstract String doUpload(byte[] contentBytes, String fileName, String contentType) throws Exception;

    /**
     * 下载文件（扩展点）
     * @param filePath 文件存储路径
     * @return 文件输入流
     * @throws Exception 下载失败
     */
    protected abstract InputStream doDownload(String filePath) throws Exception;

    /**
     * 删除文件（扩展点）
     * @param filePath 文件存储路径
     * @throws Exception 删除失败
     */
    protected abstract void doDelete(String filePath) throws Exception;

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
     * @param fileNamePattern 文件名模式（支持通配符）
     * @return 文件列表
     * @throws Exception 搜索失败
     */
    protected abstract List<FileMetadata> doSearchFiles(String fileNamePattern) throws Exception;

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
     * @param fileName    文件名
     * @param md5         文件 MD5
     * @param contentType MIME 类型
     * @param size        文件大小（字节）
     * @param path        文件存储路径
     */
    protected void saveFileMetadata(String fileName, String md5, String contentType, long size, String path) {
        FileMetadataDO metadata = new FileMetadataDO();
        metadata.setMd5(md5);
        metadata.setContentType(contentType);
        metadata.setSize(size);
        metadata.setPath(path);

        metadataMapper.insert(metadata);
        log.debug("File metadata saved: id={}, path={}", metadata.getId(), path);
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
