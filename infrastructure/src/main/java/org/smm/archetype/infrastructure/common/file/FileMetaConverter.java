package org.smm.archetype.infrastructure.common.file;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smm.archetype.domain.common.file.FileMetadata;
import org.smm.archetype.infrastructure.bizshared.dal.generated.entity.FileMetadataDO;

/**
 * 文件元数据领域对象转换器（MapStruct实现）
 *
负责FileMetadata与FileMetadataDO之间的转换
 *
通过@Mapper(componentModel = "spring")自动生成@Component，支持Spring依赖注入


 */
@Mapper(componentModel = "spring")
public interface FileMetaConverter {

    /**
     * 将数据对象转换为领域对象
     * @param dataObject 数据对象
     * @return 领域对象
     */
    @Mapping(target = "filePath", source = "path")
    @Mapping(target = "fileUrl", source = "url")
    @Mapping(target = "fileSize", source = "size")
    @Mapping(target = "contentType", expression = "java(FileMetadata.ContentType.fromMimeType(dataObject.getContentType()))")
    @Mapping(target = "fileName", source = "dataObject.md5")  // 使用md5作为fileName（占位符）
    @Mapping(target = "status", source = "dataObject.contentType")  // 使用contentType作为status（占位符）
    @Mapping(target = "version", ignore = true)
    FileMetadata toEntity(FileMetadataDO dataObject);

    /**
     * 将领域对象转换为数据对象
     * @param entity 领域对象
     * @return 数据对象
     */
    @InheritInverseConfiguration
    @Mapping(target = "contentType", expression = "java(entity.getContentType() != null ? entity.getContentType().toMimeType() : null)")
    @Mapping(target = "urlExpire", ignore = true)
    FileMetadataDO toDataObject(FileMetadata entity);

    /**
     * 自定义转换方法：设置默认的fileName和status
     */
    default FileMetadata updateMetadataFields(FileMetadata metadata, FileMetadataDO dataObject) {
        if (metadata == null) {
            return null;
        }
        // 使用默认值
        if (metadata.getFileName() == null) {
            metadata.setFileName(dataObject.getMd5());
        }
        if (metadata.getStatus() == null) {
            metadata.setStatus(FileMetadata.Status.ACTIVE);
        }
        return metadata;
    }

}
