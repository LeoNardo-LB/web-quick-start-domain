package org.smm.archetype.test.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;

/**
 * 集成测试基类，启动完整Spring上下文并提供WebTestClient。
 * 注意：不使用 @Transactional，避免强制要求数据库连接。
 * 具体的集成测试类可以根据需要自行添加 @Transactional 注解。
 */
@SpringBootTest(classes = TestBootstrap.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {

    @LocalServerPort
    protected int port;

    protected WebTestClient webTestClient;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    /**
     * 初始化 WebTestClient
     */
    @BeforeEach
    void initWebTestClient() {
        this.webTestClient = WebTestClient
                                     .bindToServer()
                                     .baseUrl("http://localhost:" + port)
                                     .responseTimeout(Duration.ofSeconds(30))
                                     .build();
    }

}
