package org.smm.archetype.domain._shared.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举工具类
 *
 * <p>提供枚举类型的安全转换功能，用于标准化外部来源的枚举值。
 *
 * <p>支持场景：
 * <ul>
 *   <li>数据库查询结果转换（DO对象 → Domain对象）</li>
 *   <li>HTTP接口参数转换</li>
 *   <li>第三方接口返回数据转换</li>
 *   <li>配置文件读取转换</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 字符串转枚举（带默认值）
 * EventStatus status = EnumUtil.fromString("CREATED", EventStatus.class, EventStatus.CREATED);
 *
 * // 字符串转枚举（异常模式）
 * EventPriority priority = EnumUtil.fromStringStrict("HIGH", EventPriority.class);
 *
 * // 数值转枚举
 * ConsumeStatus consumeStatus = EnumUtil.fromValue(1, ConsumeStatus.class);
 * }</pre>
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
public class EnumUtil {

    /**
     * 枚举缓存，提高性能
     * Key: 枚举类名 + 枚举值
     * Value: 枚举实例
     */
    private static final Map<String, Enum<?>> ENUM_CACHE = new ConcurrentHashMap<>();

    /**
     * 从字符串安全转换为枚举
     *
     * <p>标准化处理：将外部来源的字符串值转换为项目中定义的枚举类型。
     * 如果转换失败，返回指定的默认值。
     * @param value        枚举字符串值（不区分大小写）
     * @param enumClass    枚举类
     * @param defaultValue 默认值
     * @param <T>          枚举类型
     * @return 枚举实例，转换失败返回默认值
     */
    public static <T extends Enum<T>> T fromString(String value, Class<T> enumClass, T defaultValue) {
        if (value == null || value.isBlank()) {
            log.debug("Empty value for enum {}, using default: {}", enumClass.getSimpleName(), defaultValue);
            return defaultValue;
        }

        try {
            // 尝试直接转换
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid enum value '{}' for {}, using default: {}", value, enumClass.getSimpleName(), defaultValue);
            return defaultValue;
        }
    }

    /**
     * 从字符串严格转换为枚举
     *
     * <p>如果转换失败，抛出异常。
     * @param value     枚举字符串值（不区分大小写）
     * @param enumClass 枚举类
     * @param <T>       枚举类型
     * @return 枚举实例
     * @throws IllegalArgumentException 如果值无效
     */
    public static <T extends Enum<T>> T fromStringStrict(String value, Class<T> enumClass) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    String.format("Empty value for enum %s", enumClass.getSimpleName())
            );
        }

        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid enum value '%s' for %s, valid values are: %s",
                            value, enumClass.getSimpleName(), Arrays.toString(enumClass.getEnumConstants())),
                    e
            );
        }
    }

    /**
     * 从枚举名称转换为枚举实例（使用缓存）
     * @param name      枚举名称
     * @param enumClass 枚举类
     * @param <T>       枚举类型
     * @return 枚举实例
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T fromName(String name, Class<T> enumClass) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Enum name cannot be null or blank");
        }

        String cacheKey = enumClass.getName() + ":" + name;

        // 尝试从缓存获取
        Enum<?> cached = ENUM_CACHE.get(cacheKey);
        if (cached != null) {
            return (T) cached;
        }

        // 缓存未命中，进行转换并缓存
        try {
            T result = Enum.valueOf(enumClass, name);
            ENUM_CACHE.put(cacheKey, result);
            return result;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid enum name '%s' for %s", name, enumClass.getSimpleName()), e
            );
        }
    }

    /**
     * 从ordinal值转换为枚举
     *
     * <p>注意：使用ordinal不安全，不建议使用。
     * 如果枚举定义顺序改变，会导致错误。
     * @param ordinal   序数值
     * @param enumClass 枚举类
     * @param <T>       枚举类型
     * @return 枚举实例
     * @throws IllegalArgumentException 如果ordinal超出范围
     */
    public static <T extends Enum<T>> T fromOrdinal(int ordinal, Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= constants.length) {
            throw new IllegalArgumentException(
                    String.format("Invalid ordinal %d for enum %s, valid range is 0-%d",
                            ordinal, enumClass.getSimpleName(), constants.length - 1)
            );
        }
        return constants[ordinal];
    }

    /**
     * 检查枚举值是否有效
     * @param value     枚举字符串值
     * @param enumClass 枚举类
     * @param <T>       枚举类型
     * @return true-有效，false-无效
     */
    public static <T extends Enum<T>> boolean isValid(String value, Class<T> enumClass) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            Enum.valueOf(enumClass, value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 批量转换字符串数组为枚举数组
     * @param values       字符串数组
     * @param enumClass    枚举类
     * @param defaultValue 默认值（用于无效值）
     * @param <T>          枚举类型
     * @return 枚举数组
     */
    @SafeVarargs
    public static <T extends Enum<T>> T[] fromStringArray(
            String[] values,
            Class<T> enumClass,
            T... defaultValue) {

        @SuppressWarnings("unchecked")
        T[] result = (T[]) java.lang.reflect.Array.newInstance(enumClass, values.length);

        for (int i = 0; i < values.length; i++) {
            result[i] = fromString(values[i], enumClass, defaultValue[0]);
        }

        return result;
    }

    /**
     * 清除枚举缓存
     *
     * <p>主要用于测试场景，清理缓存避免影响其他测试。
     */
    public static void clearCache() {
        ENUM_CACHE.clear();
        log.debug("Enum cache cleared");
    }

}
