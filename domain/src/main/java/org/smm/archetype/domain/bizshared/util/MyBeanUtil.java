package org.smm.archetype.domain.bizshared.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean工具类，提供非空属性复制方法。
 */
public class MyBeanUtil {

    /**
     * 复制非空属性
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyNotNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullProperties(source));
    }

    /**
     * 获取source中为空的属性值（包含父类，Object以下）
     * @param source 源对象
     * @return 属性名称数组
     */
    private static String[] getNullProperties(Object source) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();

        List<String> nullPropertyNames = new ArrayList<>();
        for (PropertyDescriptor pd : propertyDescriptors) {
            String propertyName = pd.getName();
            // 排除class属性
            if (!"class".equals(propertyName)) {
                Object propertyValue = beanWrapper.getPropertyValue(propertyName);
                if (propertyValue == null) {
                    nullPropertyNames.add(propertyName);
                }
            }
        }
        return nullPropertyNames.toArray(new String[0]);
    }

}
