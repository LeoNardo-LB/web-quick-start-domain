package org.smm.archetype.domain.bizshared.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 短信发送结果
 * @author Leonardo
 * @since 2026/01/09
 */
@Getter
@Setter
@Builder(setterPrefix = "set")
@NoArgsConstructor
@AllArgsConstructor
public class SmsResult {

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
