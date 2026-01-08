package org.smm.archetype;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/13
 */
@SpringBootTest
@Slf4j
@Disabled("Disabled until proper test configuration is available")
class DemoApplicationTests {

    @Test
    void contextLoads() {
        log.info("Application context loaded successfully");
        try {
            throw new RuntimeException("测试异常");
        } catch (RuntimeException e) {
            log.error("出现了测试的异常！！", e);
            e.printStackTrace();
        }
    }

}