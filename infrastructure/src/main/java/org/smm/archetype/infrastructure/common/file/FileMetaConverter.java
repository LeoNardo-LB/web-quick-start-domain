package org.smm.archetype.infrastructure.common.file;

import org.smm.archetype.domain.common.file.FileMetadata;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.FileMetadataDO;
import org.springframework.stereotype.Component;

/**
 * 文件元数据领域对象转换器
 *
 * <p>负责FileMeta与FileMetadataDO之间的转换
 * @author Leonardo
 * @since 2026/01/10
 */
@Component
public class FileMetaConverter {

    /**
     * 将数据对象转换为领域对象
     * @param dataObject 数据对象
     * @return 领域对象
     */
    public FileMetadata toEntity(FileMetadataDO dataObject) {
        if (dataObject == null) {
            return null;
        }

        return FileMetadata.builder()
                       .setId(dataObject.getId())
                       .setCreateTime(dataObject.getCreateTime())
                       .setUpdateTime(dataObject.getUpdateTime())
                       .setCreateUser(dataObject.getCreateUser())
                       .setUpdateUser(dataObject.getUpdateUser())
                       // fileName is ignored (not mapped from DO)
                       .setFilePath(dataObject.getPath())
                       .setFileUrl(dataObject.getUrl())
                       .setFileSize(dataObject.getSize())
                       .setMd5(dataObject.getMd5())
                       .setContentType(dataObject.getContentType())
                       // status is not in DO, will use default
                       .build();
    }

    /**
     * 将领域对象转换为数据对象
     * @param entity 领域对象
     * @return 数据对象
     */
    public FileMetadataDO toDataObject(FileMetadata entity) {
        if (entity == null) {
            return null;
        }

        FileMetadataDO fileMetadataDO = FileMetadataDO.builder()
                                                .setSize(entity.getFileSize())
                                                .setUrl(entity.getFileUrl())
                                                .setPath(entity.getFilePath())
                                                // fileName is ignored (not mapped to DO)
                                                .build();

        // BaseDO fields need to be set via setters (not in builder)
        fileMetadataDO.setId(entity.getId());
        fileMetadataDO.setCreateTime(entity.getCreateTime());
        fileMetadataDO.setUpdateTime(entity.getUpdateTime());
        fileMetadataDO.setCreateUser(entity.getCreateUser());
        fileMetadataDO.setUpdateUser(entity.getUpdateUser());

        return fileMetadataDO;
    }

}
