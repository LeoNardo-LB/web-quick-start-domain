package org.smm.archetype.test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

/**
 * 测试启动类
 *
 * <p>用于集成测试，扫描实际的业务包
 *
 * <p>注意：springEventPublisher Bean由EventConfigure提供，无需在此重复定义
 */
@SpringBootApplication(
        scanBasePackages = "org.smm.archetype",
        exclude = {KafkaAutoConfiguration.class}
)
@MapperScan("org.smm.archetype.infrastructure.**.mapper")
public class TestBootstrap {

    static void main(String[] args) {
        org.springframework.boot.builder.SpringApplicationBuilder builder =
                new org.springframework.boot.builder.SpringApplicationBuilder(TestBootstrap.class)
                        .profiles("integration");
        builder.run(args);
    }

}
