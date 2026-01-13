package org.smm.archetype.infrastructure.common.file;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smm.archetype.domain.common.file.FileBusiness;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.FileBusinessDO;

/**
 * 业务文件领域对象转换器（MapStruct实现）
 *
 * <p>负责FileBusiness与FileBusinessDO之间的转换
 *
 * <p>通过@Mapper(componentModel = "spring")自动生成@Component，支持Spring依赖注入
 * @author Leonardo
 * @since 2026/01/10
 */
@Mapper(componentModel = "spring")
public interface FileBusinessConverter {

    /**
     * 将数据对象转换为领域对象
     * @param dataObject 数据对象
     * @return 领域对象
     */
    @Mapping(target = "order", source = "sort")
    @Mapping(target = "type", expression = "java(dataObject.getType() != null ? FileBusiness.Type.valueOf(dataObject.getType()) : null)")
    @Mapping(target = "usage",
            expression = "java(dataObject.getUsage() != null ? FileBusiness.Usage.valueOf(dataObject.getUsage()) : null)")
    FileBusiness toEntity(FileBusinessDO dataObject);

    /**
     * 将领域对象转换为数据对象
     * @param entity 领域对象
     * @return 数据对象
     */
    @InheritInverseConfiguration
    @Mapping(target = "sort", source = "order")
    @Mapping(target = "type", expression = "java(entity.getType() != null ? entity.getType().name() : null)")
    @Mapping(target = "usage", expression = "java(entity.getUsage() != null ? entity.getUsage().name() : null)")
    FileBusinessDO toDataObject(FileBusiness entity);

}
