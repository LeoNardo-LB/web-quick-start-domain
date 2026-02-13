package org.smm.archetype.infrastructure.shared.client.oss;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.client.OssClient;
import org.smm.archetype.domain.shared.exception.ClientErrorCode;
import org.smm.archetype.domain.shared.exception.ClientException;
import org.smm.archetype.domain.platform.file.FileMetadata;
import org.smm.archetype.domain.platform.file.FileMetadata.Status;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.FileMetadataDO;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.FileMetadataMapper;
import org.smm.archetype.infrastructure.shared.util.context.ScopedThreadContext;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

import static org.smm.archetype.infrastructure.shared.dal.generated.entity.table.FileMetadataDOTableDef.FILE_METADATA_DO;

/**
 * 对象存储服务抽象基类，提供通用文件操作流程。
 *
 * <p>职责划分：
 * <ul>
 *   <li>数据库操作：统一由抽象基类负责（查询、保存、删除元数据）</li>
 *   <li>外部能力：由具体实现类负责（文件系统、S3等）</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOssClient implements OssClient {

    /**
     * 文件元数据 Mapper（数据库访问）
     */
    protected final FileMetadataMapper metadataMapper;

    // ==================== OssClient 接口实现（模板方法） ====================

    @Override
    public final String upload(InputStream inputStream, String fileName, String contentType) {
        log.info("正在上传文件: 文件名={}, 内容类型={}", fileName, contentType);

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

            // 3. 调用扩展点（由子类实现 - 外部能力）
            String filePath = doUpload(contentBytes, fileName, contentType);
            log.debug("文件已上传到: 路径={}", filePath);

            // 4. 持久化元数据到 file_metadata 表（数据库操作）
            saveFileMetadata(md5, contentType, fileSize, filePath);

            log.info("文件上传成功: 文件名={}, 路径={}", fileName, filePath);
            return filePath;

        } catch (Exception e) {
            log.error("文件上传失败: 文件名={}", fileName, e);
            throw new ClientException("File upload failed: " + fileName, e, ClientErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    public final InputStream download(String filePath) {
        log.info("正在下载文件: 路径={}", filePath);

        try {
            // 1. 参数验证
            if (filePath == null || filePath.isBlank()) {
                throw new IllegalArgumentException("FilePath cannot be null or blank");
            }

            // 2. 从数据库查询文件元数据（数据库操作）
            FileMetadataDO metadata = metadataMapper.selectOneByQuery(
                    QueryWrapper.create()
                            .select()
                            .from(FILE_METADATA_DO)
                            .where(FILE_METADATA_DO.PATH.eq(filePath))
            );

            if (metadata == null) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }

            // 3. 调用扩展点（由子类实现 - 外部能力）
            InputStream inputStream = doDownload(filePath);
            log.info("File downloaded successfully: filePath={}", filePath);

            return inputStream;

        } catch (Exception e) {
            log.error("文件下载失败: 路径={}", filePath, e);
            throw new ClientException("File download failed: " + filePath, e, ClientErrorCode.OPERATION_FAILED);
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

            // 2. 从数据库查询文件元数据（数据库操作）
            FileMetadataDO metadata = metadataMapper.selectOneByQuery(
                    QueryWrapper.create()
                            .select()
                            .from(FILE_METADATA_DO)
                            .where(FILE_METADATA_DO.PATH.eq(filePath))
            );

            if (metadata == null) {
                log.warn("删除时文件元数据未找到: 路径={}", filePath);
                return;
            }

            // 3. 调用扩展点（由子类实现 - 外部能力）
            doDelete(filePath);

            // 4. 标记删除元数据（数据库操作）
            deleteFileMetadata(metadata.getId());

            log.info("文件删除成功: 路径={}", filePath);

        } catch (Exception e) {
            log.error("Failed to delete file: filePath={}", filePath, e);
            throw new ClientException("FileMetadata delete failed: " + filePath, e, ClientErrorCode.OPERATION_FAILED);
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

            // 2. 调用扩展点（由子类实现 - 外部能力）
            String url = doGenerateUrl(filePath, expireSeconds);
            log.debug("URL generated successfully: filePath={}, url={}", filePath, url);

            return url;

        } catch (Exception e) {
            log.error("文件生成URL失败: 路径={}", filePath, e);
            throw new ClientException("URL generation failed: " + filePath, e, ClientErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    public final List<FileMetadata> searchFiles(String fileNamePattern) {
        log.debug("Searching files: fileNamePattern={}", fileNamePattern);

        try {
            // 1. 从数据库查询文件元数据（数据库操作）
            List<FileMetadataDO> metadataDOs = queryFileMetadataByPattern(fileNamePattern);
            log.debug("Found {} fileMetadata", metadataDOs.size());

            // 2. 转换为领域对象
            return metadataDOs.stream()
                           .map(this::convertToFileMetadata)
                           .toList();

        } catch (Exception e) {
            log.error("文件搜索失败: 文件名模式={}", fileNamePattern, e);
            throw new ClientException("FileMetadata search failed", e, ClientErrorCode.OPERATION_FAILED);
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

            // 2. 调用扩展点（由子类实现 - 外部能力）
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

            // 2. 调用扩展点（由子类实现 - 外部能力）
            long size = doGetFileSize(filePath);
            log.debug("FileMetadata size: filePath={}, size={}", filePath, size);

            return size;

        } catch (Exception e) {
            log.error("Failed to get file size: filePath={}", filePath, e);
            throw new ClientException("Failed to get file size: " + filePath, e, ClientErrorCode.OPERATION_FAILED);
        }
    }

    // ==================== 扩展点（由子类实现 - 仅处理外部能力） ====================

    /**
     * 上传文件（扩展点 - 外部能力）
     * @param contentBytes 文件内容字节数组
     * @param fileName     文件名
     * @param contentType  MIME 类型
     * @return 文件存储路径
     * @throws Exception 上传失败
     */
    protected abstract String doUpload(byte[] contentBytes, String fileName, String contentType) throws Exception;

    /**
     * 下载文件（扩展点 - 外部能力）
     * @param filePath 文件存储路径
     * @return 文件输入流
     * @throws Exception 下载失败
     */
    protected abstract InputStream doDownload(String filePath) throws Exception;

    /**
     * 删除文件（扩展点 - 外部能力）
     * @param filePath 文件存储路径
     * @throws Exception 删除失败
     */
    protected abstract void doDelete(String filePath) throws Exception;

    /**
     * 生成访问 URL（扩展点 - 外部能力）
     * @param filePath      文件存储路径
     * @param expireSeconds 过期时间（秒），0 表示永久有效
     * @return 访问 URL
     * @throws Exception URL 生成失败
     */
    protected abstract String doGenerateUrl(String filePath, long expireSeconds) throws Exception;

    /**
     * 检查文件是否存在（扩展点 - 外部能力）
     * @param filePath 文件存储路径
     * @return true-存在，false-不存在
     * @throws Exception 检查失败
     */
    protected abstract boolean doExists(String filePath) throws Exception;

    /**
     * 获取文件大小（扩展点 - 外部能力）
     * @param filePath 文件存储路径
     * @return 文件大小（字节）
     * @throws Exception 获取失败
     */
    protected abstract long doGetFileSize(String filePath) throws Exception;

    // ==================== 数据库操作（统一在基类中） ====================

    /**
     * 保存文件元数据到 file_metadata 表
     * @param md5         文件 MD5
     * @param contentType MIME 类型
     * @param size        文件大小（字节）
     * @param path        文件存储路径
     */
    protected void saveFileMetadata(String md5, String contentType, long size, String path) {
        FileMetadataDO metadata = new FileMetadataDO();
        metadata.setMd5(md5);
        metadata.setContentType(contentType);
        metadata.setSize(size);
        metadata.setPath(path);

        metadataMapper.insert(metadata);
        log.debug("File metadata saved: id={}, path={}", metadata.getId(), path);
    }

    /**
     * 删除文件元数据（软删除）
     *
     * <p>软删除策略：
     * <ul>
     *   <li>设置 deleteTime 为当前时间戳</li>
     *   <li>设置 deleteUser 为当前操作用户（如果有）</li>
     *   <li>保留记录以便审计和恢复</li>
     * </ul>
     *
     * @param metadataId 元数据ID
     */
    protected void deleteFileMetadata(Long metadataId) {
        if (metadataId == null) {
            log.warn("元数据ID为空，跳过删除");
            return;
        }

        try {
            // 查询元数据
            FileMetadataDO metadata = metadataMapper.selectOneById(metadataId);

            if (metadata == null) {
                log.warn("文件元数据未找到: id={}", metadataId);
                return;
            }

            // 检查是否已经删除
            if (metadata.getDeleteTime() != null) {
                log.debug("文件元数据已删除，跳过: id={}", metadataId);
                return;
            }

            // 执行软删除
            metadata.setDeleteTime(System.currentTimeMillis());
            metadata.setDeleteUser(ScopedThreadContext.getUserId());

            int updated = metadataMapper.update(metadata);

            if (updated > 0) {
                log.info("文件元数据软删除成功: id={}, path={}, deleteTime={}",
                        metadataId, metadata.getPath(), metadata.getDeleteTime());
            } else {
                log.warn("文件元数据删除失败: id={}", metadataId);
            }

        } catch (Exception e) {
            log.error("文件元数据软删除异常: id={}", metadataId, e);
            // 不抛出异常，避免影响外部文件删除操作
        }
    }

    /**
     * 根据文件名模式查询文件元数据
     * @param fileNamePattern 文件名模式（支持通配符 * 和 ?）
     * @return 文件元数据列表
     */
    protected List<FileMetadataDO> queryFileMetadataByPattern(String fileNamePattern) {
        QueryWrapper query = QueryWrapper.create()
                                     .select()
                                     .from(FILE_METADATA_DO);

        // 如果有文件名模式，添加模糊查询
        if (fileNamePattern != null && !fileNamePattern.isBlank()) {
            // 将通配符 * 和 ? 转换为 SQL 的 % 和 _
            String sqlPattern = fileNamePattern.replace("*", "%").replace("?", "_");
            query.where(FILE_METADATA_DO.PATH.like(sqlPattern));
        }

        return metadataMapper.selectListByQuery(query);
    }

    /**
     * 将 FileMetadataDO 转换为 FileMetadata 领域对象
     * @param metadataDO 文件元数据DO
     * @return FileMetadata 领域对象
     */
    protected FileMetadata convertToFileMetadata(FileMetadataDO metadataDO) {
        if (metadataDO == null) {
            return null;
        }

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

    // ==================== 辅助方法 ====================

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
            throw new ClientException("MD5 calculation failed", e, ClientErrorCode.OPERATION_FAILED);
        }
    }

}
