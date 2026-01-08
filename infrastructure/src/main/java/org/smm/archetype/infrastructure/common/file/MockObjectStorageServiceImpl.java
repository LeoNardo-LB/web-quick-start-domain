package org.smm.archetype.infrastructure.common.file;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.common.file.File;
import org.smm.archetype.domain.common.file.ObjectStorageService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * 对象存储服务实现（Mock版本）
 *
 * <p>实际使用时应该接入 RustFS 或其他对象存储中间件。
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Service
public class MockObjectStorageServiceImpl implements ObjectStorageService {

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        log.info("Mock upload: fileName={}, contentType={}", fileName, contentType);
        // TODO: 实际接入 RustFS
        return "/mock/path/" + fileName;
    }

    @Override
    public InputStream download(String filePath) {
        log.info("Mock download: filePath={}", filePath);
        // TODO: 实际接入 RustFS
        return InputStream.nullInputStream();
    }

    @Override
    public void delete(String filePath) {
        log.info("Mock delete: filePath={}", filePath);
        // TODO: 实际接入 RustFS
    }

    @Override
    public String generateUrl(String filePath, long expireSeconds) {
        log.info("Mock generate URL: filePath={}, expireSeconds={}", filePath, expireSeconds);
        // TODO: 实际接入 RustFS
        return "https://mock-storage.example.com" + filePath;
    }

    @Override
    public List<File> searchFiles(String fileNamePattern, File.BusinessType businessType, String businessId) {
        log.info("Mock search: fileNamePattern={}, businessType={}, businessId={}",
                fileNamePattern, businessType, businessId);
        // TODO: 实际接入 RustFS
        return Collections.emptyList();
    }

    @Override
    public boolean exists(String filePath) {
        log.info("Mock exists: filePath={}", filePath);
        // TODO: 实际接入 RustFS
        return false;
    }

    @Override
    public long getFileSize(String filePath) {
        log.info("Mock getFileSize: filePath={}", filePath);
        // TODO: 实际接入 RustFS
        return 0;
    }

}
