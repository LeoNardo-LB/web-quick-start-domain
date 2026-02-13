// package org.smm.archetype.infrastructure.shared.client.email;
//
// import com.aliyun.dm20151123.Client;
// import com.aliyun.dm20151123.models.SingleSendMailRequest;
// import com.aliyun.dm20151123.models.SingleSendMailResponse;
// import com.aliyun.teaopenapi.models.Config;
// import lombok.extern.slf4j.Slf4j;
// import org.smm.archetype.domain.shared.client.dto.EmailRequest;
// import org.smm.archetype.domain.shared.client.dto.EmailResult;
// import org.smm.archetype.domain.shared.client.dto.ServiceProvider;
//
// /**
//  * 阿里云邮件实现，基于DirectMail推送。
//  */
// @Slf4j
// public class AliyunEmailClientImpl extends AbstractEmailClient {
//
//     private final String accessKeyId;
//     private final String accessKeySecret;
//     private final String regionId;
//     private final String fromAddress;
//     private final String fromAlias;
//     private final String accountName;
//     private final Client emailClient;
//
//     public AliyunEmailClientImpl(
//             String accessKeyId,
//             String accessKeySecret,
//             String regionId,
//             String fromAddress,
//             String fromAlias,
//             String accountName) {
//         this.accessKeyId = accessKeyId;
//         this.accessKeySecret = accessKeySecret;
//         this.regionId = regionId;
//         this.fromAddress = fromAddress;
//         this.fromAlias = fromAlias;
//         this.accountName = accountName;
//         this.emailClient = createEmailClient();
//     }
//
//     @Override
//     protected EmailResult doSendEmail(EmailRequest request, ServiceProvider provider) {
//         try {
//             SingleSendMailRequest sendMailRequest = new SingleSendMailRequest();
//
//             // 设置发信地址（必填）
//             String accountName = this.accountName;
//             if (accountName == null || accountName.isBlank()) {
//                 accountName = fromAddress;
//             }
//             if (accountName == null || accountName.isBlank()) {
//                 throw new IllegalArgumentException("Account name or from address must be configured");
//             }
//             sendMailRequest.setAccountName(accountName);
//
//             // 设置发信人别名（可选）
//             String fromAlias = this.fromAlias;
//             if (fromAlias != null && !fromAlias.isBlank()) {
//                 sendMailRequest.setFromAlias(fromAlias);
//             }
//
//             // 设置地址类型（1: 发信地址）
//             sendMailRequest.setAddressType(1);
//
//             // 设置收件人地址
//             sendMailRequest.setToAddress(request.getTo());
//
//             // 设置邮件主题
//             sendMailRequest.setSubject(request.getSubject());
//
//             // 设置邮件正文
//             if (request.getHtmlBody() != null && !request.getHtmlBody().isBlank()) {
//                 sendMailRequest.setHtmlBody(request.getHtmlBody());
//             } else if (request.getTextBody() != null && !request.getTextBody().isBlank()) {
//                 sendMailRequest.setTextBody(request.getTextBody());
//             } else {
//                 throw new IllegalArgumentException("Either HTML body or text body must be provided");
//             }
//
//             // 发送邮件
//             SingleSendMailResponse response = emailClient.singleSendMail(sendMailRequest);
//
//             // 判断是否成功（基于HTTP状态码）
//             boolean success = response.getStatusCode() == 200;
//
//             if (success) {
//                 log.info("Aliyun email sent successfully: requestId={}, to={}",
//                         response.getBody().getRequestId(), request.getTo());
//                 return EmailResult.builder()
//                                .setSuccess(true)
//                                .setMessageId(response.getBody().getRequestId())
//                                .build();
//             } else {
//                 log.error("Aliyun email send failed: statusCode={}, to={}",
//                         response.getStatusCode(), request.getTo());
//                 return EmailResult.builder()
//                                .setSuccess(false)
//                                .setErrorCode(String.valueOf(response.getStatusCode()))
//                                .setErrorMessage("Aliyun email send failed with status: " + response.getStatusCode())
//                                .build();
//             }
//
//         } catch (Exception e) {
//             log.error("Failed to send Aliyun email: to={}", request.getTo(), e);
//             return EmailResult.builder()
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
//      * 创建阿里云邮件客户端
//      */
//     private Client createEmailClient() {
//         Config config = new Config()
//                                 .setAccessKeyId(accessKeyId)
//                                 .setAccessKeySecret(accessKeySecret)
//                                 .setRegionId(regionId);
//
//         try {
//             return new Client(config);
//         } catch (Exception e) {
//             throw new IllegalStateException("Failed to create Aliyun email client", e);
//         }
//     }
//
// }
