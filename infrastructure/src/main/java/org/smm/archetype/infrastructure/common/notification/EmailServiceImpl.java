package org.smm.archetype.infrastructure.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.common.notification.EmailRequest;
import org.smm.archetype.domain.common.notification.EmailResult;
import org.smm.archetype.domain.common.notification.ServiceProvider;

import java.util.UUID;

/**
 * 邮件服务实现（模拟实现）
 *
 * <p>脚手架项目的模拟实现，用于演示和测试。
 *
 * <p>生产环境接入方式：
 * <ul>
 *   <li>添加邮件服务SDK依赖（如Spring Mail、阿里云邮件推送等）</li>
 *   <li>注入真实的邮件服务客户端</li>
 *   <li>实现doSendEmail方法调用真实API</li>
 *   <li>配置服务商账号信息</li>
 * </ul>
 *
 * <p>示例接入：
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class EmailServiceImpl extends AbstractEmailService {
 *
 *     private final JavaMailSender mailSender;
 *
 *     @Override
 *     protected EmailResult doSendEmail(EmailRequest request, ServiceProvider provider) {
 *         MimeMessage message = mailSender.createMimeMessage();
 *         MimeMessageHelper helper = new MimeMessageHelper(message, true);
 *         helper.setTo(request.getTo().toArray(new String[0]));
 *         helper.setSubject(request.getSubject());
 *         helper.setText(request.getBody(), true);
 *         mailSender.send(message);
 *
 *         return EmailResult.success(UUID.randomUUID().toString());
 *     }
 * }
 * }</pre>
 * @author Leonardo
 * @since 2026/01/10
 */
@Slf4j
public class EmailServiceImpl extends AbstractEmailService {

    /**
     * 默认服务商
     */
    private static final ServiceProvider DEFAULT_PROVIDER = ServiceProvider.ALIYUN;

    /**
     * 执行实际的邮件发送逻辑（模拟实现）
     *
     * <p>TODO: 生产环境需要接入真实的邮件服务商
     *
     * <p>推荐服务商：
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
        log.info("========== Email Service (Mock) ==========");
        log.info("Provider: {}", provider);
        log.info("To: {}", request.getTo());
        log.info("Subject: {}", request.getSubject());
        log.info("HTML Body: {}", request.getHtmlBody());
        log.info("Text Body: {}", request.getTextBody());
        log.info("========================================");

        // 模拟发送邮件（实际生产环境需要调用真实API）
        String messageId = "MOCK_EMAIL_" + UUID.randomUUID();

        log.info("Email sent successfully (mock): messageId={}, to={}", messageId, request.getTo());

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
