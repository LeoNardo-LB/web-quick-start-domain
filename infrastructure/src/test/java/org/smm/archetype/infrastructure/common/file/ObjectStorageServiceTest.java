package org.smm.archetype.infrastructure.common.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对象存储服务测试
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@ExtendWith(MockitoExtension.class)
class ObjectStorageServiceTest {

    @InjectMocks
    private MockObjectStorageServiceImpl objectStorageService;

    @Test
    void testUpload() {
        String content = "test content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        String filePath = objectStorageService.upload(inputStream, "test.txt", "text/plain");

        assertNotNull(filePath);
        assertTrue(filePath.contains("test.txt"));
    }

    @Test
    void testDownload() {
        String filePath = "/mock/path/test.txt";

        InputStream inputStream = objectStorageService.download(filePath);

        assertNotNull(inputStream);
    }

    @Test
    void testGenerateUrl() {
        String filePath = "/mock/path/test.txt";

        String url = objectStorageService.generateUrl(filePath, 3600);

        assertNotNull(url);
        assertTrue(url.contains(filePath));
    }

    @Test
    void testExists() {
        String filePath = "/mock/path/test.txt";

        boolean exists = objectStorageService.exists(filePath);

        assertFalse(exists); // Mock实现返回false
    }

}
