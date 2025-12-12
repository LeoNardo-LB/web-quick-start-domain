package org.smm.archetype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;

/**
 * Spring Boot应用启动类
 *
 * 作为整个应用程序的入口点，负责初始化Spring Boot应用上下文，
 * 启动Web服务器，并在应用启动完成后执行一些初始化操作。
 */
@Slf4j
@RestController
@SpringBootApplication
@EnableAspectJAutoProxy
public class ApplicationBootstrap implements CommandLineRunner {

    /**
     * 服务器端口
     *
     * 从配置文件中读取服务器监听的端口号。
     */
    @Value("${server.port}")
    public String port;

    /**
     * Servlet上下文路径
     *
     * 从配置文件中读取Servlet的上下文路径。
     */
    @Value("${server.servlet.context-path}")
    public String contextPath;

    /**
     * 应用名称
     *
     * 从配置文件中读取应用的名称。
     */
    @Value("${spring.application.name}")
    public String appName;

    /**
     * OpenAPI文档路径
     *
     * 从配置文件中读取OpenAPI(Swagger)文档的访问路径。
     */
    @Value("${springdoc.swagger-ui.path}")
    public String openapi;

    /**
     * 应用程序入口点
     *
     * 启动Spring Boot应用程序，初始化应用上下文并启动Web服务器。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ApplicationBootstrap.class, args);
    }

    /**
     * 应用启动完成后执行的回调方法
     *
     * 在Spring Boot应用完全启动后执行，用于打印应用启动成功的日志信息，
     * 包括应用名称、本地访问URL和测试API地址。
     * @param args 命令行参数
     */
    @Override
    public void run(@Nullable String... args) {
        log.info("[{}]应用启动成功!", appName);
        log.info("本地URL地址: {}", String.format("http://127.0.0.1:%s%s", port, contextPath));
        log.info("测试API地址: {}", String.format("http://127.0.0.1:%s%s%s", port, contextPath, openapi));
    }

}