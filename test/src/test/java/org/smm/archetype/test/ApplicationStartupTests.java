package org.smm.archetype.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.bizshared.client.SearchClient;
import org.smm.archetype.domain.common.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 应用启动测试，验证Spring容器成功启动并装配所有Bean。
 */
@DisplayName("应用启动测试")
@SpringBootTest(classes = TestBootstrap.class)
@ActiveProfiles("integration")
@TestPropertySource(locations = "classpath:config/application-integration.yaml")
class ApplicationStartupTests {

    @Autowired(required = false)
    private SearchClient searchClient;

    @Autowired(required = false)
    private SearchService searchService;

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
    }

    @Test
    @DisplayName("验证应用上下文成功启动")
    void verifyApplicationContextStartup() {
        // If we reach here, the context started successfully
        System.out.println("✅ Application context started successfully");
    }
}
