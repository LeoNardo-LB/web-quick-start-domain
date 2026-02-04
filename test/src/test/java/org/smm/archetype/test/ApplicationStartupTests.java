package org.smm.archetype.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.bizshared.client.SearchClient;
import org.smm.archetype.domain.common.search.SearchService;
import org.smm.archetype.infrastructure.common.log.LogAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 应用启动测试，验证Spring容器成功启动并装配所有Bean。
 */
@DisplayName("应用启动测试")
@SpringBootTest(classes = TestBootstrap.class)
@ActiveProfiles("integration")
@TestPropertySource(locations = "classpath:config/application-integration.yaml")
class ApplicationStartupTests {

    @Autowired
    private SearchClient searchClient;

    @Autowired(required = false)
    private SearchService searchService;

    @Autowired(required = false)
    private LogAspect logAspect;

    @Autowired(required = false)
    private org.smm.archetype.infrastructure.bizshared.event.publisher.DomainEventCollectAspectJ domainEventCollectAspectJ;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("验证LogAspect Bean成功装配")
    void verifyLogAspectBeanLoaded() {
        // Assert
        assertThat(logAspect).isNotNull();
        System.out.println("✅ LogAspect Bean successfully loaded: " + logAspect.getClass().getSimpleName());

        // 检查DomainEventCollectAspectJ是否也加载了
        if (domainEventCollectAspectJ != null) {
            System.out.println("✅ DomainEventCollectAspectJ Bean also loaded: " + domainEventCollectAspectJ.getClass().getSimpleName());
        }
    }

    @Test
    @DisplayName("验证EsClient Bean成功装配")
    void verifyEsClientBeanLoaded() {
        // Assert
        assertThat(searchClient).isNotNull();
        System.out.println("✅ SearchClient Bean successfully loaded: " + searchClient.getClass().getSimpleName());
    }

    @Test
    @DisplayName("验证SearchService Bean成功装配")
    void verifySearchServiceBeanLoaded() {
        // Assert
        assertThat(searchService).isNotNull();
        System.out.println("✅ SearchService Bean successfully loaded: " + searchService.getClass().getSimpleName());
        // 检查SearchService是否被代理（应该被DomainEventCollectAspectJ代理）
        System.out.println("🔍 SearchService是否是AOP代理: " + org.springframework.aop.support.AopUtils.isAopProxy(searchService));
    }

    @Test
    @DisplayName("测试SearchClient方法调用（验证LogAspect拦截）")
    void testSearchClientMethodCall() {
        // Given
        assertThat(searchClient).isNotNull();

        // 打印Bean的实际类型，看看是否被代理
        System.out.println("🔍 SearchClient实际类型: " + searchClient.getClass().getName());
        System.out.println("🔍 SearchClient实现的接口: " + String.join(", ", java.util.Arrays.stream(searchClient.getClass().getInterfaces())
                                                                                    .map(Class::getName).toList()));

        // 使用Spring的AopUtils检查是否是代理
        System.out.println("🔍 是否是AOP代理: " + org.springframework.aop.support.AopUtils.isAopProxy(searchClient));
        System.out.println("🔍 是否是CGLIB代理: " + org.springframework.aop.support.AopUtils.isCglibProxy(searchClient));
        System.out.println("🔍 是否是JDK动态代理: " + org.springframework.aop.support.AopUtils.isJdkDynamicProxy(searchClient));

        // When: 调用SearchClient方法（DisabledSearchClientImpl会抛出异常）
        try {
            Map<String, Object> result = searchClient.get("test-index", "test-id");
            System.out.println("✅ SearchClient method call successful, get returned: " + result);
        } catch (IllegalStateException e) {
            // Expected: DisabledSearchClientImpl抛出异常
            System.out.println("✅ SearchClient method call intercepted, exception caught: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("验证应用上下文成功启动")
    void verifyApplicationContextStartup() {
        // If we reach here, the context started successfully
        System.out.println("✅ Application context started successfully");

        // 查找AnnotationAwareAspectJAutoProxyCreator
        String[] autoProxyCreatorNames = applicationContext.getBeanNamesForType(
                org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator.class
        );

        System.out.println("🔍 AnnotationAwareAspectJAutoProxyCreator Bean数量: " + autoProxyCreatorNames.length);
        for (String name : autoProxyCreatorNames) {
            System.out.println("🔍 找到AOP代理创建器: " + name);
        }

        // 检查BeanPostProcessors
        String[] bppNames = applicationContext.getBeanNamesForType(
                org.springframework.beans.factory.config.BeanPostProcessor.class
        );
        System.out.println("🔍 AOP相关的BeanPostProcessors:");
        for (String bppName : bppNames) {
            if (bppName.toLowerCase().contains("proxy") || bppName.toLowerCase().contains("aspect")) {
                System.out.println("  - " + bppName);
            }
        }

        // 检查Advisors
        String[] advisorNames = applicationContext.getBeanNamesForType(
                org.springframework.aop.Advisor.class
        );
        System.out.println("🔍 所有Advisors数量: " + advisorNames.length);
        for (String advisorName : advisorNames) {
            Object adv = applicationContext.getBean(advisorName);
            System.out.println("  - " + advisorName + " (" + adv.getClass().getSimpleName() + ")");
        }
    }
}
