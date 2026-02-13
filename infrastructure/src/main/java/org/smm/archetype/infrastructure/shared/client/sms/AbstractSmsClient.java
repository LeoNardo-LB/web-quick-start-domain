package org.smm.archetype.infrastructure.shared.client.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.client.SmsClient;
import org.smm.archetype.domain.shared.client.dto.ServiceProvider;
import org.smm.archetype.domain.shared.client.dto.SmsRequest;
import org.smm.archetype.domain.shared.client.dto.SmsResult;

import java.util.List;

/**
 * 短信服务抽象基类，提供通用发送流程模板。
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSmsClient implements SmsClient {

    /**
     * 发送短信（使用默认服务商）。
     * @param request 短信请求
     * @return 发送结果
     * @throws IllegalArgumentException 当 request 为 null 时抛出
     */
    @Override
    public final SmsResult sendSms(SmsRequest request) {
        return sendSms(request, getDefaultProvider());
    }

    /**
     * 发送短信（指定服务商）。
     * @param request 短信请求
     * @param provider 服务商
     * @return 发送结果
     * @throws IllegalArgumentException 当 request 或 provider 为 null 时抛出
     */
    @Override
    public final SmsResult sendSms(SmsRequest request, ServiceProvider provider) {
        // 1. 参数校验
        if (request == null) {
            log.error("短信请求不能为空");
            throw new IllegalArgumentException("SMS request cannot be null");
        }
        if (provider == null) {
            log.error("服务提供者不能为空");
            throw new IllegalArgumentException("Service provider cannot be null");
        }

        log.debug("正在发送短信: 接收方={}, 提供者={}", request.getPhoneNumber(), provider);

        try {
            // 2. 调用子类实现的具体发送逻辑
            SmsResult result = doSendSms(request, provider);

            // 3. 记录结果
            if (result.isSuccess()) {
                log.info("短信发送成功: 接收方={}, 提供者={}, 消息ID={}",
                        request.getPhoneNumber(), provider, result.getMessageId());
            } else {
                log.warn("短信发送失败: 接收方={}, 提供者={}, 错误={}",
                        request.getPhoneNumber(), provider, result.getErrorMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("发送短信时出现意外错误: 接收方={}, 提供者={}", request.getPhoneNumber(), provider, e);
            return SmsResult.builder()
                           .setSuccess(false)
                           .setErrorMessage("Unexpected error: " + e.getMessage())
                           .build();
        }
    }

    /**
     * 批量发送短信（模板方法）
     *
    调用单发方法实现批量发送。
     * @param requests 短信请求列表
     * @return 发送结果列表
     * @throws IllegalArgumentException 当 requests 为 null 时抛出
     */
    @Override
    public final List<SmsResult> sendBatchSms(List<SmsRequest> requests) {
        if (requests == null) {
            log.error("SMS requests cannot be null");
            throw new IllegalArgumentException("SMS requests cannot be null");
        }

        log.info("Sending batch SMS: count={}", requests.size());

        return requests.stream()
                       .map(this::sendSms)
                       .toList();
    }

    /**
     * 执行实际的短信发送逻辑（由子类实现）
     * <p>
     * 扩展点：子类实现具体服务商的短信发送逻辑，如：
     * <ul>
     *   <li>阿里云短信服务</li>
     *   <li>腾讯云短信服务</li>
     *   <li>华为云短信服务</li>
     *   <li>Twilio</li>
     * </ul>
     * @param request  短信请求
     * @param provider 服务商
     * @return 发送结果
     */
    protected abstract SmsResult doSendSms(SmsRequest request, ServiceProvider provider);

    /**
     * 获取默认服务商（由子类实现）
     * @return 默认服务商
     */
    protected abstract ServiceProvider getDefaultProvider();

}
