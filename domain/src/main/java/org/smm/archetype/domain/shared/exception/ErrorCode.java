package org.smm.archetype.domain.shared.exception;

/**
 * 错误码接口
 *
 * <p>所有错误码枚举都应实现此接口，以提供统一的错误码访问方式。
 * 使用接口而非直接使用枚举，允许不同模块定义自己的错误码枚举。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * public enum OrderErrorCode implements ErrorCode {
 *     ORDER_NOT_FOUND("ORDER-001", "订单不存在"),
 *     ORDER_ALREADY_PAID("ORDER-002", "订单已支付");
 *
 *     private final String code;
 *     private final String message;
 *
 *     OrderErrorCode(String code, String message) {
 *         this.code = code;
 *         this.message = message;
 *     }
 *
 *     {@literal @}Override
 *     public String getCode() { return code; }
 *
 *     {@literal @}Override
 *     public String getMessage() { return message; }
 * }
 * </pre>
 */
public interface ErrorCode {

    /**
     * 获取错误码
     *
     * @return 错误码字符串
     */
    String getCode();

    /**
     * 获取错误消息
     *
     * @return 错误消息
     */
    String getMessage();

}
