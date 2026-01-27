package org.smm.archetype.infrastructure.bizshared.client.sms;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.client.dto.ServiceProvider;
import org.smm.archetype.domain.bizshared.client.dto.SmsRequest;
import org.smm.archetype.domain.bizshared.client.dto.SmsResult;

import java.util.UUID;

/**
 * 短信服务实现（模拟实现）
 *
 * <p>脚手架项目的模拟实现，用于演示和测试。
 *
 * <p>生产环境接入方式：
 * <ul>
 *   <li>添加短信服务SDK依赖（如阿里云短信、腾讯云短信等）</li>
 *   <li>注入真实的短信服务客户端</li>
 *   <li>实现doSendSms方法调用真实API</li>
 *   <li>配置服务商账号信息</li>
 * </ul>
 *
 * <p>示例接入（实现类）：
 * <pre>{@code
 * public class AliyunSmsClientImpl extends AbstractSmsClient {
 *
 *     private final SendSmsRequest smsClient;
 *
 *     public AliyunSmsClientImpl(SendSmsRequest smsClient) {
 *         this.smsClient = smsClient;
 *     }
 *
 *     @Override
 *     protected SmsResult doSendSms(SmsRequest request, ServiceProvider provider) {
 *         SendSmsRequest req = SendSmsRequest.builder()
 *             .phoneNumbers(request.getPhone())
 *             .templateCode(request.getTemplateCode())
 *             .templateParam(JsonUtils.toJson(request.getParams()))
 *             .build();
 *         SendSmsResponse response = smsClient.send(req);
 *
 *         return SmsResult.success(response.getRequestId());
 *     }
 * }
 * }</pre>
 *
 * <p>示例接入（配置类注册）：
 * <pre>{@code
 * @Configuration
 * public class NotificationConfigure {
 *
 *     @Bean
 *     @ConditionalOnProperty(prefix = "notification.sms", name = "enabled", havingValue = "true")
 *     public SmsClient smsService(SendSmsRequest smsClient) {
 *         return new AliyunSmsClientImpl(smsClient);
 *     }
 * }
 * }</pre>
 * @author Leonardo
 * @since 2026/01/10
 */
@Slf4j
public class SmsClientImpl extends AbstractSmsClient {

    /**
     * 默认服务商
     */
    private static final ServiceProvider DEFAULT_PROVIDER = ServiceProvider.ALIYUN;

    /**
     * 执行实际的短信发送逻辑（模拟实现）
     *
     * <p>TODO: 生产环境需要接入真实的短信服务商
     *
     * <p>推荐服务商：
     * <ul>
     *   <li><a href="https://www.aliyun.com/product/sms">阿里云短信</a></li>
     *   <li>腾讯云短信：https://cloud.tencent.com/product/sms</li>
     *   <li>华为云短信：https://www.huaweicloud.com/product/msgsms.html</li>
     *   <li>Twilio：https://www.twilio.com/sms</li>
     *   <li>云片：https://www.yunpian.com/</li>
     * </ul>
     * @param request  短信请求
     * @param provider 服务商
     * @return 发送结果（模拟成功）
     */
    @Override
    protected SmsResult doSendSms(SmsRequest request, ServiceProvider provider) {
        log.info("========== SMS Service (Mock) ==========");
        log.info("Provider: {}", provider);
        log.info("Phone Number: {}", request.getPhoneNumber());
        log.info("Sign Name: {}", request.getSignName());
        log.info("Template Code: {}", request.getTemplateCode());
        log.info("Template Param: {}", request.getTemplateParam());
        log.info("========================================");

        // 模拟发送短信（实际生产环境需要调用真实API）
        String messageId = "MOCK_SMS_" + UUID.randomUUID();

        log.info("SMS sent successfully (mock): messageId={}, phoneNumber={}", messageId, request.getPhoneNumber());

        return SmsResult.builder()
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
