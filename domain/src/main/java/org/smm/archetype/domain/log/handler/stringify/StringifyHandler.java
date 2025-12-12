package org.smm.archetype.domain.log.handler.stringify;

/**
 * 对象字符串化处理器接口
 *
 * 定义对象字符串化处理器的标准接口，不同的字符串化方式需要实现此接口。
 * 通过工厂模式根据字符串化类型获取对应的处理器实例。
 */
public interface StringifyHandler {

    /**
     * 获取字符串化类型
     *
     * 返回当前处理器支持的字符串化类型，用于在处理器工厂中建立类型与处理器的映射关系。
     * @return 字符串化类型枚举值
     */
    StringifyType getStringifyType();

    /**
     * 将对象转换为字符串
     *
     * 根据具体的字符串化策略，将传入的对象转换为可读的字符串格式。
     * @param target 待转换的对象
     * @return 转换后的字符串
     */
    String stringify(Object target);

}
