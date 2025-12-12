package org.smm.archetype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;

@Slf4j
@RestController
@SpringBootApplication
public class ApplicationBootstrap implements CommandLineRunner {

    @Value("${server.port}")
    public String port;

    @Value("${server.servlet.context-path}")
    public String contextPath;

    @Value("${spring.application.name}")
    public String appName;

    @Value("${springdoc.swagger-ui.path}")
    public String openapi;

    public static void main(String[] args) {
        SpringApplication.run(ApplicationBootstrap.class, args);
    }

    @Override
    public void run(@Nullable String... args) {
        log.info("[{}]应用启动成功!", appName);
        log.info("本地URL地址: {}", String.format("http://127.0.0.1:%s%s", port, contextPath));
        log.info("测试API地址: {}", String.format("http://127.0.0.1:%s%s%s", port, contextPath, openapi));
    }

}