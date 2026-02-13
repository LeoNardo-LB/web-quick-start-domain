package org.smm.archetype.domain.platform.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.shared.base.Entity;

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
     * 文件内容类型（MIME类型）
     */
    private String contentType;

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
        return contentType != null && contentType.toLowerCase().startsWith("image/");
    }

    /**
     * 判断是否为文档
     * @return true-是文档，false-不是文档
     */
    public boolean isDocument() {
        if (contentType == null) {
            return false;
        }
        String lowerContentType = contentType.toLowerCase();
        return lowerContentType.contains("application/pdf")
                       || lowerContentType.contains("application/msword")
                       || lowerContentType.contains("application/vnd")
                       || lowerContentType.startsWith("text/");
    }

    /**
     * 判断是否为视频
     * @return true-是视频，false-不是视频
     */
    public boolean isVideo() {
        return contentType != null && contentType.toLowerCase().startsWith("video/");
    }

    /**
     * 判断内容类型是否未知
     * @return true-未知，false-已知
     */
    public boolean isContentTypeUnknown() {
        return contentType == null || contentType.trim().isEmpty();
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
