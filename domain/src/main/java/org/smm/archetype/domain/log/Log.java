package org.smm.archetype.domain.log;

import org.smm.archetype.domain.log.handler.persistence.PersistenceType;
import org.smm.archetype.domain.log.handler.stringify.StringifyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务日志注解
 *
 * 用于标记需要记录业务日志的方法，支持配置持久化方式和字符串化方式。
 * 通过AOP切面拦截带有此注解的方法，在方法执行前后记录相关信息。
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 业务名称
     *
     * 标识当前业务操作的名称，用于日志记录和分类。
     */
    String value() default "";

    /**
     * 持久化类型
     *
     * 指定日志的持久化方式，支持多种持久化类型组合。
     */
    PersistenceType[] persistence() default PersistenceType.FILE;

    /**
     * 默认持久化类型下转为字符串的形式
     *
     * 指定在默认持久化方式下，将对象转换为字符串的策略。
     */
    StringifyType stringify() default StringifyType.JDK;

}
