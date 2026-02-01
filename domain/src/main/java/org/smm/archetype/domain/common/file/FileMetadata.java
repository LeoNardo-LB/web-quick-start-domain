package org.smm.archetype.domain.common.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smm.archetype.domain.bizshared.base.Entity;

/**
 * 文件领域模型，表示上传到对象存储的文件。
 */
@Getter
@Setter
@SuperBuilder(setterPrefix = "set")
@EqualsAndHashCode(callSuper = true)
public class FileMetadata extends Entity {

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 对象存储路径
     */
    private String filePath;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件MD5
     */
    private String md5;

    /**
     * 文件内容类型
     */
    private ContentType contentType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件状态
     */
    private Status status;

    /**
     * 判断文件是否有效
     * @return true-有效，false-已删除
     */
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    /**
     * 判断文件是否已删除
     * @return true-已删除，false-未删除
     */
    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    /**
     * 标记文件为已删除
     */
    public void markAsDeleted() {
        this.status = Status.DELETED;
        this.markAsUpdated();
    }

    /**
     * 获取文件扩展名
     * @return 文件扩展名（如".jpg"）
     */
    public String getExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 判断是否为图片
     * @return true-是图片，false-不是图片
     */
    public boolean isImage() {
        return contentType == ContentType.IMAGE;
    }

    /**
     * 判断是否为文档
     * @return true-是文档，false-不是文档
     */
    public boolean isDocument() {
        return contentType == ContentType.DOCUMENT;
    }

    /**
     * 判断是否为视频
     * @return true-是视频，false-不是视频
     */
    public boolean isVideo() {
        return contentType == ContentType.VIDEO;
    }

    /**
     * 判断内容类型是否未知
     * @return true-未知，false-已知
     */
    public boolean isContentTypeUnknown() {
        return contentType == ContentType.UNKNOWN;
    }

    /**
     * 判断是否为PDF
     * @return true-是PDF，false-不是PDF
     */
    public boolean isPdf() {
        return getExtension().equalsIgnoreCase(".pdf");
    }

    /**
     * 格式化文件大小
     * @return 格式化后的文件大小（如"1.5 MB"）
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "0 B";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 文件内容类型枚举
     *
     * <p>定义系统支持的文件内容类型，用于分类和管理
     */
    @Getter
    public enum ContentType {

        /**
         * 图片类型
         */
        IMAGE("image", "图片"),

        /**
         * 文档类型
         */
        DOCUMENT("document", "文档"),

        /**
         * 视频类型
         */
        VIDEO("video", "视频"),

        /**
         * 音频类型
         */
        AUDIO("audio", "音频"),

        /**
         * 压缩文件类型
         */
        ARCHIVE("archive", "压缩文件"),

        /**
         * 其他类型
         */
        OTHER("other", "其他"),

        /**
         * 未知类型
         */
        UNKNOWN("unknown", "未知");

        private static final Logger log = LoggerFactory.getLogger(ContentType.class);
        private final        String code;
        private final        String description;

        ContentType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        /**
         * 从 MIME 类型转换为内容类型
         *
         * <p>根据 MIME 类型判断文件内容类型，如果无法识别返回 UNKNOWN
         * @param mimeType MIME 类型
         * @return 内容类型枚举
         */
        public static ContentType fromMimeType(String mimeType) {
            if (mimeType == null || mimeType.trim().isEmpty()) {
                log.warn("Empty mimeType, using default: UNKNOWN");
                return UNKNOWN;
            }

            String lowerMimeType = mimeType.toLowerCase();

            if (lowerMimeType.startsWith("image/")) {
                return IMAGE;
            } else if (lowerMimeType.startsWith("video/")) {
                return VIDEO;
            } else if (lowerMimeType.startsWith("audio/")) {
                return AUDIO;
            } else if (lowerMimeType.contains("application/pdf")
                               || lowerMimeType.contains("application/msword")
                               || lowerMimeType.contains("application/vnd")
                               || lowerMimeType.contains("text/")) {
                return DOCUMENT;
            } else if (lowerMimeType.contains("zip")
                               || lowerMimeType.contains("tar")
                               || lowerMimeType.contains("rar")
                               || lowerMimeType.contains("7z")) {
                return ARCHIVE;
            } else {
                log.warn("Unknown mimeType: {}, using default: UNKNOWN", mimeType);
                return UNKNOWN;
            }
        }

        /**
         * 从字符串转换为内容类型
         *
         * <p>如果转换失败，返回 UNKNOWN
         * @param value 类型字符串
         * @return 内容类型枚举
         */
        public static ContentType fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                log.warn("Empty ContentType value, using default: UNKNOWN");
                return UNKNOWN;
            }

            try {
                return ContentType.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid ContentType: {}, using default: UNKNOWN", value);
                return UNKNOWN;
            }
        }

        /**
         * 从文件扩展名判断内容类型
         *
         * <p>根据文件扩展名判断文件内容类型
         * @param extension 文件扩展名（包含点号，如 ".jpg"）
         * @return 内容类型枚举
         */
        public static ContentType fromExtension(String extension) {
            if (extension == null || extension.trim().isEmpty()) {
                log.warn("Empty file extension, using default: UNKNOWN");
                return UNKNOWN;
            }

            String lowerExt = extension.toLowerCase();

            // 图片类型
            if (lowerExt.matches("\\.(jpg|jpeg|png|gif|bmp|webp|svg)$")) {
                return IMAGE;
            }

            // 视频类型
            if (lowerExt.matches("\\.(mp4|avi|mkv|mov|wmv|flv|webm)$")) {
                return VIDEO;
            }

            // 音频类型
            if (lowerExt.matches("\\.(mp3|wav|flac|aac|ogg|wma)$")) {
                return AUDIO;
            }

            // 文档类型
            if (lowerExt.matches("\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|md)$")) {
                return DOCUMENT;
            }

            // 压缩文件类型
            if (lowerExt.matches("\\.(zip|rar|7z|tar|gz|bz2)$")) {
                return ARCHIVE;
            }

            log.warn("Unknown file extension: {}, using default: UNKNOWN", extension);
            return UNKNOWN;
        }

        /**
         * 获取 MIME 类型字符串
         *
         * <p>用于持久化到数据库或传递给外部系统
         * @return MIME 类型字符串或 code 值
         */
        public String toMimeType() {
            return this.code;
        }
    }

    /**
     * 文件状态枚举
     */
    @Getter
    public enum Status {

        /**
         * 有效
         */
        ACTIVE("有效"),

        /**
         * 已删除
         */
        DELETED("已删除");

        /**
         * 描述
         */
        private final String desc;

        /**
         * 构造函数
         */
        Status(String desc) {
            this.desc = desc;
        }

    }



}
