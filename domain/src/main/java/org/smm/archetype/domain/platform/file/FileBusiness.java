package org.smm.archetype.domain.platform.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.shared.base.Entity;

/**
 * 业务关联信息
 */
@Getter
@Setter
@SuperBuilder(setterPrefix = "set")
public class FileBusiness extends Entity {

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 业务实体类型
     */
    private Type type;

    /**
     * 使用场景
     */
    private Usage usage;

    /**
     * 备注
     */
    private String remark;

    /**
     * 文件元信息
     */
    private FileMetadata fileMetadata;

    /**
     * 排序（用于多文件排序）
     */
    @Builder.Default
    private Integer order = 0;

    /**
     * 业务实体类型枚举
    定义文件可以关联的业务实体类型
     */
    @Getter
    public enum Type {

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

        /**
         * 描述
         */
        private final String desc;

        /**
         * 构造函数
         */
        Type(String desc) {
            this.desc = desc;
        }

    }

    /**
     * 使用场景枚举
     */
    @Getter
    public enum Usage {

        /**
         * 头像
         */
        AVATAR("头像", Type.USER),

        /**
         * 证件照片
         */
        ID_CARD("证件照片", Type.USER),

        /**
         * 合同文件
         */
        CONTRACT_FILE("合同文件", Type.CONTRACT),

        /**
         * 产品图片
         */
        PRODUCT_IMAGE("产品图片", Type.PRODUCT),

        /**
         * 附件
         */
        ATTACHMENT("附件", Type.OTHER),

        /**
         * 其他
         */
        OTHER("其他", Type.OTHER);

        /**
         * 描述
         */
        private final String desc;

        /**
         * 类型
         */
        private final Type type;

        /**
         * 构造函数
         */
        Usage(String desc, Type type) {
            this.desc = desc;
            this.type = type;
        }

    }

}