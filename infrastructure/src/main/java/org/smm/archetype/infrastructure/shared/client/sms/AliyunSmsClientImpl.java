// package org.smm.archetype.infrastructure.shared.client.sms;
//
// import com.alibaba.fastjson2.JSON;
// import com.aliyun.dysmsapi20170525.Client;
// import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
// import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
// import com.aliyun.teaopenapi.models.Config;
// import lombok.extern.slf4j.Slf4j;
// import org.smm.archetype.domain.shared.client.dto.ServiceProvider;
// import org.smm.archetype.domain.shared.client.dto.SmsRequest;
// import org.smm.archetype.domain.shared.client.dto.SmsResult;
//
// /**
//  * 阿里云短信服务实现
//  *
// 基于阿里云短信服务（SMS）发送短信。
//  *
// <a href="https://help.aliyun.com/zh/sms/">官方文档</a>
//
//
//  */
// @Slf4j
// public class AliyunSmsClientImpl extends AbstractSmsClient {
//
//     private final String accessKeyId;
//     private final String accessKeySecret;
//     private final String regionId;
//     private final String signName;
//     private final Client smsClient;
//
//     public AliyunSmsClientImpl(
//             String accessKeyId,
//             String accessKeySecret,
//             String regionId,
//             String signName) {
//         this.accessKeyId = accessKeyId;
//         this.accessKeySecret = accessKeySecret;
//         this.regionId = regionId;
//         this.signName = signName;
//         this.smsClient = createSmsClient();
//     }
//
//     @Override
//     protected SmsResult doSendSms(SmsRequest request, ServiceProvider provider) {
//         try {
//             SendSmsRequest sendSmsRequest = new SendSmsRequest();
//
//             // 设置手机号（需要去掉+86前缀，如果有的话）
//             String phoneNumber = request.getPhoneNumber();
//             if (phoneNumber.startsWith("+")) {
//                 phoneNumber = phoneNumber.substring(1);
//             }
//             sendSmsRequest.setPhoneNumbers(phoneNumber);
//
//             // 设置签名名称（优先使用请求中的签名，否则使用配置的默认签名）
//             String signName = request.getSignName() != null && !request.getSignName().isBlank()
//                                       ? request.getSignName()
//                                       : this.signName;
//             sendSmsRequest.setSignName(signName);
//
//             // 设置模板代码
//             sendSmsRequest.setTemplateCode(request.getTemplateCode());
//
//             // 设置模板参数（转换为JSON字符串）
//             if (request.getTemplateParam() != null && !request.getTemplateParam().isEmpty()) {
//                 String templateParamJson = JSON.toJSONString(request.getTemplateParam());
//                 sendSmsRequest.setTemplateParam(templateParamJson);
//             }
//
//             // 发送短信
//             SendSmsResponse response = smsClient.sendSms(sendSmsRequest);
//
//             // 判断是否成功（阿里云短信成功返回Code="OK"）
//             boolean success = "OK".equals(response.getBody().getCode());
//
//             if (success) {
//                 log.info("Aliyun SMS sent successfully: requestId={}, phoneNumber={}",
//                         response.getBody().getRequestId(), phoneNumber);
//                 return SmsResult.builder()
//                                .setSuccess(true)
//                                .setMessageId(response.getBody().getRequestId())
//                                .build();
//             } else {
//                 log.error("Aliyun SMS send failed: code={}, message={}, phoneNumber={}",
//                         response.getBody().getCode(),
//                         response.getBody().getMessage(),
//                         phoneNumber);
//                 return SmsResult.builder()
//                                .setSuccess(false)
//                                .setErrorCode(response.getBody().getCode())
//                                .setErrorMessage("Aliyun SMS send failed: " + response.getBody().getMessage())
//                                .build();
//             }
//
//         } catch (Exception e) {
//             log.error("Failed to send Aliyun SMS: phoneNumber={}", request.getPhoneNumber(), e);
//             return SmsResult.builder()
//                            .setSuccess(false)
//                            .setErrorCode("EXCEPTION")
//                            .setErrorMessage("Exception: " + e.getMessage())
//                            .build();
//         }
//     }
//
//     @Override
//     protected ServiceProvider getDefaultProvider() {
//         return ServiceProvider.ALIYUN;
//     }
//
//     /**
//      * 创建阿里云短信客户端
//      */
//     private Client createSmsClient() {
//         Config config = new Config()
//                                 .setAccessKeyId(accessKeyId)
//                                 .setAccessKeySecret(accessKeySecret)
//                                 .setRegionId(regionId);
//
//         try {
//             return new Client(config);
//         } catch (Exception e) {
//             throw new IllegalStateException("Failed to create Aliyun SMS client", e);
//         }
//     }
//
// }
