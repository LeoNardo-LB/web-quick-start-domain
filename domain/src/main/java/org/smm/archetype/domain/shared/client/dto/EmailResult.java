package org.smm.archetype.domain.shared.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 邮件发送结果


 */
@Getter
@Setter
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
