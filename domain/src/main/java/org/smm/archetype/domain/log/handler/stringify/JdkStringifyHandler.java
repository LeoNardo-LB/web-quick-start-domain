package org.smm.archetype.domain.log.handler.stringify;

import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * JDK默认字符串化处理器
 *
 * 实现基于JDK默认toString方法的对象字符串化处理器，支持处理数组和集合类型的对象。
 * 对于null值返回"null"字符串，对于数组和集合作递归处理，其他对象直接调用toString方法。
 */
@Component
public class JdkStringifyHandler implements StringifyHandler {

    /**
     * 获取字符串化类型
     *
     * 返回JDK字符串化类型，用于标识此处理器支持的字符串化方式。
     * @return 字符串化类型枚举值 StringifyType.JDK
     */
    @Override
    public StringifyType getStringifyType() {
        return StringifyType.JDK;
    }

    /**
     * 将对象转换为字符串
     *
     * 基于JDK默认toString方法将对象转换为字符串。对于null值返回"null"字符串，
     * 对于数组和集合类型递归处理每个元素，其他对象直接调用toString方法。
     * @param target 待转换的对象
     * @return 转换后的字符串
     */
    @Override
    public String stringify(Object target) {
        if (target == null) {
            return "null";
        }
        if (target instanceof Iterable<?> || target.getClass().isArray()) {
            List<String> outputStrings = new ArrayList<>();
            if (target.getClass().isArray()) {
                int length = Array.getLength(target);
                for (int i = 0; i < length; i++) {
                    outputStrings.add(this.stringify(Array.get(target, i)));
                }
            } else {
                assert target instanceof Iterable<?>;
                for (Object object : (Iterable<?>) target) {
                    outputStrings.add(this.stringify(object));
                }
            }
            StringJoiner joiner = new StringJoiner(",");
            outputStrings.forEach(joiner::add);
            return "[" + joiner + "]";
        }
        return target.toString();
    }

}
