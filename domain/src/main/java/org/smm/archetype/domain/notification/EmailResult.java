package org.smm.archetype.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件发送结果
 * @author Leonardo
 * @since 2026/01/09
 */
@Data
@Builder(setterPrefix = "set")
@NoArgsConstructor
@AllArgsConstructor
public class EmailResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

}
