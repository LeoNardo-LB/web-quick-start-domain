package org.smm.archetype.domain.exampleorder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.smm.archetype.domain.shared.exception.ErrorCode;

/**
 * 订单模块错误码枚举
 *
 * <p>定义订单相关的所有业务错误码。</p>
 *
 * <p>错误码命名规范：ORDER-xxx</p>
 */
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    // ========== 订单基础错误 ==========

    /**
     * 订单不存在
     */
    ORDER_NOT_FOUND("ORDER-001", "订单不存在"),

    /**
     * 订单项不能为空
     */
    ORDER_ITEMS_EMPTY("ORDER-002", "订单项不能为空"),

    /**
     * 订单项无效
     */
    ORDER_ITEM_INVALID("ORDER-003", "订单项无效"),

    // ========== 订单状态错误 ==========

    /**
     * 订单状态不允许此操作
     */
    ORDER_STATUS_INVALID("ORDER-010", "当前订单状态不允许此操作"),

    /**
     * 订单已支付
     */
    ORDER_ALREADY_PAID("ORDER-011", "订单已支付"),

    /**
     * 订单已取消
     */
    ORDER_ALREADY_CANCELLED("ORDER-012", "订单已取消"),

    /**
     * 订单未支付
     */
    ORDER_NOT_PAID("ORDER-013", "订单未支付"),

    // ========== 订单项字段验证错误 ==========

    /**
     * 商品ID不能为空
     */
    PRODUCT_ID_EMPTY("ORDER-020", "商品ID不能为空"),

    /**
     * 商品名称不能为空
     */
    PRODUCT_NAME_EMPTY("ORDER-021", "商品名称不能为空"),

    /**
     * SKU编码不能为空
     */
    SKU_CODE_EMPTY("ORDER-022", "SKU编码不能为空"),

    /**
     * 单价必须大于0
     */
    UNIT_PRICE_INVALID("ORDER-023", "单价必须大于0"),

    /**
     * 数量必须大于0
     */
    QUANTITY_INVALID("ORDER-024", "数量必须大于0"),

    // ========== 金额错误 ==========

    /**
     * 金额无效
     */
    MONEY_INVALID("ORDER-030", "金额无效"),

    /**
     * 币种不一致
     */
    CURRENCY_MISMATCH("ORDER-031", "币种不一致"),

    // ========== 退款错误 ==========

    /**
     * 退款金额无效
     */
    REFUND_AMOUNT_INVALID("ORDER-040", "退款金额无效"),

    /**
     * 退款金额超过剩余可退金额
     */
    REFUND_AMOUNT_EXCEEDED("ORDER-041", "退款金额超过剩余可退金额"),

    ;

    private final String code;
    private final String message;

}
