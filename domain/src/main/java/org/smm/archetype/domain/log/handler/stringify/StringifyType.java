package org.smm.archetype.domain.log.handler.stringify;

/**
 * 对象字符串化类型枚举
 *
 * 定义支持的对象字符串化方式，包括JDK默认的toString方法和JSON格式转换。
 */
public enum StringifyType {

    /**
     * JDK默认的toString方法
     *
     * 使用对象的toString()方法进行字符串化，适用于大多数Java对象。
     */
    JDK,

    /**
     * 转为JSON
     *
     * 将对象转换为JSON格式的字符串，便于阅读和解析。
     */
    JSON,
}
