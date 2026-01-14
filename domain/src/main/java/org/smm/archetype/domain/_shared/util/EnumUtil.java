package org.smm.archetype.domain._shared.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举工具类
 *
 * <p>提供枚举类型的安全转换功能，使用流式API避免重复传递枚举类型。
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
 * // 流式API：避免重复传递枚举类
 * EventStatus status = EnumUtil.of(EventStatus.class).fromName("CREATED");
 * EventStatus status = EnumUtil.of(EventStatus.class).fromString("created", EventStatus.CREATED);
 * EventStatus status = EnumUtil.of(EventStatus.class).fromOrdinal(0);
 * boolean valid = EnumUtil.of(EventStatus.class).isValid("CREATED");
 *
 * // 重复使用时更优雅
 * EnumWrapper<EventStatus> wrapper = EnumUtil.of(EventStatus.class);
 * EventStatus status1 = wrapper.fromName("CREATED");
 * EventStatus status2 = wrapper.fromName("PAID");
 * boolean valid = wrapper.isValid("CREATED");
 * }</pre>
 *
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
     * EnumWrapper 缓存，提高性能
     * Key: 枚举类
     * Value: EnumWrapper 实例
     */
    private static final Map<Class<?>, EnumWrapper<?>> WRAPPER_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取枚举包装器（流式API）
     *
     * <p>这是推荐的API，避免重复传递枚举类型。
     *
     * <p>使用示例：
     * <pre>{@code
     * EventStatus status = EnumUtil.of(EventStatus.class).fromName("CREATED");
     * EventStatus status = EnumUtil.of(EventStatus.class).fromString("created", EventStatus.CREATED);
     * }</pre>
     *
     * @param enumClass 枚举类
     * @param <T>       枚举类型
     * @return 枚举包装器
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumWrapper<T> of(Class<T> enumClass) {
        // 尝试从缓存获取
        EnumWrapper<?> cached = WRAPPER_CACHE.get(enumClass);
        if (cached != null) {
            return (EnumWrapper<T>) cached;
        }

        // 缓存未命中，创建新的 Wrapper 并缓存
        EnumWrapper<T> wrapper = new EnumWrapper<>(enumClass);
        WRAPPER_CACHE.put(enumClass, wrapper);
        return wrapper;
    }

    /**
     * 从枚举名称转换为枚举实例（包级私有，供 EnumWrapper 调用）
     *
     * @param name      枚举名称
     * @param enumClass 枚举类
     * @param <T>       枚举类型
     * @return 枚举实例
     */
    @SuppressWarnings("unchecked")
    static <T extends Enum<T>> T fromNameCached(String name, Class<T> enumClass) {
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
     * 清除枚举缓存
     *
     * <p>主要用于测试场景，清理缓存避免影响其他测试。
     */
    public static void clearCache() {
        ENUM_CACHE.clear();
        WRAPPER_CACHE.clear();
        log.debug("Enum cache cleared");
    }

    /**
     * 枚举包装类
     *
     * <p>提供流式API，避免重复传递枚举类型。
     *
     * <p>使用示例：
     * <pre>{@code
     * // 从枚举名称转换
     * EventStatus status = EnumUtil.of(EventStatus.class).fromName("CREATED");
     *
     * // 从字符串转换（带默认值）
     * EventStatus status = EnumUtil.of(EventStatus.class).fromString("created", EventStatus.CREATED);
     *
     * // 从字符串转换（严格模式）
     * EventStatus status = EnumUtil.of(EventStatus.class).fromStringStrict("CREATED");
     *
     * // 从ordinal值转换
     * EventStatus status = EnumUtil.of(EventStatus.class).fromOrdinal(0);
     *
     * // 验证枚举值是否有效
     * boolean valid = EnumUtil.of(EventStatus.class).isValid("CREATED");
     * }</pre>
     *
     * @param <T> 枚举类型
     * @author Leonardo
     * @since 2026/01/15
     */
    @Getter
    @Slf4j
    public static class EnumWrapper<T extends Enum<T>> {

        /**
         * -- GETTER --
         *  获取枚举类
         *
         * @return 枚举类
         */
        private final Class<T> enumClass;

        EnumWrapper(Class<T> enumClass) {
            this.enumClass = enumClass;
        }

        /**
         * 从字符串安全转换为枚举
         *
         * <p>标准化处理：将外部来源的字符串值转换为项目中定义的枚举类型。
         * 如果转换失败，返回指定的默认值。
         *
         * @param value        枚举字符串值（不区分大小写）
         * @param defaultValue 默认值
         * @return 枚举实例，转换失败返回默认值
         */
        public T fromString(String value, T defaultValue) {
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
         *
         * @param value 枚举字符串值（不区分大小写）
         * @return 枚举实例
         * @throws IllegalArgumentException 如果值无效
         */
        public T fromStringStrict(String value) {
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
         *
         * @param name 枚举名称
         * @return 枚举实例
         * @throws IllegalArgumentException 如果名称无效
         */
        public T fromName(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Enum name cannot be null or blank");
            }

            // 从 EnumUtil 的缓存获取
            return fromNameCached(name, enumClass);
        }

        /**
         * 从ordinal值转换为枚举
         *
         * <p>注意：使用ordinal不安全，不建议使用。
         * 如果枚举定义顺序改变，会导致错误。
         *
         * @param ordinal 序数值
         * @return 枚举实例
         * @throws IllegalArgumentException 如果ordinal超出范围
         */
        public T fromOrdinal(int ordinal) {
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
         *
         * @param value 枚举字符串值
         * @return true-有效，false-无效
         */
        public boolean isValid(String value) {
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
         * 获取枚举类的所有常量
         *
         * @return 枚举常量数组
         */
        public T[] getConstants() {
            return enumClass.getEnumConstants();
        }

    }

}
