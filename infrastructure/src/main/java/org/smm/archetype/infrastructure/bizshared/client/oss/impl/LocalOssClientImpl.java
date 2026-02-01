package org.smm.archetype.infrastructure.bizshared.client.oss.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.common.file.FileMetadata;
import org.smm.archetype.domain.common.file.FileMetadata.Status;
import org.smm.archetype.infrastructure.bizshared.client.oss.AbstractOssClient;
import org.smm.archetype.infrastructure.bizshared.dal.generated.entity.FileMetadataDO;
import org.smm.archetype.infrastructure.bizshared.dal.generated.mapper.FileMetadataMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.smm.archetype.infrastructure.bizshared.dal.generated.entity.table.FileMetadataDOTableDef.FILE_METADATA_DO;

/**
 * 本地文件系统OSS实现，支持零拷贝和日期分层存储。
 * @author Leonardo
 * @since 2026/1/10
 */
@Slf4j
public class LocalOssClientImpl extends AbstractOssClient {

    private final Path baseStoragePath;
    private final boolean zeroCopy;

    /**
     * 初始化本地OSS客户端。
     * @param basePath 基础存储路径（可选）
     * @param zeroCopy 是否使用零拷贝
     * @param metadataMapper 文件元数据Mapper
     */
    public LocalOssClientImpl(String basePath,
                              boolean zeroCopy,
                              FileMetadataMapper metadataMapper) throws IOException {
        super(metadataMapper);
        this.zeroCopy = zeroCopy;

        // 解析基础路径: 用户文件夹/.project/${spring.application.name}/oss
        String userHome = System.getProperty("user.home");
        String appName = System.getProperty("spring.application.name", "quickstart");

        if (basePath != null && !basePath.isBlank()) {
            this.baseStoragePath = Paths.get(basePath);
        } else {
            this.baseStoragePath = Paths.get(userHome, ".project", appName, "oss");
        }

        // 创建目录（如果不存在）
        Files.createDirectories(baseStoragePath);
        log.info("本地对象存储初始化: basePath={}, 零拷贝={}",
                baseStoragePath.toAbsolutePath(), zeroCopy);
    }

    @Override
    protected String doUpload(byte[] contentBytes, String fileName, String contentType) throws Exception {
        // 生成文件路径: {year}/{month}/{timestamp}-{fileName}
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path targetPath = baseStoragePath.resolve(datePath);

        // 创建日期目录
        Files.createDirectories(targetPath);

        // 使用纳秒时间戳作为文件标识（保证唯一性）
        String timestamp = String.valueOf(System.nanoTime());
        Path filePath = targetPath.resolve(timestamp + "-" + fileName);

        // 零拷贝上传
        if (zeroCopy) {
            // 先写入临时文件
            Path tempFile = Files.createTempFile("upload-", ".tmp");
            try {
                Files.write(tempFile, contentBytes, StandardOpenOption.WRITE);

                // NIO transferTo 零拷贝
                try (FileChannel sourceChannel = FileChannel.open(tempFile, StandardOpenOption.READ);
                     FileChannel targetChannel = FileChannel.open(filePath,
                             StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                    long size = sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
                    log.debug("零拷贝上传完成: 大小={}, 路径={}", size, filePath);
                }
            } finally {
                // 删除临时文件
                Files.deleteIfExists(tempFile);
            }
        } else {
            // 传统拷贝方式
            Files.write(filePath, contentBytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            log.debug("传统上传完成: 路径={}", filePath);
        }

        // 返回相对路径
        return datePath + "/" + timestamp + "-" + fileName;
    }

    @Override
    protected java.io.InputStream doDownload(String filePath) throws Exception {
        Path fullPath = baseStoragePath.resolve(filePath);

        if (!Files.exists(fullPath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        // 返回 FileInputStream
        return new FileInputStream(fullPath.toFile());
    }

    @Override
    protected void doDelete(String filePath) throws Exception {
        Path fullPath = baseStoragePath.resolve(filePath);

        // 删除文件
        boolean deleted = Files.deleteIfExists(fullPath);

        if (deleted) {
            log.debug("文件删除: 路径={}", fullPath);
        } else {
            log.warn("删除时文件未找到: 路径={}", fullPath);
        }
    }

    @Override
    protected String doGenerateUrl(String filePath, long expireSeconds) {
        // 本地文件使用 file:// 协议
        Path fullPath = baseStoragePath.resolve(filePath);
        return fullPath.toUri().toString();
    }

    @Override
    protected List<FileMetadata> doSearchFiles(String fileNamePattern) {
        // 从数据库查询（使用 MyBatis-Flex）
        QueryWrapper query = QueryWrapper.create()
                                     .select()
                                     .from(FILE_METADATA_DO);

        // 如果有文件名模式，添加模糊查询
        if (fileNamePattern != null && !fileNamePattern.isBlank()) {
            // 将通配符 * 和 ? 转换为 SQL 的 % 和 _
            String sqlPattern = fileNamePattern.replace("*", "%").replace("?", "_");
            query.where(FILE_METADATA_DO.PATH.like(sqlPattern));
        }

        List<FileMetadataDO> metadataDOs = metadataMapper.selectListByQuery(query);

        // 转换为 FileMetadata 领域对象
        return metadataDOs.stream()
                       .map(this::toFile)
                       .toList();
    }

    @Override
    protected boolean doExists(String filePath) {
        Path fullPath = baseStoragePath.resolve(filePath);
        return Files.exists(fullPath);
    }

    @Override
    protected long doGetFileSize(String filePath) throws Exception {
        Path fullPath = baseStoragePath.resolve(filePath);

        if (!Files.exists(fullPath)) {
            throw new FileNotFoundException("FileMetadata not found: " + filePath);
        }

        return Files.size(fullPath);
    }

    // ==================== 辅助方法 ====================

    /**
     * 将 FileMetadataDO 转换为 FileMetadata 领域对象
     * @param metadataDO 文件元数据DO
     * @return FileMetadata 领域对象
     */
    private FileMetadata toFile(FileMetadataDO metadataDO) {
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
                       .setContentType(FileMetadata.ContentType.fromMimeType(metadataDO.getContentType()))
                       .setFileSize(metadataDO.getSize())
                       .setStatus(Status.ACTIVE)
                       .setCreateTime(metadataDO.getCreateTime())
                       .setUpdateTime(metadataDO.getUpdateTime())
                       .build();
    }

}
