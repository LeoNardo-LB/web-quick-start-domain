package org.smm.archetype.infrastructure.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.common.notification.EmailRequest;
import org.smm.archetype.domain.common.notification.EmailResult;
import org.smm.archetype.domain.common.notification.EmailService;
import org.smm.archetype.domain.common.notification.provider.ServiceProvider;

import java.util.List;

/**
 * 邮件服务抽象基类
 *
 * <p>实现邮件发送的通用流程模板，定义扩展点供子类实现具体服务商接入。
 *
 * <p>核心功能：
 * <ul>
 *   <li>参数校验</li>
 *   <li>服务商选择</li>
 *   <li>批量发送流程编排</li>
 *   <li>异常处理与日志记录</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
public abstract class AbstractEmailService implements EmailService {

    /**
     * 发送邮件（模板方法）
     *
     * <p>定义发送邮件的通用流程：
     * <ol>
     *   <li>校验请求参数</li>
     *   <li>选择默认服务商</li>
     *   <li>调用具体发送逻辑</li>
     *   <li>记录日志</li>
     * </ol>
     * @param request 邮件请求
     * @return 发送结果
     * @throws IllegalArgumentException 当 request 为 null 时抛出
     */
    @Override
    public final EmailResult sendEmail(EmailRequest request) {
        return sendEmail(request, getDefaultProvider());
    }

    /**
     * 发送邮件（指定服务商，模板方法）
     *
     * <p>定义发送邮件的通用流程：
     * <ol>
     *   <li>校验请求参数</li>
     *   <li>调用具体发送逻辑</li>
     *   <li>记录日志</li>
     * </ol>
     * @param request  邮件请求
     * @param provider 服务商
     * @return 发送结果
     * @throws IllegalArgumentException 当 request 或 provider 为 null 时抛出
     */
    @Override
    public final EmailResult sendEmail(EmailRequest request, ServiceProvider provider) {
        // 1. 参数校验
        if (request == null) {
            log.error("Email request cannot be null");
            throw new IllegalArgumentException("Email request cannot be null");
        }
        if (provider == null) {
            log.error("Service provider cannot be null");
            throw new IllegalArgumentException("Service provider cannot be null");
        }

        log.debug("Sending email: to={}, provider={}", request.getTo(), provider);

        try {
            // 2. 调用子类实现的具体发送逻辑
            EmailResult result = doSendEmail(request, provider);

            // 3. 记录结果
            if (result.isSuccess()) {
                log.info("Email sent successfully: to={}, provider={}, messageId={}",
                        request.getTo(), provider, result.getMessageId());
            } else {
                log.warn("Email sending failed: to={}, provider={}, error={}",
                        request.getTo(), provider, result.getErrorMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("Unexpected error sending email: to={}, provider={}", request.getTo(), provider, e);
            return EmailResult.builder()
                           .setSuccess(false)
                           .setErrorMessage("Unexpected error: " + e.getMessage())
                           .build();
        }
    }

    /**
     * 批量发送邮件（模板方法）
     *
     * <p>调用单发方法实现批量发送。
     * @param requests 邮件请求列表
     * @return 发送结果列表
     * @throws IllegalArgumentException 当 requests 为 null 时抛出
     */
    @Override
    public final List<EmailResult> sendBatchEmail(List<EmailRequest> requests) {
        if (requests == null) {
            log.error("Email requests cannot be null");
            throw new IllegalArgumentException("Email requests cannot be null");
        }

        log.info("Sending batch emails: count={}", requests.size());

        return requests.stream()
                       .map(this::sendEmail)
                       .toList();
    }

    /**
     * 执行实际的邮件发送逻辑（由子类实现）
     *
     * <p>扩展点：子类实现具体服务商的邮件发送逻辑，如：
     * <ul>
     *   <li>阿里云邮件服务</li>
     *   <li>腾讯云邮件服务</li>
     *   <li>华为云邮件服务</li>
     *   <li>SMTP 服务</li>
     * </ul>
     * @param request  邮件请求
     * @param provider 服务商
     * @return 发送结果
     * @throws Exception 发送异常
     */
    protected abstract EmailResult doSendEmail(EmailRequest request, ServiceProvider provider);

    /**
     * 获取默认服务商（由子类实现）
     * @return 默认服务商
     */
    protected abstract ServiceProvider getDefaultProvider();

}
