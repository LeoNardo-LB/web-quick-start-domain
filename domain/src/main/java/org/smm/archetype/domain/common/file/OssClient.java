package org.smm.archetype.domain.common.file;

import java.io.InputStream;

/**
 *
 *
 * @author Leonardo
 * @since 2026/1/7
 */
public interface OssClient {

    String upload(String fileId, InputStream inputStream);

    InputStream download(String fileId);

    String getUrl(String fileId);

    void delete(String fileId);

}
