package org.smm.archetype.domain.shared.client.dto;

import lombok.Getter;

/**
 * 云服务商枚举，支持阿里云、腾讯云、华为云。
 */
@Getter
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
     * 构造函数
     */
    ServiceProvider(String description) {
        this.description = description;
    }

    /**
     * 获取默认服务商（阿里云）
     * @return 默认服务商
     */
    public static ServiceProvider getDefault() {
        return ALIYUN;
    }

}
