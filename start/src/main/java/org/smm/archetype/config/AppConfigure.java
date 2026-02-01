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
 * Application层统一配置类
 *
 * <p>职责：
 * <ul>
 *   <li>启用事务管理</li>
 *   <li>注册应用层Bean</li>
 *   <li>配置事务模板</li>
 * </ul>
 *
 * <p>依赖注入原则：
 * <ul>
 *   <li>跨配置类依赖：使用构造器注入</li>
 *   <li>同配置类依赖：使用@Bean方法参数注入</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/10
 */
@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class AppConfigure {

    /**
     * 配置事务管理器
     *
     * <p>用于声明式事务管理（@Transactional注解）。
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
     * <p>用于编程式事务管理，在不使用@Transactional注解的场景下使用。
     * @param transactionManager 事务管理器
     * @return 事务模板
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

}
