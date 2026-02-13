package org.smm.archetype.infrastructure.shared.client.oss;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.FileMetadataMapper;

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

/**
 * 本地文件系统OSS实现，支持零拷贝和日期分层存储。
 *
 * <p>职责划分：
 * <ul>
 *   <li>外部能力：本地文件系统操作（上传、下载、删除等）</li>
 *   <li>数据库操作：委托给父类 AbstractOssClient 处理</li>
 * </ul>
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

    // ==================== 外部能力实现（子类职责） ====================

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

}
