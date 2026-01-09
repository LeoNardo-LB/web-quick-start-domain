package org.smm.archetype.infrastructure.common.file.converter;

import org.mapstruct.Mapper;
import org.smm.archetype.domain.common.file.File;
import org.smm.archetype.infrastructure._shared.generated.entity.FileBusinessDO;
import org.smm.archetype.infrastructure._shared.generated.entity.FileMetadataDO;

/**
 * 文件领域对象转换器
 *
 * <p>负责 File 领域对象与数据对象之间的转换。
 *
 * <p>由于 File 领域对象同时依赖 FileMetadataDO 和 FileBusinessDO 两个数据对象，
 * 因此不继承 BaseDomainConverter，而是提供独立的转换方法。
 * @author Leonardo
 * @since 2026/1/10
 */
@Mapper(componentModel = "spring")
public interface FileConverter {

    /**
     * 将 FileMetadataDO + FileBusinessDO 转换为 File 领域对象
     * @param metadataDO 文件元数据DO
     * @param businessDO 文件业务DO
     * @return File 领域对象
     */
    default File toFile(FileMetadataDO metadataDO, FileBusinessDO businessDO) {
        if (metadataDO == null) {
            return null;
        }

        File.FileBuilder fileBuilder = File.builder()
                                               .setFileId(metadataDO.getFileId())
                                               .setFilePath(metadataDO.getPath())
                                               .setFileUrl(metadataDO.getUrl())
                                               .setMd5(metadataDO.getMd5())
                                               .setContentType(metadataDO.getContentType())
                                               .setFileSize(metadataDO.getSize())
                                               .setStatus(File.FileStatus.ACTIVE)
                                               .setCreateTime(metadataDO.getCreateTime())
                                               .setUpdateTime(metadataDO.getUpdateTime());

        // 如果有业务关联信息，则设置
        if (businessDO != null) {
            fileBuilder.setFileName(businessDO.getName())
                    .setFileBusiness(File.FileBusiness.builder()
                                             .setBusinessEntityType(toBusinessEntityType(businessDO.getType()))
                                             .setBusinessId(businessDO.getBusinessId())
                                             .setUsageType(toUsageType(businessDO.getUsage()))
                                             .setRemark(businessDO.getRemark())
                                             .setOrder(businessDO.getSort())
                                             .build());
        }

        return fileBuilder.build();
    }

    /**
     * 将 FileMetadataDO 转换为 File 领域对象（不含业务信息）
     * @param metadataDO 文件元数据DO
     * @return File 领域对象
     */
    default File toFile(FileMetadataDO metadataDO) {
        return toFile(metadataDO, null);
    }

    /**
     * 将 File 领域对象转换为 FileMetadataDO
     * @param file File 领域对象
     * @return FileMetadataDO
     */
    default FileMetadataDO toFileMetadataDO(File file) {
        if (file == null) {
            return null;
        }

        return FileMetadataDO.builder()
                       .fileId(file.getFileId())
                       .md5(file.getMd5())
                       .contentType(file.getContentType())
                       .size(file.getFileSize())
                       .url(file.getFileUrl())
                       .path(file.getFilePath())
                       .build();
    }

    /**
     * 将 File 领域对象转换为 FileBusinessDO
     * @param file File 领域对象
     * @return FileBusinessDO
     */
    default FileBusinessDO toFileBusinessDO(File file) {
        if (file == null) {
            return null;
        }

        File.FileBusiness business = file.getFileBusiness();
        if (business == null) {
            return null;
        }

        return FileBusinessDO.builder()
                       .fileId(file.getFileId())
                       .name(file.getFileName())
                       .type(business.getBusinessEntityType().name())
                       .businessId(business.getBusinessId())
                       .usage(business.getUsageType().name())
                       .remark(business.getRemark())
                       .sort(business.getOrder())
                       .build();
    }

    /**
     * 将字符串转换为 FileBusinessEntityType 枚举
     * @param type 业务类型字符串
     * @return FileBusinessEntityType 枚举
     */
    default File.FileBusinessEntityType toBusinessEntityType(String type) {
        if (type == null || type.isBlank()) {
            return File.FileBusinessEntityType.OTHER;
        }

        try {
            return File.FileBusinessEntityType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return File.FileBusinessEntityType.OTHER;
        }
    }

    /**
     * 将字符串转换为 UsageType 枚举
     * @param usage 使用场景字符串
     * @return UsageType 枚举
     */
    default File.UsageType toUsageType(String usage) {
        if (usage == null || usage.isBlank()) {
            return File.UsageType.OTHER;
        }

        try {
            return File.UsageType.valueOf(usage);
        } catch (IllegalArgumentException e) {
            return File.UsageType.OTHER;
        }
    }

}
