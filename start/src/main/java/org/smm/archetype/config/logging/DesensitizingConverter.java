package org.smm.archetype.config.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.base.Strings;

import java.util.regex.Pattern;

/**
 * 敏感信息脱敏转换器
 *
 * <p>作为 Logback ConversionWord 使用，对日志消息进行脱敏处理。
 * 在 logback-spring.xml 中使用：%msg 作为普通消息，%desensitizedMsg 作为脱敏后的消息
 */
public class DesensitizingConverter extends MessageConverter {

    private static final Pattern PASSWORD_PATTERN  = Pattern.compile("(password|pwd|passwd)=\\S*");
    private static final Pattern TOKEN_PATTERN     = Pattern.compile("(token|access_token|refresh_token)=\\S*");
    private static final Pattern PHONE_PATTERN     = Pattern.compile("(\\d{3})(\\d{4})(\\d{4})");
    private static final Pattern ID_CARD_PATTERN   = Pattern.compile("(\\d{3})(\\d{11})(\\d{3}[0-9Xx])");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})(\\d{8,12})(\\d{4})");
    private static final Pattern IP_PATTERN = Pattern.compile(
            "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
    private static final Pattern EMAIL_PATTERN     = Pattern.compile("([\\w.-]+)@([\\w.-]+)\\.([a-zA-Z]{2,})");

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        if (message == null) {
            return null;
        }

        // 按顺序脱敏各类敏感信息
        message = desensitizePassword(message);
        message = desensitizeToken(message);
        message = desensitizePhone(message);
        message = desensitizeIdCard(message);
        message = desensitizeBankCard(message);
        message = desensitizeIP(message);
        message = desensitizeEmail(message);

        // 截断过长的日志（2048 字符限制）
        if (message.length() > 2048) {
            message = message.substring(0, 2048) + "...(truncated)";
        }

        return message;
    }

    /**
     * 脱敏密码
     */
    public String desensitizePassword(String message) {
        if (message == null)
            return null;
        // 使用 Guava Strings.repeat() 优化脱敏逻辑
        return PASSWORD_PATTERN.matcher(message).replaceAll("$1=" + Strings.repeat("*", 3) + "\"");
    }

    /**
     * 脱敏 Token
     */
    public String desensitizeToken(String message) {
        if (message == null)
            return null;
        // 使用 Guava Strings.repeat() 优化脱敏逻辑
        return TOKEN_PATTERN.matcher(message).replaceAll("$1=" + Strings.repeat("*", 3) + "\"");
    }

    /**
     * 脱敏手机号（格式：138****5678）
     */
    public String desensitizePhone(String message) {
        if (message == null)
            return null;
        // 直接使用 replaceAll 和反向引用
        return PHONE_PATTERN.matcher(message).replaceAll("$1****$3");
    }

    /**
     * 脱敏身份证号（格式：110****1234）
     */
    public String desensitizeIdCard(String message) {
        if (message == null)
            return null;
        // 直接使用 replaceAll 和反向引用
        return ID_CARD_PATTERN.matcher(message).replaceAll("$1****$3");
    }

    /**
     * 脱敏银行卡号（格式：6222********1234）
     */
    public String desensitizeBankCard(String message) {
        if (message == null)
            return null;
        // 直接使用 replaceAll 和反向引用
        return BANK_CARD_PATTERN.matcher(message).replaceAll("$1********$3");
    }

    /**
     * 脱敏 IP 地址（格式：192.***.1）
     */
    public String desensitizeIP(String message) {
        if (message == null)
            return null;
        // 直接使用 replaceAll 和反向引用
        return IP_PATTERN.matcher(message).replaceAll("$1.***.$4");
    }

    /**
     * 脱敏邮箱（格式：user***@example.com）
     */
    public String desensitizeEmail(String message) {
        if (message == null)
            return null;
        // 直接使用 replaceAll 和反向引用
        return EMAIL_PATTERN.matcher(message).replaceAll("***@$2.$3");
    }

}
