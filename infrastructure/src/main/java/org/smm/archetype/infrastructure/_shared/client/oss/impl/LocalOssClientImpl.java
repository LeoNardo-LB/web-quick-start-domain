package org.smm.archetype.infrastructure._shared.client.oss.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.IdClient;
import org.smm.archetype.domain.common.file.FileMetadata;
import org.smm.archetype.domain.common.file.FileMetadata.Status;
import org.smm.archetype.infrastructure._shared.client.oss.AbstractOssClient;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.FileMetadataDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.FileMetadataMapper;

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

import static org.smm.archetype.infrastructure._shared.generated.repository.entity.table.FileMetadataDOTableDef.FILE_METADATA_DO;

/**
 * 本地文件系统对象存储服务实现
 *
 * <p>作为默认兜底实现，使用本地文件系统存储文件。
 *
 * <h3>特性</h3>
 * <ul>
 *   <li>零拷贝上传/下载（使用 NIO FileChannel.transferTo）</li>
 *   <li>顺序存储（按日期分层：yyyy/MM）</li>
 *   <li>总是可用，不依赖外部服务</li>
 * </ul>
 *
 * <h3>存储路径</h3>
 * <pre>
 * 用户文件夹/.project/${spring.application.name}/oss/
 * ├── 2026/
 * │   ├── 01/
 * │   │   ├── file123-test1.txt
 * │   │   └── file456-test2.jpg
 * │   └── 02/
 * │       └── file789-test3.pdf
 * </pre>
 * @author Leonardo
 * @since 2026/1/10
 */
@Slf4j
public class LocalOssClientImpl extends AbstractOssClient {

    private final Path baseStoragePath;
    private final boolean zeroCopy;

    /**
     * 构造函数
     * @param basePath       基础存储路径（可选，默认：用户文件夹/.project/${spring.application.name}/oss）
     * @param zeroCopy       是否使用零拷贝（NIO transferTo/transferFrom）
     * @param metadataMapper 文件元数据 Mapper
     * @param idClient       ID 生成服务
     * @throws IOException 如果创建存储目录失败
     */
    public LocalOssClientImpl(String basePath,
                              boolean zeroCopy,
                              FileMetadataMapper metadataMapper,
                              IdClient idClient) throws IOException {
        super(metadataMapper, idClient);
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
        log.info("Local object storage initialized: basePath={}, zeroCopy={}",
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
                    log.debug("Zero-copy upload completed: size={}, path={}", size, filePath);
                }
            } finally {
                // 删除临时文件
                Files.deleteIfExists(tempFile);
            }
        } else {
            // 传统拷贝方式
            Files.write(filePath, contentBytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            log.debug("Traditional upload completed: path={}", filePath);
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
            log.debug("File deleted: path={}", fullPath);
        } else {
            log.warn("File not found for deletion: path={}", fullPath);
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
