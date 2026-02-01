package org.smm.archetype.test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 测试启动类，用于集成测试扫描业务包。
 */
@SpringBootApplication(scanBasePackages = "org.smm.archetype")
@MapperScan("org.smm.archetype.infrastructure.**.mapper")
public class TestBootstrap {

    static void main(String[] args) {
        org.springframework.boot.builder.SpringApplicationBuilder builder =
                new org.springframework.boot.builder.SpringApplicationBuilder(TestBootstrap.class)
                        .profiles("integration");
        builder.run(args);
    }

}
