package org.smm.archetype.domain.platform.file;

import java.util.List;
import java.util.Optional;

/**
 * 通用文件仓储接口，负责文件业务对象和元数据的持久化。
 */
public interface FileRepository {

    /**
     * 保存业务文件（包含元数据）
     * @param fileBusiness 业务文件
     * @return 保存后的业务文件
     */
    FileBusiness save(FileBusiness fileBusiness);

    /**
     * 根据ID查询业务文件
     * @param id 文件ID
     * @return 业务文件
     */
    Optional<FileBusiness> findById(String id);

    /**
     * 根据业务ID、类型和场景查询文件列表
     * @param businessId 业务ID
     * @param type       业务实体类型
     * @param usage      使用场景
     * @return 业务文件列表
     */
    List<FileBusiness> findByBusinessIdAndTypeAndUsage(
            String businessId, FileBusiness.Type type, FileBusiness.Usage usage);

    /**
     * 根据ID查询文件元数据
     * @param id 文件元数据ID（继承自Entity的id字段）
     * @return 文件元数据
     */
    Optional<FileMetadata> findFileMetaByFileId(String id);

    /**
     * 删除业务文件
     * @param id 文件ID
     */
    void deleteById(String id);

}
