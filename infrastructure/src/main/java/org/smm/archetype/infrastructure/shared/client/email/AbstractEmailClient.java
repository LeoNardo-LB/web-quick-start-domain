package org.smm.archetype.infrastructure.shared.client.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.client.EmailClient;
import org.smm.archetype.domain.shared.client.dto.EmailRequest;
import org.smm.archetype.domain.shared.client.dto.EmailResult;
import org.smm.archetype.domain.shared.client.dto.ServiceProvider;

import java.util.List;

/**
 * 邮件服务抽象基类，提供通用发送流程模板。
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEmailClient implements EmailClient {

    /**
     * 发送邮件（使用默认服务商）。
     * @param request 邮件请求
     * @return 发送结果
     * @throws IllegalArgumentException 当 request 为 null 时抛出
     */
    @Override
    public final EmailResult sendEmail(EmailRequest request) {
        return sendEmail(request, getDefaultProvider());
    }

    /**
     * 发送邮件（指定服务商）。
     * @param request 邮件请求
     * @param provider 服务商
     * @return 发送结果
     * @throws IllegalArgumentException 当 request 或 provider 为 null 时抛出
     */
    @Override
    public final EmailResult sendEmail(EmailRequest request, ServiceProvider provider) {
        // 1. 参数校验
        if (request == null) {
            log.error("邮件请求不能为空");
            throw new IllegalArgumentException("Email request cannot be null");
        }
        if (provider == null) {
            log.error("服务提供者不能为空");
            throw new IllegalArgumentException("Service provider cannot be null");
        }

        log.debug("正在发送邮件: 接收方={}, 提供者={}", request.getTo(), provider);

        try {
            // 2. 调用子类实现的具体发送逻辑
            EmailResult result = doSendEmail(request, provider);

            // 3. 记录结果
            if (result.isSuccess()) {
                log.info("邮件发送成功: 接收方={}, 提供者={}, 消息ID={}",
                        request.getTo(), provider, result.getMessageId());
            } else {
                log.warn("邮件发送失败: 接收方={}, 提供者={}, 错误={}",
                        request.getTo(), provider, result.getErrorMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("发送邮件时出现意外错误: 接收方={}, 提供者={}", request.getTo(), provider, e);
            return EmailResult.builder()
                           .setSuccess(false)
                           .setErrorMessage("Unexpected error: " + e.getMessage())
                           .build();
        }
    }

    /**
     * 批量发送邮件（模板方法）
     *
    调用单发方法实现批量发送。
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
    扩展点：子类实现具体服务商的邮件发送逻辑，如：
     * <ul>
     *   <li>阿里云邮件服务</li>
     *   <li>腾讯云邮件服务</li>
     *   <li>华为云邮件服务</li>
     *   <li>SMTP 服务</li>
     * </ul>
     * @param request  邮件请求
     * @param provider 服务商
     * @return 发送结果
     */
    protected abstract EmailResult doSendEmail(EmailRequest request, ServiceProvider provider);

    /**
     * 获取默认服务商（由子类实现）
     * @return 默认服务商
     */
    protected abstract ServiceProvider getDefaultProvider();

}
