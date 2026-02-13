package org.smm.archetype.domain.shared.client;

import org.smm.archetype.domain.shared.client.dto.ServiceProvider;
import org.smm.archetype.domain.shared.client.dto.SmsRequest;
import org.smm.archetype.domain.shared.client.dto.SmsResult;

/**
 * 短信服务接口，支持多云服务商。
 */
public interface SmsClient {

    /**
     * 发送短信
     * @param request 短信请求
     * @return 发送结果
     */
    SmsResult sendSms(SmsRequest request);

    /**
     * 发送短信（指定服务商）
     * @param request 短信请求
     * @param provider 服务商
     * @return 发送结果
     */
    SmsResult sendSms(SmsRequest request, ServiceProvider provider);

    /**
     * 批量发送短信
     * @param requests 短信请求列表
     * @return 发送结果列表
     */
    java.util.List<SmsResult> sendBatchSms(java.util.List<SmsRequest> requests);

}
