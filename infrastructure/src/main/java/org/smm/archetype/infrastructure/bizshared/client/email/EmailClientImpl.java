package org.smm.archetype.infrastructure.bizshared.client.email;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.client.dto.EmailRequest;
import org.smm.archetype.domain.bizshared.client.dto.EmailResult;
import org.smm.archetype.domain.bizshared.client.dto.ServiceProvider;

import java.util.UUID;

/**
 * 邮件服务实现（模拟），用于演示和测试。
 */
@Slf4j
public class EmailClientImpl extends AbstractEmailClient {

    /**
     * 默认服务商
     */
    private static final ServiceProvider DEFAULT_PROVIDER = ServiceProvider.ALIYUN;

    /**
     * 执行实际的邮件发送逻辑（模拟实现）
     *
    TODO: 生产环境需要接入真实的邮件服务商
     *
    推荐服务商：
     * <ul>
     *   <li><a href="https://www.aliyun.com/product/directmail">阿里云邮件推送</a></li>
     *   <li><a href="https://cloud.tencent.com/product/ses">腾讯云邮件服务</a></li>
     *   <li><a href="https://sendgrid.com/">SendGrid</a></li>
     *   <li><a href="https://spring.io/guides/gs/sending-mail/">Spring Mail（SMTP）</a></li>
     * </ul>
     * @param request  邮件请求
     * @param provider 服务商
     * @return 发送结果（模拟成功）
     */
    @Override
    protected EmailResult doSendEmail(EmailRequest request, ServiceProvider provider) {
        log.info("========== 邮件服务（模拟）==========");
        log.info("Provider: {}", provider);
        log.info("To: {}", request.getTo());
        log.info("Subject: {}", request.getSubject());
        log.info("HTML Body: {}", request.getHtmlBody());
        log.info("Text Body: {}", request.getTextBody());
        log.info("========================================");

        // 模拟发送邮件（实际生产环境需要调用真实API）
        String messageId = "MOCK_EMAIL_" + UUID.randomUUID();

        log.info("邮件发送成功（模拟）: messageId={}, 接收方={}", messageId, request.getTo());

        return EmailResult.builder()
                       .setSuccess(true)
                       .setMessageId(messageId)
                       .build();
    }

    /**
     * 获取默认服务商
     * @return 默认服务商（阿里云）
     */
    @Override
    protected ServiceProvider getDefaultProvider() {
        return DEFAULT_PROVIDER;
    }

}
