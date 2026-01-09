package org.smm.archetype.domain.common.file;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件内容类型枚举
 *
 * <p>定义系统中支持的文件内容类型。
 * @author Leonardo
 * @since 2026/01/09
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
    private final String code;
    private final String description;

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
}
