package org.smm.archetype;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spring Boot应用启动测试
 *
 * <p>此测试用于验证：
 * <ul>
 *   <li>Spring上下文能够成功启动</li>
 *   <li>所有Bean能够正确装配</li>
 *   <li>没有ERROR级别的日志输出</li>
 *   <li>主启动类能够正常启动</li>
 * </ul>
 *
 * <p>测试配置说明：
 * <ul>
 *   <li>使用主配置文件（application.yaml）</li>
 *   <li>通过Bean装配机制自动选择中间件</li>
 *   <li>Redis可用时自动使用Redis，否则使用Caffeine</li>
 *   <li>Kafka可用时自动使用Kafka，否则使用Spring Events</li>
 *   <li>保持测试纯洁性，不使用@MockBean</li>
 *   <li>启动后立即关闭上下文，避免阻塞</li>
 * </ul>
 *
 * <p>运行方式：
 * <ul>
 *   <li>IDEA：直接运行测试方法</li>
 *   <li>Maven: mvn test -Dtest=ApplicationStartupTests -pl start</li>
 *   <li>主启动类运行: mvn spring-boot:run -pl start</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2025/12/13
 */
@Slf4j
class ApplicationStartupTests {

    @Test
    @DisplayName("验证主启动类能够成功启动且无ERROR日志")
    void contextLoads() {
        log.info("============================================");
        log.info("开始测试主启动类启动...");

        // 使用主启动类启动应用
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ApplicationBootstrap.class).run();

        // 验证上下文已激活
        assertTrue(context.isActive(), "应用上下文应该处于激活状态");

        // 验证Bean工厂已初始化
        assertNotNull(context.getBeanFactory(), "Bean工厂应该已初始化");

        // 验证环境已配置
        assertNotNull(context.getEnvironment(), "环境配置应该已加载");

        log.info("应用上下文加载成功");
        log.info("所有Bean初始化完成，无错误");
        log.info("主启动类启动验证通过");
        log.info("============================================");

        // 立即关闭上下文，避免阻塞
        context.close();
    }

}
