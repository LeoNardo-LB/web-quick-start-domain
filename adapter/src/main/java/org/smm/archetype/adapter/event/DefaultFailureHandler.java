package org.smm.archetype.adapter.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.client.EmailClient;
import org.smm.archetype.domain.shared.client.dto.EmailRequest;
import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.domain.shared.event.Type;
import org.smm.archetype.infrastructure.shared.event.persistence.EventConsumeRecord;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 默认事件失败处理器，记录告警日志并发送邮件通知。
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultFailureHandler implements FailureHandler {

    /**
     * 默认告警邮箱地址（可通过配置文件覆盖）
     */
    private static final String DEFAULT_ALERT_EMAIL = "admin@example.com";

    /**
     * HTML邮件模板路径
     */
    private static final String HTML_TEMPLATE_PATH = "templates/email/event-failure-alert.html";

    /**
     * 纯文本邮件模板路径
     */
    private static final String TEXT_TEMPLATE_PATH = "templates/email/event-failure-alert.txt";

    /**
     * 邮件客户端
     */
    private final EmailClient emailClient;

    @Override
    public void handleFailure(Event<?> event, EventConsumeRecord consumeRecord, Exception e) {
        String aggregateId = getAggregateId(event);
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
                event.getEid(),
                event.getType(),
                aggregateId,
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
    public boolean supports(Type domainEventType) {
        // 默认支持所有事件类型
        return true;
    }

    /**
     * 发送告警通知。
     * @param event 事件
     * @param consumeRecord 消费记录
     */
    private void sendAlert(Event<?> event, EventConsumeRecord consumeRecord) {
        try {
            // 发送邮件告警
            sendEmailAlert(event, consumeRecord);

            // 可扩展：发送钉钉通知
            // sendDingTalkAlert(event, consumeRecord);

            // 可扩展：发送企业微信通知
            // sendWeChatAlert(event, consumeRecord);

            log.info("已发送失败事件告警: eventId={}", event.getEid());
        } catch (Exception e) {
            // 告警发送失败不应该影响主流程
            log.error("发送事件告警失败: eventId={}", event.getEid(), e);
        }
    }

    /**
     * 发送邮件告警
     * @param event         事件
     * @param consumeRecord 消费记录值对象
     */
    private void sendEmailAlert(Event<?> event, EventConsumeRecord consumeRecord) {
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

        log.info("已发送邮件告警: eventId={}, to={}", event.getEid(), getAlertEmail());
    }

    /**
     * 构建告警邮件主题
     * @param event 事件
     * @return 邮件主题
     */
    private String buildAlertSubject(Event<?> event) {
        return String.format("[事件处理失败告警] EventId=%s, Type=%s",
                event.getEid(),
                event.getType());
    }

    /**
     * 构建告警邮件 HTML 内容
     * @param event         事件
     * @param consumeRecord 消费记录值对象
     * @return HTML 邮件内容
     */
    private String buildAlertHtmlBody(Event<?> event, EventConsumeRecord consumeRecord) {
        String template = loadTemplate(HTML_TEMPLATE_PATH);
        return replaceTemplatePlaceholders(template, event, consumeRecord, true);
    }

    /**
     * 构建告警邮件纯文本内容
     * @param event         事件
     * @param consumeRecord 消费记录值对象
     * @return 纯文本邮件内容
     */
    private String buildAlertTextBody(Event<?> event, EventConsumeRecord consumeRecord) {
        String template = loadTemplate(TEXT_TEMPLATE_PATH);
        return replaceTemplatePlaceholders(template, event, consumeRecord, false);
    }

    /**
     * 加载模板文件
     * @param templatePath 模板路径
     * @return 模板内容
     */
    private String loadTemplate(String templatePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Template not found: " + templatePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load template: " + templatePath, e);
        }
    }

    /**
     * 替换模板占位符
     * @param template          模板内容
     * @param event             事件
     * @param consumeRecord     消费记录值对象
     * @param escapeHtmlContent 是否转义HTML内容
     * @return 替换后的内容
     */
    private String replaceTemplatePlaceholders(String template, Event<?> event,
                                               EventConsumeRecord consumeRecord, boolean escapeHtmlContent) {
        String errorMessage = escapeHtmlContent
                                      ? escapeHtml(consumeRecord.getErrorMessage())
                                      : consumeRecord.getErrorMessage();

        return template
                       .replace("{{eventId}}", String.valueOf(event.getEid()))
                       .replace("{{eventType}}", String.valueOf(event.getType()))
                       .replace("{{aggregateId}}", String.valueOf(getAggregateId(event)))
                       .replace("{{occurredOn}}", String.valueOf(event.getOccurredOn()))
                       .replace("{{consumerGroup}}", String.valueOf(consumeRecord.getConsumerGroup()))
                       .replace("{{consumerName}}", String.valueOf(consumeRecord.getConsumerName()))
                       .replace("{{retryTimes}}", String.valueOf(consumeRecord.getRetryTimes()))
                       .replace("{{createTime}}", String.valueOf(consumeRecord.getCreateTime()))
                       .replace("{{errorMessage}}", errorMessage != null ? errorMessage : "")
                       .replace("{{sendTime}}", String.valueOf(java.time.Instant.now()));
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
     * 获取告警邮箱地址。
     * @return 告警邮箱地址
     */
    private String getAlertEmail() {
        // 可以通过 @Value 注入配置
        return DEFAULT_ALERT_EMAIL;
    }

    /**
     * 从事件中获取聚合根ID
     * @param event 事件
     * @return 聚合根ID，如果无法获取则返回 "unknown"
     */
    private String getAggregateId(Event<?> event) {
        if (event.getPayload() instanceof org.smm.archetype.domain.shared.event.dto.DomainEventDTO dto) {
            return dto.getAggregateId();
        }
        return "unknown";
    }

}
