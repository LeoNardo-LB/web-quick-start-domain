package org.smm.archetype.domain.bizshared.client;

import org.smm.archetype.domain.bizshared.client.dto.EmailRequest;
import org.smm.archetype.domain.bizshared.client.dto.EmailResult;
import org.smm.archetype.domain.bizshared.client.dto.ServiceProvider;

/**
 * 邮件服务接口
 *
 * <p>支持多云服务商（阿里云、腾讯云、华为云）。
 *
 * @author Leonardo
 * @since 2026/01/09
 */
public interface EmailClient {

    /**
     * 发送邮件
     * @param request 邮件请求
     * @return 发送结果
     */
    EmailResult sendEmail(EmailRequest request);

    /**
     * 发送邮件（指定服务商）
     * @param request 邮件请求
     * @param provider 服务商
     * @return 发送结果
     */
    EmailResult sendEmail(EmailRequest request, ServiceProvider provider);

    /**
     * 批量发送邮件
     * @param requests 邮件请求列表
     * @return 发送结果列表
     */
    java.util.List<EmailResult> sendBatchEmail(java.util.List<EmailRequest> requests);

}
