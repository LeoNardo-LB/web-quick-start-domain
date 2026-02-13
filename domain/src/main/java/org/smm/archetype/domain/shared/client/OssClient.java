package org.smm.archetype.domain.shared.client;

import org.smm.archetype.domain.platform.file.FileMetadata;

import java.io.InputStream;
import java.util.List;

/**
 * 对象存储服务接口，提供文件上传下载操作。
 */
public interface OssClient {

    /**
     * 上传文件
     * @param inputStream 文件流
     * @param fileName    文件名
     * @param contentType MIME类型
     * @return 文件路径
     */
    String upload(InputStream inputStream, String fileName, String contentType);

    /**
     * 下载文件
     * @param filePath 文件路径
     * @return 文件流
     */
    InputStream download(String filePath);

    /**
     * 删除文件
     * @param filePath 文件路径
     */
    void delete(String filePath);

    /**
     * 生成访问URL
     * @param filePath      文件路径
     * @param expireSeconds 过期时间（秒），0表示永久有效
     * @return 访问URL
     */
    String generateUrl(String filePath, long expireSeconds);

    /**
     * 模糊查询文件
     * @param fileNamePattern 文件名模式（支持通配符）
     * @return 文件列表
     */
    List<FileMetadata> searchFiles(String fileNamePattern);

    /**
     * 检查文件是否存在
     * @param filePath 文件路径
     * @return true-存在，false-不存在
     */
    boolean exists(String filePath);

    /**
     * 获取文件大小
     * @param filePath 文件路径
     * @return 文件大小（字节）
     */
    long getFileSize(String filePath);

}
