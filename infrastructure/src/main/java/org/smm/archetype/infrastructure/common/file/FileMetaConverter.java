package org.smm.archetype.infrastructure.common.file;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.smm.archetype.domain.common.file.FileMetadata;
import org.smm.archetype.infrastructure._shared.converter.BaseDomainConverter;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.FileMetadataDO;

/**
 * 文件元数据领域对象转换器
 *
 * <p>负责FileMeta与FileMetadataDO之间的转换
 * @author Leonardo
 * @since 2026/01/10
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileMetaConverter extends BaseDomainConverter<FileMetadata, FileMetadataDO> {

    @Override
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "filePath", source = "path")
    @Mapping(target = "fileUrl", source = "url")
    @Mapping(target = "fileSize", source = "size")
    FileMetadata toEntity(FileMetadataDO dataObject);

    @Override
    @Mapping(target = "size", source = "fileSize")
    @Mapping(target = "url", source = "fileUrl")
    @Mapping(target = "path", source = "filePath")
    FileMetadataDO toDataObject(FileMetadata entity);

}
