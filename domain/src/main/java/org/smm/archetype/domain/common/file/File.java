package org.smm.archetype.domain.common.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain._shared.base.Entity;

import java.io.InputStream;
import java.time.Instant;

/**
 * 文件领域模型
 *
 * <p>表示上传到对象存储的文件。
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Data
@SuperBuilder(setterPrefix = "set")
@EqualsAndHashCode(callSuper = true)
public class File extends Entity {

    /**
     * 文件唯一标识（UUID）
     */
    private String fileId;

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
     * MIME类型
     */
    private String contentType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件状态
     */
    private FileStatus status;

    /**
     * 业务关联信息
     */
    private FileBusiness fileBusiness;

    /**
     * 文件状态枚举
     */
    @Getter
    @AllArgsConstructor
    public enum FileStatus {

        /**
         * 有效
         */
        ACTIVE("有效"),

        /**
         * 已删除
         */
        DELETED("已删除");

        private final String description;

    }

    /**
     * 业务关联信息
     */
    @Data
    @Builder(setterPrefix = "set")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileBusiness {

        /**
         * 业务类型
         */
        private BusinessType businessType;

        /**
         * 业务ID（关联的业务实体ID）
         */
        private String businessId;

        /**
         * 使用场景
         */
        private UsageType usageType;

        /**
         * 备注
         */
        private String remark;

        /**
         * 排序（用于多文件排序）
         */
        private Integer order = 0;

    }

    /**
     * 业务类型枚举
     */
    @Getter
    @AllArgsConstructor
    public enum BusinessType {

        /**
         * 订单相关
         */
        ORDER("订单"),

        /**
         * 用户相关
         */
        USER("用户"),

        /**
         * 产品相关
         */
        PRODUCT("产品"),

        /**
         * 合同相关
         */
        CONTRACT("合同"),

        /**
         * 其他
         */
        OTHER("其他");

        private final String description;

    }

    /**
     * 使用场景枚举
     */
    @Getter
    @AllArgsConstructor
    public enum UsageType {

        /**
         * 头像
         */
        AVATAR("头像"),

        /**
         * 证件照片
         */
        ID_CARD("证件照片"),

        /**
         * 合同文件
         */
        CONTRACT_FILE("合同文件"),

        /**
         * 产品图片
         */
        PRODUCT_IMAGE("产品图片"),

        /**
         * 附件
         */
        ATTACHMENT("附件"),

        /**
         * 其他
         */
        OTHER("其他");

        private final String description;

    }

    /**
     * 判断文件是否有效
     * @return true-有效，false-已删除
     */
    public boolean isActive() {
        return status == FileStatus.ACTIVE;
    }

    /**
     * 判断文件是否已删除
     * @return true-已删除，false-未删除
     */
    public boolean isDeleted() {
        return status == FileStatus.DELETED;
    }

    /**
     * 标记文件为已删除
     */
    public void markAsDeleted() {
        this.status = FileStatus.DELETED;
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
        String ext = getExtension().toLowerCase();
        return ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png")
                || ext.equals(".gif") || ext.equals(".bmp") || ext.equals(".webp");
    }

    /**
     * 判断是否为PDF
     * @return true-是PDF，false-不是PDF
     */
    public boolean isPdf() {
        return getExtension().toLowerCase().equals(".pdf");
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

}
