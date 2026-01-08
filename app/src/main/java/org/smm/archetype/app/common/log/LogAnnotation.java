package org.smm.archetype.app.common.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 日志注解配置信息
 *
 * <p>封装@Log注解的配置信息。
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogAnnotation {

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 是否记录参数
     */
    private boolean logArgs;

    /**
     * 是否记录返回值
     */
    private boolean logResult;

    /**
     * 是否记录异常
     */
    private boolean logError;

}
