package org.smm.archetype.infrastructure.common.file;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.smm.archetype.domain.common.file.FileBusiness;
import org.smm.archetype.infrastructure._shared.converter.BaseDomainConverter;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.FileBusinessDO;

/**
 * 业务文件领域对象转换器
 *
 * <p>负责FileBusiness与FileBusinessDO之间的转换
 * @author Leonardo
 * @since 2026/01/10
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileBusinessConverter extends BaseDomainConverter<FileBusiness, FileBusinessDO> {

    @Override
    @Mapping(target = "fileMetadata", ignore = true)
    @Mapping(target = "order", source = "sort")
    FileBusiness toEntity(FileBusinessDO dataObject);

    @Override
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "sort", source = "order")
    FileBusinessDO toDataObject(FileBusiness entity);

}
