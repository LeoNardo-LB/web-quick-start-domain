package org.smm.archetype.infrastructure.common.log;

import com.alibaba.fastjson2.JSON;
import org.smm.archetype.domain.common.log.handler.stringify.StringifyHandler;
import org.smm.archetype.domain.common.log.handler.stringify.StringifyType;
import org.springframework.stereotype.Component;

/**
 * JDK默认字符串化处理器
 *
 * 实现基于JDK默认toString方法的对象字符串化处理器，支持处理数组和集合类型的对象。
 * 对于null值返回"null"字符串，对于数组和集合作递归处理，其他对象直接调用toString方法。
 */
@Component
public class JsonStringifyHandler implements StringifyHandler {

    /**
     * 获取字符串化类型
     * 返回JDK字符串化类型，用于标识此处理器支持的字符串化方式。
     * @return 字符串化类型枚举值 StringifyType.JDK
     */
    @Override
    public StringifyType getStringifyType() {
        return StringifyType.JSON;
    }

    /**
     * 将对象转换为字符串
     * 基于JDK默认toString方法将对象转换为字符串。对于null值返回"null"字符串，
     * 对于数组和集合类型递归处理每个元素，其他对象直接调用toString方法。
     * @param target 待转换的对象
     * @return 转换后的字符串
     */
    @Override
    public String stringify(Object target) {
        return JSON.toJSONString(target);
    }

}
