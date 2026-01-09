package org.smm.archetype.infrastructure.common.notification.sms.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.common.notification.SmsRequest;
import org.smm.archetype.domain.common.notification.SmsResult;
import org.smm.archetype.domain.common.notification.provider.ServiceProvider;
import org.smm.archetype.infrastructure.common.notification.sms.AbstractSmsService;

/**
 * 短信服务 TODO 实现
 *
 * <p>占位实现，抛出 UnsupportedOperationException 提示需要接入真实的短信服务商。
 *
 * <p>待接入服务商：
 * <ul>
 *   <li>阿里云短信服务</li>
 *   <li>腾讯云短信服务</li>
 *   <li>华为云短信服务</li>
 *   <li>Twilio</li>
 *   <li>云片</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
public class TodoSmsServiceImpl extends AbstractSmsService {

    /**
     * 默认服务商
     */
    private static final ServiceProvider DEFAULT_PROVIDER = ServiceProvider.ALIYUN;

    /**
     * 执行实际的短信发送逻辑（TODO）
     * @param request  短信请求
     * @param provider 服务商
     * @return 发送结果
     * @throws UnsupportedOperationException 始终抛出，提示需要接入真实服务商
     */
    @Override
    protected SmsResult doSendSms(SmsRequest request, ServiceProvider provider) {
        log.error("SmsService is not implemented yet. Please integrate with a real SMS provider: provider={}", provider);

        throw new UnsupportedOperationException(
                String.format("SmsService is not implemented yet. Please integrate with a real SMS provider: %s. " +
                                      "Supported providers: ALIYUN, TENCENT, HUAWEI, TWILIO, YUNPIAN.", provider)
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
