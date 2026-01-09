package org.smm.archetype;

import org.junit.jupiter.api.Test;
import org.smm.archetype.domain._shared.client.OssClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OssClient Bean 装配验证测试
 *
 * <p>验证 OssClient Bean 是否被正确创建并注入到 Spring 上下文中。
 */
@SpringBootTest
class OssClientBeanTest {

    @Autowired(required = false)
    private OssClient ossClient;

    @Test
    void ossServiceBean_ShouldExist() {
        assertThat(ossClient)
                .isNotNull();
        System.out.println("OssClient Bean 类名: " + ossClient.getClass().getName());
    }

}
