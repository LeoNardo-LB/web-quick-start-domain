package org.smm.archetype.config.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 敏感信息脱敏日志编码器
 *
 * <p>包装 PatternLayoutEncoder，对日志消息进行脱敏处理。
 * 使用方式：在 logback-spring.xml 中作为 encoder 使用
 */
public class DesensitizingEncoder extends EncoderBase<ILoggingEvent> {

    private final DesensitizingLayout desensitizingLayout = new DesensitizingLayout();

    private Charset charset = StandardCharsets.UTF_8;

    @Override
    public byte[] encode(ILoggingEvent event) {
        String formattedMessage = desensitizingLayout.doLayout(event);
        if (formattedMessage == null) {
            return new byte[0];
        }
        return formattedMessage.getBytes(charset);
    }

    @Override
    public void start() {
        desensitizingLayout.start();
        super.start();
    }

    @Override
    public void stop() {
        desensitizingLayout.stop();
        super.stop();
    }

    /**
     * 设置字符集
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] footerBytes() {
        return null;
    }

}
