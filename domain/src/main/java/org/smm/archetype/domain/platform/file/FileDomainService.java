package org.smm.archetype.domain.platform.file;

import org.smm.archetype.domain.platform.file.FileBusiness.Type;
import org.smm.archetype.domain.platform.file.FileBusiness.Usage;

import java.io.InputStream;
import java.util.List;

/**
 * 通用文件服务，整合对象存储客户端和文件领域对象。
 */
public interface FileDomainService {

    /**
     * 上传文件
     * @param inputStream 文件输入流
     * @param fileMetadata    文件元信息
     */
    void uploadFile(InputStream inputStream, FileMetadata fileMetadata, FileBusiness fileBusiness);

    /**
     * 列出业务文件
     * @param businessId 业务ID
     * @param type       业务实体类型
     * @param usage      使用场景
     * @return 文件列表
     */
    List<FileBusiness> listFileBusinesss(String businessId, Type type, Usage usage);

    /**
     * 获取业务文件
     * @param id 文件ID
     */
    FileBusiness getFileBusiness(String id);

    /**
     * 获取文件
     * @param id 元文件ID
     * @return 元文件
     */
    FileMetadata getFileMeta(String id);

    /**
     * 获取文件URL
     * @param fileMetaId 文件元信息ID
     * @return 文件URL
     */
    String getFileUrl(String fileMetaId);

}
