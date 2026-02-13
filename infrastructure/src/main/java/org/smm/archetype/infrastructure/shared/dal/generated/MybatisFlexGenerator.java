package org.smm.archetype.infrastructure.shared.dal.generated;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.config.TableConfig;
import com.mybatisflex.codegen.dialect.JdbcTypeMapping;
import com.zaxxer.hikari.HikariDataSource;
import org.smm.archetype.infrastructure.shared.dal.BaseDO;
import org.smm.archetype.infrastructure.shared.dal.BaseDOFillListener;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * MyBatisFlex代码生成器，在当前目录生成Mapper和DO对象。


 */
public class MybatisFlexGenerator {

    /**
     * 数据库协议
     */
    private static final String DATABASE_URL = "jdbc:mysql://127.0.0.1/testdb?characterEncoding=utf-8&useInformationSchema=true";

    /**
     * 数据库用户名
     */
    private static final String USERNAME = "root";

    /**
     * 数据库密码
     */
    private static final String PASSWORD = "leonardo123";

    /**
     * 生成的代码的根包名
     */
    private static final String PACKAGE = "org.smm.archetype.infrastructure.shared.dal.generated";

    /**
     * 需要生成的表名
     */
    private static final String[] tables = {"event", "file_metadata", "file_business"};

    /**
     * 全局配置
     */
    public static GlobalConfig createGlobalConfig() {
        // 创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 设置根包
        globalConfig.setBasePackage(PACKAGE);

        // 设置表前缀和只生成哪些表
        // globalConfig.setTablePrefix("tb_");
        globalConfig.setGenerateTable(tables);

        // 设置生成 entity 并启用 Lombok
        globalConfig.setEntityGenerateEnable(true);
        globalConfig.setEntityWithLombok(true);
        globalConfig.setEntityOverwriteEnable(true);
        globalConfig.setMapperOverwriteEnable(true);
        globalConfig.setEntityClassSuffix("DO");
        globalConfig.setEntitySuperClass(BaseDO.class);
        TableConfig tableConfig = new TableConfig();
        tableConfig.setUpdateListenerClass(BaseDOFillListener.class);
        tableConfig.setInsertListenerClass(BaseDOFillListener.class);
        globalConfig.setTableConfig(tableConfig);

        // 覆盖
        globalConfig.setMapperOverwriteEnable(true);
        globalConfig.setEntityOverwriteEnable(true);
        globalConfig.setEntityBaseOverwriteEnable(true);

        // 设置项目的JDK版本，项目的JDK为14及以上时建议设置该项，小于14则可以不设置
        globalConfig.setEntityJdkVersion(25);

        // 设置生成 mapper
        globalConfig.setMapperGenerateEnable(true);
        globalConfig.enableEntity();

        return globalConfig;
    }

    /**
     * 生成代码
     */
    void main() {
        // 配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(DATABASE_URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        // 创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfig();
        String sourceDir = System.getProperty("user.dir") + "/infrastructure/src/main/java";
        globalConfig.setSourceDir(sourceDir);

        // 通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);
        JdbcTypeMapping.registerMapping(Timestamp.class, Instant.class);
        // 生成代码
        generator.generate();
    }

}