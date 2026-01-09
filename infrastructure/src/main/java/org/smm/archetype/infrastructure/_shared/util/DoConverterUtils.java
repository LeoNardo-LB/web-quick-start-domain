package org.smm.archetype.infrastructure._shared.util;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.event.ConsumeStatus;
import org.smm.archetype.domain._shared.event.EventPriority;
import org.smm.archetype.domain._shared.event.EventStatus;
import org.smm.archetype.domain._shared.util.EnumUtils;

/**
 * 数据库DO对象转换工具类
 *
 * <p>提供DO对象到Domain对象的转换功能，特别关注枚举类型的标准化处理。
 *
 * <p>标准化规则：
 * <ul>
 *   <li>外部来源的所有枚举含义字段必须转换为项目中定义的枚举类型</li>
 *   <li>使用EnumUtils进行安全转换，提供合理的默认值</li>
 *   <li>记录转换失败的日志，便于问题排查</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
public class DoConverterUtils {

    /**
     * 转换事件状态字符串为枚举
     * @param status 状态字符串
     * @return EventStatus枚举，无效值返回CREATED
     */
    public static EventStatus convertEventStatus(String status) {
        return EnumUtils.fromString(status, EventStatus.class, EventStatus.CREATED);
    }

    /**
     * 转换消费状态字符串为枚举
     * @param status 状态字符串
     * @return ConsumeStatus枚举，无效值返回READY
     */
    public static ConsumeStatus convertConsumeStatus(String status) {
        return EnumUtils.fromString(status, ConsumeStatus.class, ConsumeStatus.READY);
    }

    /**
     * 转换事件优先级字符串为枚举
     * @param priority 优先级字符串
     * @return EventPriority枚举，无效值返回LOW
     */
    public static EventPriority convertEventPriority(String priority) {
        return EnumUtils.fromString(priority, EventPriority.class, EventPriority.LOW);
    }

    /**
     * 安全的Long值转换
     * @param value        字符串值
     * @param defaultValue 默认值
     * @return Long值
     */
    public static Long convertLong(String value, Long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid Long value: {}, using default: {}", value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 安全的Integer值转换
     * @param value        字符串值
     * @param defaultValue 默认值
     * @return Integer值
     */
    public static Integer convertInteger(String value, Integer defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid Integer value: {}, using default: {}", value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 安全的Boolean值转换
     * @param value        字符串值
     * @param defaultValue 默认值
     * @return Boolean值
     */
    public static Boolean convertBoolean(String value, Boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

}
