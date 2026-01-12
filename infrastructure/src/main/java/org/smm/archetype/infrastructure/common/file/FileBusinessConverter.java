package org.smm.archetype.infrastructure.common.file;

import org.smm.archetype.domain.common.file.FileBusiness;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.FileBusinessDO;

/**
 * 业务文件领域对象转换器
 *
 * <p>负责FileBusiness与FileBusinessDO之间的转换
 * <p>通过ConverterConfigure配置类注册为Bean
 * @author Leonardo
 * @since 2026/01/10
 */
public class FileBusinessConverter {

    /**
     * 将数据对象转换为领域对象
     * @param dataObject 数据对象
     * @return 领域对象
     */
    public FileBusiness toEntity(FileBusinessDO dataObject) {
        if (dataObject == null) {
            return null;
        }

        return FileBusiness.builder()
                       .setOrder(dataObject.getSort())
                       .setId(dataObject.getId())
                       .setCreateTime(dataObject.getCreateTime())
                       .setUpdateTime(dataObject.getUpdateTime())
                       .setCreateUser(dataObject.getCreateUser())
                       .setUpdateUser(dataObject.getUpdateUser())
                       .setBusinessId(dataObject.getBusinessId())
                       .setType(dataObject.getType() != null ? FileBusiness.Type.valueOf(dataObject.getType()) : null)
                       .setUsage(dataObject.getUsage() != null ? FileBusiness.Usage.valueOf(dataObject.getUsage()) : null)
                       .setRemark(dataObject.getRemark())
                       .build();
    }

    /**
     * 将领域对象转换为数据对象
     * @param entity 领域对象
     * @return 数据对象
     */
    public FileBusinessDO toDataObject(FileBusiness entity) {
        if (entity == null) {
            return null;
        }

        return FileBusinessDO.builder()
                       .setSort(entity.getOrder())
                       .setBusinessId(entity.getBusinessId())
                       .setType(entity.getType() != null ? entity.getType().name() : null)
                       .setUsage(entity.getUsage() != null ? entity.getUsage().name() : null)
                       .setRemark(entity.getRemark())
                       .build();
    }

}
