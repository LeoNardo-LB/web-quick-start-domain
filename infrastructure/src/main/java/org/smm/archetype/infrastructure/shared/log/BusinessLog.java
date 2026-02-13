package org.smm.archetype.infrastructure.shared.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 业务日志注解，标记需记录的方法，支持配置持久化和字符串化。
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessLog {

    /**
     * 业务名称
     * 标识当前业务操作的名称，用于日志记录和分类。
     */
    String value() default "";

}
