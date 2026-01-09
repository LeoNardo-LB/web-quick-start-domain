package org.smm.archetype.infrastructure.common.notification.email.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.common.notification.EmailRequest;
import org.smm.archetype.domain.common.notification.EmailResult;
import org.smm.archetype.domain.common.notification.provider.ServiceProvider;
import org.smm.archetype.infrastructure.common.notification.email.AbstractEmailService;

/**
 * 邮件服务 TODO 实现
 *
 * <p>占位实现，抛出 UnsupportedOperationException 提示需要接入真实的邮件服务商。
 *
 * <p>待接入服务商：
 * <ul>
 *   <li>阿里云邮件推送</li>
 *   <li>腾讯云邮件服务</li>
 *   <li>华为云邮件服务</li>
 *   <li>SendGrid</li>
 *   <li>SMTP 服务</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
public class TodoEmailServiceImpl extends AbstractEmailService {

    /**
     * 默认服务商
     */
    private static final ServiceProvider DEFAULT_PROVIDER = ServiceProvider.ALIYUN;

    /**
     * 执行实际的邮件发送逻辑（TODO）
     * @param request  邮件请求
     * @param provider 服务商
     * @return 发送结果
     * @throws UnsupportedOperationException 始终抛出，提示需要接入真实服务商
     */
    @Override
    protected EmailResult doSendEmail(EmailRequest request, ServiceProvider provider) {
        log.error("EmailService is not implemented yet. Please integrate with a real email provider: provider={}", provider);

        throw new UnsupportedOperationException(
                String.format("EmailService is not implemented yet. Please integrate with a real email provider: %s. " +
                                      "Supported providers: ALIYUN, TENCENT, HUAWEI, SENDGRID, SMTP.", provider)
        );
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
