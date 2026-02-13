package org.smm.archetype.config.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感信息脱敏日志格式化器
 *
 * <p>自动对日志消息中的敏感信息进行脱敏处理，包括：
 * <ul>
 *   <li>密码（password, pwd, passwd）</li>
 *   <li>Token（token, access_token, refresh_token）</li>
 *   <li>手机号（1[3-9]xxxxxxxxx）</li>
 *   <li>身份证号（18位）</li>
 *   <li>银行卡号（16-19位）</li>
 *   <li>IP地址（xxx.xxx.xxx.xxx）</li>
 *   <li>邮箱地址（user@domain.com）</li>
 * </ul>
 *
 * <p>使用方式：在 logback-spring.xml 中创建自定义的 pattern 并应用脱敏
 */
public class DesensitizingLayout extends LayoutBase<ILoggingEvent> {

    private static final Pattern PASSWORD_PATTERN  = Pattern.compile("(password|pwd|passwd)=\\S*");
    private static final Pattern TOKEN_PATTERN     = Pattern.compile("(token|access_token|refresh_token)=\\S*");
    private static final Pattern PHONE_PATTERN     = Pattern.compile("\\b1[3-9]\\d{9}\\b");
    private static final Pattern ID_CARD_PATTERN   = Pattern.compile(
            "\\b[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]\\b");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\b\\d{4}\\d{4}\\d{4}(\\d{3})?\\b");
    private static final Pattern IP_PATTERN        = Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");
    private static final Pattern EMAIL_PATTERN     = Pattern.compile("\\b[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}\\b");

    @Override
    public String doLayout(ILoggingEvent event) {
        if (event == null || event.getFormattedMessage() == null) {
            return null;
        }

        String formattedMessage = event.getFormattedMessage();

        // 应用脱敏规则
        formattedMessage = desensitizePassword(formattedMessage);
        formattedMessage = desensitizeToken(formattedMessage);
        formattedMessage = desensitizePhone(formattedMessage);
        formattedMessage = desensitizeIdCard(formattedMessage);
        formattedMessage = desensitizeBankCard(formattedMessage);
        formattedMessage = desensitizeIP(formattedMessage);
        formattedMessage = desensitizeEmail(formattedMessage);

        // 截断过长的日志（2048 字符限制）
        if (formattedMessage.length() > 2048) {
            formattedMessage = formattedMessage.substring(0, 2048) + "...(truncated)";
        }

        return formattedMessage;
    }

    /**
     * 脱敏密码
     */
    private String desensitizePassword(String message) {
        if (message == null)
            return null;
        Matcher matcher = PASSWORD_PATTERN.matcher(message);
        return matcher.replaceFirst("$1=***");
    }

    /**
     * 脱敏 Token
     */
    private String desensitizeToken(String message) {
        if (message == null)
            return null;
        Matcher matcher = TOKEN_PATTERN.matcher(message);
        return matcher.replaceFirst("$1=***");
    }

    /**
     * 脱敏手机号（格式：138****5678）
     */
    private String desensitizePhone(String message) {
        if (message == null)
            return null;
        Matcher matcher = PHONE_PATTERN.matcher(message);
        return matcher.replaceFirst("$1****$2");
    }

    /**
     * 脱敏身份证号（格式：110****1234）
     */
    private String desensitizeIdCard(String message) {
        if (message == null)
            return null;
        Matcher matcher = ID_CARD_PATTERN.matcher(message);
        return matcher.replaceFirst("$1****$2");
    }

    /**
     * 脱敏银行卡号（格式：6222********1234）
     */
    private String desensitizeBankCard(String message) {
        if (message == null)
            return null;
        Matcher matcher = BANK_CARD_PATTERN.matcher(message);
        return matcher.replaceFirst("$1********$2");
    }

    /**
     * 脱敏 IP 地址（格式：192.***.1）
     */
    private String desensitizeIP(String message) {
        if (message == null)
            return null;
        Matcher matcher = IP_PATTERN.matcher(message);
        return matcher.replaceFirst("$1.***.$2");
    }

    /**
     * 脱敏邮箱（格式：user***@example.com）
     */
    private String desensitizeEmail(String message) {
        if (message == null)
            return null;
        Matcher matcher = EMAIL_PATTERN.matcher(message);
        return matcher.replaceFirst("$1***@$2");
    }

}
