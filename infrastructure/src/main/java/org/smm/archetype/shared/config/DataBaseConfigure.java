package org.smm.archetype.shared.config;

import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@Configuration
public class DataBaseConfigure implements ConfigurationCustomizer {

    @Override
    public void customize(FlexConfiguration configuration) {
        configuration.setLogImpl(StdOutImpl.class);
    }

}
