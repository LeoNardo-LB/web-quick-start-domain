package org.smm.archetype.infrastructure.bizshared.client.oss.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.client.OssClient;
import org.smm.archetype.domain.common.file.FileMetadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Mock对象存储客户端实现,模拟OSS文件操作。
 * 用于测试和开发环境,避免依赖对象存储中间件。
 */
@Slf4j
public class MockOssClientImpl implements OssClient {

    /**
     * 文件数据存储
     */
    private final Map<String, byte[]> fileData = new HashMap<>();

    /**
     * 文件元数据存储
     */
    private final Map<String, FileMetadata> fileMetadataMap = new HashMap<>();

    /**
     * 文件ID生成器
     */
    private final AtomicLong fileIdGenerator = new AtomicLong(1);

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        log.debug("Mock OSS上传: fileName={}, contentType={}", fileName, contentType);

        try {
            byte[] bytes = inputStream.readAllBytes();
            String filePath = generateFilePath(fileName);

            // 存储文件数据
            fileData.put(filePath, bytes);

            // 创建文件元数据
            FileMetadata metadata = FileMetadata.builder()
                .setFileName(fileName)
                .setFilePath(filePath)
                .setFileUrl(generateMockUrl(filePath))
                .setMd5(calculateMd5(bytes))
                .setContentType(FileMetadata.ContentType.fromMimeType(contentType))
                .setFileSize((long) bytes.length)
                .setStatus(FileMetadata.Status.ACTIVE)
                .build();
            fileMetadataMap.put(filePath, metadata);

            log.debug("Mock OSS上传成功: filePath={}, size={} bytes", filePath, bytes.length);
            return filePath;
        } catch (IOException e) {
            log.error("Mock OSS上传失败: fileName={}", fileName, e);
            throw new RuntimeException("Mock OSS上传失败", e);
        }
    }

    @Override
    public InputStream download(String filePath) {
        log.debug("Mock OSS下载: filePath={}", filePath);

        byte[] data = fileData.get(filePath);
        if (data == null) {
            log.warn("Mock OSS下载失败: 文件不存在, filePath={}", filePath);
            return null;
        }

        log.debug("Mock OSS下载成功: filePath={}, size={} bytes", filePath, data.length);
        return new ByteArrayInputStream(data);
    }

    @Override
    public void delete(String filePath) {
        log.debug("Mock OSS删除: filePath={}", filePath);

        byte[] removed = fileData.remove(filePath);
        fileMetadataMap.remove(filePath);

        if (removed != null) {
            log.debug("Mock OSS删除成功: filePath={}, size={} bytes", filePath, removed.length);
        } else {
            log.warn("Mock OSS删除失败: 文件不存在, filePath={}", filePath);
        }
    }

    @Override
    public String generateUrl(String filePath, long expireSeconds) {
        log.debug("Mock OSS生成URL: filePath={}, expireSeconds={}", filePath, expireSeconds);

        String url = generateMockUrl(filePath);
        if (expireSeconds > 0) {
            url += "?expires=" + expireSeconds;
        }

        log.debug("Mock OSS生成URL成功: url={}", url);
        return url;
    }

    @Override
    public List<FileMetadata> searchFiles(String fileNamePattern) {
        log.debug("Mock OSS模糊查询: pattern={}", fileNamePattern);

        String lowerPattern = fileNamePattern.toLowerCase().replace("*", ".*").replace("?", ".");

        List<FileMetadata> results = fileMetadataMap.values().stream()
            .filter(metadata -> metadata.getFileName().toLowerCase().matches(lowerPattern))
            .collect(Collectors.toList());

        log.debug("Mock OSS模糊查询成功: pattern={}, count={}", fileNamePattern, results.size());
        return results;
    }

    @Override
    public boolean exists(String filePath) {
        log.debug("Mock OSS检查文件存在: filePath={}", filePath);

        boolean exists = fileData.containsKey(filePath);
        log.debug("Mock OSS检查文件存在结果: filePath={}, exists={}", filePath, exists);
        return exists;
    }

    @Override
    public long getFileSize(String filePath) {
        log.debug("Mock OSS获取文件大小: filePath={}", filePath);

        byte[] data = fileData.get(filePath);
        if (data == null) {
            log.warn("Mock OSS获取文件大小失败: 文件不存在, filePath={}", filePath);
            return 0;
        }

        log.debug("Mock OSS获取文件大小成功: filePath={}, size={} bytes", filePath, data.length);
        return data.length;
    }

    /**
     * 生成模拟文件路径
     */
    private String generateFilePath(String fileName) {
        long fileId = fileIdGenerator.getAndIncrement();
        return String.format("/mock/oss/files/%d/%s", fileId, fileName);
    }

    /**
     * 生成模拟访问URL
     */
    private String generateMockUrl(String filePath) {
        return "http://mock.oss.example.com" + filePath;
    }

    /**
     * 计算简单的MD5模拟值
     */
    private String calculateMd5(byte[] data) {
        // 简单模拟MD5，实际项目中应使用MessageDigest
        return String.format("%08x", data.length);
    }
}
