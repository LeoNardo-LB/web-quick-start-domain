package support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;

/**
 * 集成测试基类 - 启动完整Spring上下文
 *
 * <p>特点：
 * <ul>
 *   <li>✅ 启动完整的Spring上下文</li>
 *   <li>✅ 使用 Testcontainers + MySQL（完全隔离，与生产环境一致）</li>
 *   <li>✅ MyBatis-Flex自动建表</li>
 *   <li>✅ 事务回滚保证隔离性</li>
 *   <li>✅ 提供WebTestClient用于Web测试</li>
 * </ul>
 */
@SpringBootTest(classes = org.smm.archetype.test.TestBootstrap.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@TestPropertySource(locations = "classpath:config/application-integration.yaml")
@Transactional // 每个测试方法后自动回滚事务
public abstract class ITestBase {

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
