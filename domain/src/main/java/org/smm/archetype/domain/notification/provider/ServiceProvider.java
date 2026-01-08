package org.smm.archetype.domain.notification.provider;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务商枚举
 *
 * <p>支持短信和邮件的多云服务商。
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum ServiceProvider {

    /**
     * 阿里云（默认服务商）
     */
    ALIYUN("阿里云"),

    /**
     * 腾讯云
     */
    TENCENT("腾讯云"),

    /**
     * 华为云
     */
    HUAWEI("华为云");

    /**
     * 服务商描述
     */
    private final String description;

    /**
     * 获取默认服务商（阿里云）
     * @return 默认服务商
     */
    public static ServiceProvider getDefault() {
        return ALIYUN;
    }

}
