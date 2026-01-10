package org.smm.archetype.infrastructure.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.common.notification.SmsRequest;
import org.smm.archetype.domain.common.notification.SmsResult;
import org.smm.archetype.domain.common.notification.SmsService;
import org.smm.archetype.domain.common.notification.provider.ServiceProvider;

import java.util.List;

/**
 * 短信服务抽象基类
 *
 * <p>实现短信发送的通用流程模板，定义扩展点供子类实现具体服务商接入。
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
public abstract class AbstractSmsService implements SmsService {

    /**
     * 发送短信（模板方法）
     *
     * <p>定义发送短信的通用流程：
     * <ol>
     *   <li>校验请求参数</li>
     *   <li>选择默认服务商</li>
     *   <li>调用具体发送逻辑</li>
     *   <li>记录日志</li>
     * </ol>
     * @param request 短信请求
     * @return 发送结果
     * @throws IllegalArgumentException 当 request 为 null 时抛出
     */
    @Override
    public final SmsResult sendSms(SmsRequest request) {
        return sendSms(request, getDefaultProvider());
    }

    /**
     * 发送短信（指定服务商，模板方法）
     *
     * <p>定义发送短信的通用流程：
     * <ol>
     *   <li>校验请求参数</li>
     *   <li>调用具体发送逻辑</li>
     *   <li>记录日志</li>
     * </ol>
     * @param request  短信请求
     * @param provider 服务商
     * @return 发送结果
     * @throws IllegalArgumentException 当 request 或 provider 为 null 时抛出
     */
    @Override
    public final SmsResult sendSms(SmsRequest request, ServiceProvider provider) {
        // 1. 参数校验
        if (request == null) {
            log.error("SMS request cannot be null");
            throw new IllegalArgumentException("SMS request cannot be null");
        }
        if (provider == null) {
            log.error("Service provider cannot be null");
            throw new IllegalArgumentException("Service provider cannot be null");
        }

        log.debug("Sending SMS: to={}, provider={}", request.getPhoneNumber(), provider);

        try {
            // 2. 调用子类实现的具体发送逻辑
            SmsResult result = doSendSms(request, provider);

            // 3. 记录结果
            if (result.isSuccess()) {
                log.info("SMS sent successfully: to={}, provider={}, messageId={}",
                        request.getPhoneNumber(), provider, result.getMessageId());
            } else {
                log.warn("SMS sending failed: to={}, provider={}, error={}",
                        request.getPhoneNumber(), provider, result.getErrorMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("Unexpected error sending SMS: to={}, provider={}", request.getPhoneNumber(), provider, e);
            return SmsResult.builder()
                           .setSuccess(false)
                           .setErrorMessage("Unexpected error: " + e.getMessage())
                           .build();
        }
    }

    /**
     * 批量发送短信（模板方法）
     *
     * <p>调用单发方法实现批量发送。
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
     *
     * <p>扩展点：子类实现具体服务商的短信发送逻辑，如：
     * <ul>
     *   <li>阿里云短信服务</li>
     *   <li>腾讯云短信服务</li>
     *   <li>华为云短信服务</li>
     *   <li>Twilio</li>
     * </ul>
     * @param request  短信请求
     * @param provider 服务商
     * @return 发送结果
     * @throws Exception 发送异常
     */
    protected abstract SmsResult doSendSms(SmsRequest request, ServiceProvider provider);

    /**
     * 获取默认服务商（由子类实现）
     * @return 默认服务商
     */
    protected abstract ServiceProvider getDefaultProvider();

}
