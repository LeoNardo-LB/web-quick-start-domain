package org.smm.archetype.config;

import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import lombok.RequiredArgsConstructor;
import org.smm.archetype.config.properties.DatabaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
@RequiredArgsConstructor
public class DataBaseConfigure implements ConfigurationCustomizer {

    private final DatabaseProperties databaseProperties;

    @Override
    public void customize(FlexConfiguration configuration) {
        // 使用配置的日志实现类
        String logImplClassName = databaseProperties.getLogImplClassName();
        try {
            @SuppressWarnings("unchecked")
            Class<? extends org.apache.ibatis.logging.Log> logImplClass =
                    (Class<? extends org.apache.ibatis.logging.Log>) Class.forName(logImplClassName);
            configuration.setLogImpl(logImplClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Invalid log implementation: " + logImplClassName, e);
        }
    }

}
