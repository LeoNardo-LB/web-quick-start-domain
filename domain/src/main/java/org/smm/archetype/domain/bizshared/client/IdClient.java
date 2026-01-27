package org.smm.archetype.domain.bizshared.client;

/**
 * ID 生成服务接口
 *
 * <p>定义分布式 ID 生成的契约，支持多种 ID 类型和生成策略。
 * @author Leonardo
 * @since 2026/1/7
 */
public interface IdClient {

    /**
     * 生成 ID
     * @param type ID 类型
     * @return ID 字符串
     */
    String generateId(Type type);

    /**
     * ID 类型枚举
     *
     * <p>定义系统中各类业务实体的 ID 类型，用于区分不同 ID 的来源或用途。
     */
    enum Type {

        /**
         * 通用类型
         */
        GENERAL,

        /**
         * 用户类型
         */
        USER,

        /**
         * 订单类型
         */
        ORDER,

        /**
         * 产品类型
         */
        PRODUCT,

        /**
         * 支付类型
         */
        PAYMENT,

        /**
         * 文件类型
         */
        FILE,

        /**
         * 事件类型
         */
        EVENT

    }

}
