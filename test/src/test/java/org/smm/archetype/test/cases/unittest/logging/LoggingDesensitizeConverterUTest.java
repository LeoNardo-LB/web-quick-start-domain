package org.smm.archetype.test.cases.unittest.logging;

import org.junit.jupiter.api.Test;
import org.smm.archetype.config.logging.DesensitizingConverter;
import org.smm.archetype.test.support.UnitTestBase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * DesensitizingConverter 单元测试
 * 测试敏感信息脱敏功能
 */
class LoggingDesensitizeConverterUTest extends UnitTestBase {

    private final DesensitizingConverter converter = new DesensitizingConverter();

    /**
     * 测试脱敏密码
     */
    @Test
    void testDesensitizePassword() {
        // Given
        String message = "User login: username=test&password=secret123";

        // When
        String result = converter.desensitizePassword(message);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("secret123"), "Result should not contain secret123: " + result);
        assertTrue(result.contains("password=***"), "Result should contain password=***: " + result);
    }

    /**
     * 测试脱敏 Token
     */
    @Test
    void testDesensitizeToken() {
        // Given
        String message = "Authorization: token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

        // When
        String result = converter.desensitizeToken(message);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"));
        assertTrue(result.contains("token=***"));
    }

    /**
     * 测试脱敏手机号
     */
    @Test
    void testDesensitizePhone() {
        // Given
        String message = "User phone: 13812345678";

        // When
        String result = converter.desensitizePhone(message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("138****5678"));
        assertFalse(result.contains("13812345678"));
    }

    /**
     * 测试脱敏身份证号
     */
    @Test
    void testDesensitizeIdCard() {
        // Given
        String message = "ID card: 11010519900307890X";

        // When
        String result = converter.desensitizeIdCard(message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("110****890X"));
        assertFalse(result.contains("11010519900307890X"));
    }

    /**
     * 测试脱敏银行卡号
     */
    @Test
    void testDesensitizeBankCard() {
        // Given
        String message = "Bank card: 6222021234567890123";

        // When
        String result = converter.desensitizeBankCard(message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("6222********0123"));
        assertFalse(result.contains("6222021234567890123"));
    }

    /**
     * 测试脱敏 IP 地址
     */
    @Test
    void testDesensitizeIP() {
        // Given
        String message = "Request from: 192.168.1.100";

        // When
        String result = converter.desensitizeIP(message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("192.***.100"));
        assertFalse(result.contains("192.168.1.100"));
    }

    /**
     * 测试脱敏邮箱
     */
    @Test
    void testDesensitizeEmail() {
        // Given
        String message = "Contact: user@example.com";

        // When
        String result = converter.desensitizeEmail(message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("***@example.com"));
        assertFalse(result.contains("user@example.com"));
    }

    /**
     * 测试脱敏多种敏感数据
     */
    @Test
    void testDesensitizeMultipleSensitiveData() {
        // Given

        // When - 按顺序调用所有脱敏方法（模拟 convert 方法的流程）
        String result = "User: phone=13812345678, email=test@example.com, password=secret";
        result = converter.desensitizePassword(result);
        result = converter.desensitizeToken(result);
        result = converter.desensitizePhone(result);
        result = converter.desensitizeIdCard(result);
        result = converter.desensitizeBankCard(result);
        result = converter.desensitizeIP(result);
        result = converter.desensitizeEmail(result);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("138****5678"));
        assertTrue(result.contains("***@example.com"));
        assertTrue(result.contains("password=***"));
        assertFalse(result.contains("secret"));
    }

    /**
     * 测试脱敏 null 消息
     */
    @Test
    void testDesensitizeNullMessage() {
        // Given
        String message = null;

        // When
        String result = converter.desensitizePassword(message);

        // Then
        assertNull(result);
    }

    /**
     * 测试脱敏空消息
     */
    @Test
    void testDesensitizeEmptyMessage() {
        // Given
        String message = "";

        // When
        String result = converter.desensitizePassword(message);

        // Then
        assertNotNull(result);
        assertEquals("", result);
    }

    /**
     * 测试脱敏长消息（超过 2048 字符）
     */
    @Test
    void testDesensitizeLongMessage() {
        // Given - 超过 2048 字符的消息
        String longMessage = "test".repeat(1000); // 4000 字符

        // When - 调用convert方法测试截断逻辑
        ch.qos.logback.classic.spi.ILoggingEvent mockEvent = mock(ch.qos.logback.classic.spi.ILoggingEvent.class);
        when(mockEvent.getFormattedMessage()).thenReturn(longMessage);
        String result = converter.convert(mockEvent);

        // Then
        assertNotNull(result);
        assertTrue(result.length() <= 2062, "Result should be truncated to max 2062 chars"); // 2048 + "...(truncated)" (14 chars) = 2062
        assertTrue(result.endsWith("...(truncated)"), "Result should end with ...(truncated)");
    }

    /**
     * 测试脱敏无敏感数据的消息
     */
    @Test
    void testDesensitizeNoSensitiveData() {
        // Given - 没有敏感数据的消息
        String message = "This is a normal message without sensitive data";

        // When
        String result = message;
        result = converter.desensitizePassword(result);
        result = converter.desensitizeToken(result);
        result = converter.desensitizePhone(result);
        result = converter.desensitizeIdCard(result);
        result = converter.desensitizeBankCard(result);
        result = converter.desensitizeIP(result);
        result = converter.desensitizeEmail(result);

        // Then
        assertNotNull(result);
        assertEquals(message, result);
    }

    /**
     * 测试脱敏 pwd 变体
     */
    @Test
    void testDesensitizePwdVariant() {
        // Given
        String message = "Login with pwd=mysecret";

        // When
        String result = converter.desensitizePassword(message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("pwd=***"));
        assertFalse(result.contains("mysecret"));
    }

    /**
     * 测试脱敏 passwd 变体
     */
    @Test
    void testDesensitizePasswdVariant() {
        // Given
        String message = "Config: passwd=changeme";

        // When
        String result = converter.desensitizePassword(message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("passwd=***"));
        assertFalse(result.contains("changeme"));
    }

    /**
     * 测试脱敏 access_token 变体
     */
    @Test
    void testDesensitizeAccessTokenVariant() {
        // Given
        String message = "access_token=mytoken123";

        // When
        String result = converter.desensitizeToken(message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("access_token=***"));
        assertFalse(result.contains("mytoken123"));
    }

    /**
     * 测试脱敏 refresh_token 变体
     */
    @Test
    void testDesensitizeRefreshTokenVariant() {
        // Given
        String message = "refresh_token=refreshtoken456";

        // When
        String result = converter.desensitizeToken(message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("refresh_token=***"));
        assertFalse(result.contains("refreshtoken456"));
    }

    /**
     * 测试所有脱敏方法的 null 消息处理（覆盖 null 返回分支）
     */
    @Test
    void testAllDesensitizeMethodsWithNull() {
        // When - 测试所有脱敏方法的 null 处理
        String passwordResult = converter.desensitizePassword(null);
        String tokenResult = converter.desensitizeToken(null);
        String phoneResult = converter.desensitizePhone(null);
        String idCardResult = converter.desensitizeIdCard(null);
        String bankCardResult = converter.desensitizeBankCard(null);
        String ipResult = converter.desensitizeIP(null);
        String emailResult = converter.desensitizeEmail(null);

        // Then - 所有方法应返回 null
        assertNull(passwordResult);
        assertNull(tokenResult);
        assertNull(phoneResult);
        assertNull(idCardResult);
        assertNull(bankCardResult);
        assertNull(ipResult);
        assertNull(emailResult);
    }

    /**
     * 测试 convert 方法处理 null 消息
     */
    @Test
    void testConvertWithNullMessage() {
        // Given
        ch.qos.logback.classic.spi.ILoggingEvent mockEvent = mock(ch.qos.logback.classic.spi.ILoggingEvent.class);
        when(mockEvent.getFormattedMessage()).thenReturn(null);

        // When
        String result = converter.convert(mockEvent);

        // Then
        assertNull(result);
    }

    /**
     * 测试 convert 方法处理空消息
     */
    @Test
    void testConvertWithEmptyMessage() {
        // Given
        ch.qos.logback.classic.spi.ILoggingEvent mockEvent = mock(ch.qos.logback.classic.spi.ILoggingEvent.class);
        when(mockEvent.getFormattedMessage()).thenReturn("");

        // When
        String result = converter.convert(mockEvent);

        // Then
        assertNotNull(result);
        assertEquals("", result);
    }

    /**
     * 测试脱敏方法异常处理（通过模拟异常触发 catch 块）
     * 注意：正则表达式操作通常不抛异常，所以此测试主要通过代码覆盖率验证异常处理路径存在
     */
    @Test
    void testDesensitizeExceptionHandling() {
        // Given - 正常消息，异常路径不会被触发（因为 Pattern 和 Matcher 操作通常不抛异常）
        String message = "password=secret123";

        // When - 验证正常脱敏流程不会抛出异常
        assertDoesNotThrow(() -> {
            converter.desensitizePassword(message);
            converter.desensitizeToken(message);
            converter.desensitizePhone(message);
            converter.desensitizeIdCard(message);
            converter.desensitizeBankCard(message);
            converter.desensitizeIP(message);
            converter.desensitizeEmail(message);
        });

        // Then - 所有方法应正常处理并返回脱敏后的结果
        String passwordResult = converter.desensitizePassword(message);
        assertNotNull(passwordResult);
        assertTrue(passwordResult.contains("password=***"));
    }

    /**
     * 测试 convert 方法异常处理
     * 通过测试确保 convert 方法中的 try-catch 块不会影响正常脱敏流程
     */
    @Test
    void testConvertExceptionHandling() {
        // Given - 包含多种敏感信息的消息
        String message = "User: phone=13812345678, email=test@example.com, password=secret";
        ch.qos.logback.classic.spi.ILoggingEvent mockEvent = mock(ch.qos.logback.classic.spi.ILoggingEvent.class);
        when(mockEvent.getFormattedMessage()).thenReturn(message);

        // When
        String result = converter.convert(mockEvent);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("138****5678"));
        assertTrue(result.contains("***@example.com"));
        assertTrue(result.contains("password=***"));
        assertFalse(result.contains("secret"));
    }

}
