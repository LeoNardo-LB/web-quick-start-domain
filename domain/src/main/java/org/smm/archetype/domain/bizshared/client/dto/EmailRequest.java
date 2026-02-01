package org.smm.archetype.domain.bizshared.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 邮件请求DTO，包含收件人、主题和正文。
 */
@Getter
@Setter
@Builder(setterPrefix = "set")
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    /**
     * 收件人邮箱
     */
    private String to;

    /**
     * 抄送邮箱
     */
    private List<String> cc;

    /**
     * 密送邮箱
     */
    private List<String> bcc;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件正文（HTML格式）
     */
    private String htmlBody;

    /**
     * 邮件正文（纯文本格式）
     */
    private String textBody;

    /**
     * 附件列表
     */
    private List<EmailAttachment> attachments;

    /**
     * 邮件附件
     */
    @Getter
    @Setter
    @Builder(setterPrefix = "set")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailAttachment {

        /**
         * 文件名
         */
        private String fileName;

        /**
         * 文件内容（Base64编码）
         */
        private String content;

        /**
         * MIME类型
         */
        private String contentType;

    }

}
