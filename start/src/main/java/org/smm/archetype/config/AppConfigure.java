package org.smm.archetype.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * Application层配置类，启用事务管理并注册应用层Bean。
 */
@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class AppConfigure {

    /**
     * 配置事务管理器
     *
    用于声明式事务管理（@Transactional注解）。
     * @param dataSource 数据源
     * @return 事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 配置事务模板（编程式事务）
     *
    用于编程式事务管理，在不使用@Transactional注解的场景下使用。
     * @param transactionManager 事务管理器
     * @return 事务模板
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

}
