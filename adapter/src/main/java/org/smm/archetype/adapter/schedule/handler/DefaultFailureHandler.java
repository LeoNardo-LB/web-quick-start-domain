package org.smm.archetype.adapter.schedule.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.base.DomainEvent;
import org.smm.archetype.domain.bizshared.client.EmailClient;
import org.smm.archetype.domain.bizshared.client.dto.EmailRequest;
import org.smm.archetype.domain.bizshared.event.EventConsumeRecord;
import org.smm.archetype.domain.bizshared.event.EventType;

/**
 * 默认事件失败处理器
 *
 * <p>实现：
 * <ul>
 *   <li>记录告警日志</li>
 *   <li>发送邮件通知</li>
 *   <li>可扩展发送钉钉/企微通知</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultFailureHandler implements FailureHandler {

    /**
     * 默认告警邮箱地址（可通过配置文件覆盖）
     */
    private static final String DEFAULT_ALERT_EMAIL = "admin@example.com";

    /**
     * 邮件客户端
     */
    private final EmailClient emailClient;

    @Override
    public void handleFailure(DomainEvent event, EventConsumeRecord consumeRecord, Exception e) {
        log.error("""
                        !!! Event processing failed after max retries !!!
                        Event ID: {}
                        Event Type: {}
                        Aggregate ID: {}
                        Error Message: {}
                        Retry Times: {}
                        Consumer Group: {}
                        Consumer Name: {}
                        Occurred At: {}
                        """,
                event.getEventId(),
                event.getEventTypeName(),
                event.getAggregateId(),
                consumeRecord.getErrorMessage(),
                consumeRecord.getRetryTimes(),
                consumeRecord.getConsumerGroup(),
                consumeRecord.getConsumerName(),
                consumeRecord.getCreateTime(),
                e
        );

        // 发送告警通知
        sendAlert(event, consumeRecord);
    }

    @Override
    public boolean supports(EventType eventType) {
        // 默认支持所有事件类型
        return true;
    }

    @Override
    public int getPriority() {
        // 优先级较低，让业务特定的处理器先处理
        return 1000;
    }

    /**
     * 发送告警通知
     *
     * <p>实现邮件告警功能，可扩展其他通知方式（钉钉、企微等）。
     * @param event         领域事件
     * @param consumeRecord 消费记录值对象
     */
    private void sendAlert(DomainEvent event, EventConsumeRecord consumeRecord) {
        try {
            // 发送邮件告警
            sendEmailAlert(event, consumeRecord);

            // 可扩展：发送钉钉通知
            // sendDingTalkAlert(event, consumeRecord);

            // 可扩展：发送企业微信通知
            // sendWeChatAlert(event, consumeRecord);

            log.info("Alert sent for failed event: eventId={}", event.getEventId());
        } catch (Exception e) {
            // 告警发送失败不应该影响主流程
            log.error("Failed to send alert for event: eventId={}", event.getEventId(), e);
        }
    }

    /**
     * 发送邮件告警
     * @param event         领域事件
     * @param consumeRecord 消费记录值对象
     */
    private void sendEmailAlert(DomainEvent event, EventConsumeRecord consumeRecord) {
        String subject = buildAlertSubject(event);
        String htmlBody = buildAlertHtmlBody(event, consumeRecord);
        String textBody = buildAlertTextBody(event, consumeRecord);

        EmailRequest emailRequest = EmailRequest.builder()
                                            .setTo(getAlertEmail())
                                            .setSubject(subject)
                                            .setHtmlBody(htmlBody)
                                            .setTextBody(textBody)
                                            .build();

        emailClient.sendEmail(emailRequest);

        log.info("Email alert sent: eventId={}, to={}", event.getEventId(), getAlertEmail());
    }

    /**
     * 构建告警邮件主题
     * @param event 领域事件
     * @return 邮件主题
     */
    private String buildAlertSubject(DomainEvent event) {
        return String.format("[事件处理失败告警] EventId=%s, Type=%s",
                event.getEventId(),
                event.getEventTypeName());
    }

    /**
     * 构建告警邮件 HTML 内容
     * @param event         领域事件
     * @param consumeRecord 消费记录值对象
     * @return HTML 邮件内容
     */
    private String buildAlertHtmlBody(DomainEvent event, EventConsumeRecord consumeRecord) {
        return String.format("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background-color: #f44336; color: white; padding: 15px; text-align: center; }
                                .content { padding: 20px; border: 1px solid #ddd; }
                                .info { background-color: #f9f9f9; padding: 10px; margin: 10px 0; }
                                .error { background-color: #ffebee; padding: 10px; margin: 10px 0; border-left: 4px solid #f44336; }
                                .footer { margin-top: 20px; font-size: 12px; color: #999; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h2>⚠️ 事件处理失败告警</h2>
                                </div>
                                <div class="content">
                                    <h3>事件信息</h3>
                                    <div class="info">
                                        <p><strong>事件ID：</strong>%s</p>
                                        <p><strong>事件类型：</strong>%s</p>
                                        <p><strong>聚合根ID：</strong>%s</p>
                                        <p><strong>发生时间：</strong>%s</p>
                                    </div>
                        
                                    <h3>消费信息</h3>
                                    <div class="info">
                                        <p><strong>消费者组：</strong>%s</p>
                                        <p><strong>消费者名称：</strong>%s</p>
                                        <p><strong>重试次数：</strong>%s</p>
                                        <p><strong>创建时间：</strong>%s</p>
                                    </div>
                        
                                    <h3>错误信息</h3>
                                    <div class="error">
                                        <p>%s</p>
                                    </div>
                        
                                    <p style="color: #666; margin-top: 20px;">
                                        <strong>操作建议：</strong><br>
                                        1. 检查事件处理逻辑是否存在异常<br>
                                        2. 查看应用日志获取详细错误堆栈<br>
                                        3. 确认外部依赖服务是否正常<br>
                                        4. 必要时手动重试处理失败的事件
                                    </p>
                                </div>
                                <div class="footer">
                                    <p>此邮件由系统自动发送，请勿回复。</p>
                                    <p>发送时间：%s</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                event.getEventId(),
                event.getEventTypeName(),
                event.getAggregateId(),
                event.getOccurredOn(),
                consumeRecord.getConsumerGroup(),
                consumeRecord.getConsumerName(),
                consumeRecord.getRetryTimes(),
                consumeRecord.getCreateTime(),
                escapeHtml(consumeRecord.getErrorMessage()),
                java.time.Instant.now()
        );
    }

    /**
     * 构建告警邮件纯文本内容
     * @param event         领域事件
     * @param consumeRecord 消费记录值对象
     * @return 纯文本邮件内容
     */
    private String buildAlertTextBody(DomainEvent event, EventConsumeRecord consumeRecord) {
        return String.format("""
                        事件处理失败告警
                        ========================================
                        
                        事件信息：
                        - 事件ID：%s
                        - 事件类型：%s
                        - 聚合根ID：%s
                        - 发生时间：%s
                        
                        消费信息：
                        - 消费者组：%s
                        - 消费者名称：%s
                        - 重试次数：%s
                        - 创建时间：%s
                        
                        错误信息：
                        %s
                        
                        操作建议：
                        1. 检查事件处理逻辑是否存在异常
                        2. 查看应用日志获取详细错误堆栈
                        3. 确认外部依赖服务是否正常
                        4. 必要时手动重试处理失败的事件
                        
                        ========================================
                        发送时间：%s
                        """,
                event.getEventId(),
                event.getEventTypeName(),
                event.getAggregateId(),
                event.getOccurredOn(),
                consumeRecord.getConsumerGroup(),
                consumeRecord.getConsumerName(),
                consumeRecord.getRetryTimes(),
                consumeRecord.getCreateTime(),
                consumeRecord.getErrorMessage(),
                java.time.Instant.now()
        );
    }

    /**
     * 转义 HTML 特殊字符
     * @param text 原始文本
     * @return 转义后的文本
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                       .replace("<", "&lt;")
                       .replace(">", "&gt;")
                       .replace("\"", "&quot;")
                       .replace("'", "&#39;");
    }

    /**
     * 获取告警邮箱地址
     *
     * <p>可从配置文件读取，或使用系统环境变量。
     * @return 告警邮箱地址
     */
    private String getAlertEmail() {
        // TODO: 从配置文件读取
        // 可以通过 @Value 注入配置
        return DEFAULT_ALERT_EMAIL;
    }

}
