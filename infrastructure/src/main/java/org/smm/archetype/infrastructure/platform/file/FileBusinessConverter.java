package org.smm.archetype.infrastructure.platform.file;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smm.archetype.domain.platform.file.FileBusiness;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.FileBusinessDO;

/**
 * 业务文件领域对象转换器（MapStruct实现）
 *
负责FileBusiness与FileBusinessDO之间的转换
 *
通过@Mapper(componentModel = "spring")自动生成@Component，支持Spring依赖注入


 */
@Mapper(componentModel = "spring")
public interface FileBusinessConverter {

    /**
     * 将数据对象转换为领域对象
     * @param dataObject 数据对象
     * @return 领域对象
     */
    @Mapping(target = "order", source = "sort")
    @Mapping(target = "type", expression = "java(dataObject.getType() != null ? "
                                                   + "FileBusiness.Type.valueOf(dataObject.getType()) : null)")
    @Mapping(target = "usage", expression = "java(dataObject.getUsage() != null ? "
                                                    + "FileBusiness.Usage.valueOf(dataObject.getUsage()) : null)")
    @Mapping(target = "fileMetadata", ignore = true)  // fileMetadata需要单独查询
    @Mapping(target = "version", ignore = true)  // DO没有version字段
    FileBusiness toEntity(FileBusinessDO dataObject);

    /**
     * 将领域对象转换为数据对象
     * @param entity 领域对象
     * @return 数据对象
     */
    @InheritInverseConfiguration
    @Mapping(target = "sort", source = "order")
    @Mapping(target = "name", ignore = true)  // 领域类没有name字段
    @Mapping(target = "fileMetaId", ignore = true)  // 领域类有fileMetadata对象，不是直接存储fileMetaId
    @Mapping(target = "type", expression = "java(entity.getType() != null ? entity.getType().name() : null)")
    @Mapping(target = "usage", expression = "java(entity.getUsage() != null ? entity.getUsage().name() : null)")
    @Mapping(target = "deleteTime", ignore = true)  // FileBusiness领域类没有删除标记
    @Mapping(target = "deleteUser", ignore = true)  // FileBusiness领域类没有删除用户信息
    FileBusinessDO toDataObject(FileBusiness entity);

}
