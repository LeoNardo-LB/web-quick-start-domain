package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 对象存储配置属性
 *
 * <p>支持本地文件系统和 RustFS 对象存储两种实现。
 * @author Leonardo
 * @since 2026/1/10
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "middleware.object-storage")
public class OssProperties {

    /**
     * 存储类型：local | rustfs
     */
    private String type = "local";

    // Getter 方法（Lombok可能未正常工作）
    /**
     * RustFS 配置
     */
    private RustFs rustfs = new RustFs();

    /**
     * 本地存储配置
     */
    private Local local = new Local();

    /**
     * RustFS 配置
     */
    @Getter
    @Setter
    public static class RustFs {

        /**
         * RustFS 服务器地址
         */
        private String endpoint = "http://localhost:9000";

        /**
         * Access Key
         */
        private String accessKey = "leonardo123";

        /**
         * Secret Key
         */
        private String secretKey = "leonardo123";

        /**
         * Bucket 名称
         */
        private String bucket = "default";

        /**
         * 连接超时（毫秒）
         */
        private Duration connectTimeout = Duration.ofSeconds(5);

        /**
         * 读取超时（毫秒）
         */
        private Duration readTimeout = Duration.ofSeconds(30);

    }

    /**
     * 本地存储配置
     */
    @Getter
    @Setter
    public static class Local {

        /**
         * 基础路径（可选，默认：用户文件夹/.project/${spring.application.name}/oss）
         */
        private String basePath;

        /**
         * 是否使用零拷贝（NIO transferTo/transferFrom）
         */
        private boolean zeroCopy = true;

    }

}
