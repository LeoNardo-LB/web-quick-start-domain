package org.smm.archetype.infrastructure.platform.file;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.smm.archetype.domain.platform.file.FileMetadata;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.FileMetadataDO;

/**
 * 文件元数据领域对象转换器（MapStruct实现）
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
    @Mapping(target = "contentType", source = "contentType")
    @Mapping(target = "fileName", expression = "java(generateFileName(dataObject))")
    @Mapping(target = "status", expression = "java(toStatus(dataObject))")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "createTime", source = "createTime")
    @Mapping(target = "updateTime", source = "updateTime")
    @Mapping(target = "version", ignore = true)  // DO没有version字段，使用默认值
    FileMetadata toEntity(FileMetadataDO dataObject);

    /**
     * 将领域对象转换为数据对象
     * @param entity 领域对象
     * @return 数据对象
     */
    @Mapping(target = "md5", source = "md5")
    @Mapping(target = "contentType", source = "contentType")
    @Mapping(target = "size", source = "fileSize")
    @Mapping(target = "url", source = "fileUrl")
    @Mapping(target = "path", source = "filePath")
    @Mapping(target = "urlExpire", ignore = true)  // 领域类没有urlExpire字段
    @Mapping(target = "deleteTime", expression = "java(toDeleteTime(entity))")
    @Mapping(target = "deleteUser", expression = "java(toDeleteUser(entity))")
    FileMetadataDO toDataObject(FileMetadata entity);

    /**
     * 将deleteTime转换为FileMetadata.Status
     * @param dataObject 数据对象
     * @return 文件状态
     */
    @Named("toStatus")
    default org.smm.archetype.domain.platform.file.FileMetadata.Status toStatus(FileMetadataDO dataObject) {
        if (dataObject == null || dataObject.getDeleteTime() == null || dataObject.getDeleteTime() == 0) {
            return org.smm.archetype.domain.platform.file.FileMetadata.Status.ACTIVE;
        }
        return org.smm.archetype.domain.platform.file.FileMetadata.Status.DELETED;
    }

    /**
     * 将FileMetadata.Status转换为deleteTime
     * @param entity 领域对象
     * @return 删除时间
     */
    @Named("toDeleteTime")
    default Long toDeleteTime(org.smm.archetype.domain.platform.file.FileMetadata entity) {
        if (entity == null || entity.getStatus() == org.smm.archetype.domain.platform.file.FileMetadata.Status.ACTIVE) {
            return 0L;
        }
        return System.currentTimeMillis(); // 返回当前时间作为删除时间
    }

    /**
     * 获取删除用户ID（当前实现返回空）
     * @param entity 领域对象
     * @return 删除用户ID
     */
    @Named("toDeleteUser")
    default String toDeleteUser(org.smm.archetype.domain.platform.file.FileMetadata entity) {
        // 当前实现中FileMetadata不包含删除用户信息，返回空
        return null;
    }

    /**
     * 根据MD5和MIME类型生成文件名
     * 格式：md5值.扩展名
     * @param dataObject 数据对象
     * @return 文件名
     */
    @Named("generateFileName")
    default String generateFileName(FileMetadataDO dataObject) {
        if (dataObject == null || dataObject.getMd5() == null) {
            return "";
        }

        String md5 = dataObject.getMd5();
        String contentType = dataObject.getContentType();

        // 从MIME类型推断扩展名
        String extension = getExtensionFromMimeType(contentType);

        return md5 + extension;
    }

    /**
     * 从MIME类型获取文件扩展名
     * @param mimeType MIME类型
     * @return 文件扩展名（如".jpg"），如果无法识别则返回""
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Named("getExtensionFromMimeType")
    default String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return "";
        }

        String lowerMimeType = mimeType.toLowerCase();

        // 图片类型
        if (lowerMimeType.startsWith("image/jpeg") || lowerMimeType.startsWith("image/jpg")) {
            return ".jpg";
        } else if (lowerMimeType.startsWith("image/png")) {
            return ".png";
        } else if (lowerMimeType.startsWith("image/gif")) {
            return ".gif";
        } else if (lowerMimeType.startsWith("image/webp")) {
            return ".webp";
        } else if (lowerMimeType.startsWith("image/svg")) {
            return ".svg";
        } else if (lowerMimeType.startsWith("image/")) {
            return ".img"; // 其他图片类型通用扩展名
        }

        // 视频类型
        if (lowerMimeType.startsWith("video/mp4")) {
            return ".mp4";
        } else if (lowerMimeType.startsWith("video/avi")) {
            return ".avi";
        } else if (lowerMimeType.startsWith("video/quicktime")) {
            return ".mov";
        } else if (lowerMimeType.startsWith("video/")) {
            return ".video"; // 其他视频类型通用扩展名
        }

        // 音频类型
        if (lowerMimeType.startsWith("audio/mpeg")) {
            return ".mp3";
        } else if (lowerMimeType.startsWith("audio/wav")) {
            return ".wav";
        } else if (lowerMimeType.startsWith("audio/")) {
            return ".audio"; // 其他音频类型通用扩展名
        }

        // 文档类型
        if (lowerMimeType.equals("application/pdf")) {
            return ".pdf";
        } else if (lowerMimeType.contains("application/msword") || lowerMimeType.contains("wordprocessingml")) {
            return ".docx";
        } else if (lowerMimeType.contains("application/vnd.ms-excel") || lowerMimeType.contains("spreadsheetml")) {
            return ".xlsx";
        } else if (lowerMimeType.contains("application/vnd.ms-powerpoint")
                           || lowerMimeType.contains("presentationml")) {
            return ".pptx";
        } else if (lowerMimeType.startsWith("text/")) {
            return ".txt";
        }

        // 压缩文件
        if (lowerMimeType.contains("zip")) {
            return ".zip";
        } else if (lowerMimeType.contains("tar")) {
            return ".tar";
        } else if (lowerMimeType.contains("rar")) {
            return ".rar";
        } else if (lowerMimeType.contains("7z")) {
            return ".7z";
        }

        // 其他类型，返回空扩展名
        return "";
    }

}
