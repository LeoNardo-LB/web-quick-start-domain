package org.smm.archetype.infrastructure.bizshared.dal.generated;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.config.TableConfig;
import com.mybatisflex.codegen.dialect.JdbcTypeMapping;
import com.zaxxer.hikari.HikariDataSource;
import org.smm.archetype.infrastructure.bizshared.dal.BaseDO;
import org.smm.archetype.infrastructure.bizshared.dal.BaseDOFillListener;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * MybatisFlex 代码生成器，默认在当前目录下生成 repository 包，包含 Mapper与DO对象
 *
 * ⚠️ 重要提示：
 * 1. 生成器默认使用@Data注解，违反项目编码规范（代码编写规范.md 第1.3节）
 * 2. 生成代码后，必须手动将@Data替换为精确的Lombok注解：
 * - @Getter
 * - @Setter
 * - @Builder(setterPrefix = "set")
 * - @EqualsAndHashCode(callSuper = true)
 * - @ToString(callSuper = true)
 * 3. 已修改的DO类：EventConsumeDO, EventPublishDO, LogDO, FileMetadataDO, FileBusinessDO
 */
public class MybatisFlexCodegenTest {

    /**
     * 数据库协议
     */
    private static final String   DATABASE_URL = "jdbc:mysql://127.0.0.1/testdb?characterEncoding=utf-8&useInformationSchema=true";

    /**
     * 数据库用户名
     */
    private static final String   USERNAME     = "root";

    /**
     * 数据库密码
     */
    private static final String   PASSWORD     = "leonardo123";

    /**
     * 生成的代码的根包名
     */
    private static final String   PACKAGE      = "org.smm.archetype.infrastructure.bizshared.dal.generated";

    /**
     * 需要生成的表名
     */
    private static final String[] tables       = {"event_consume", "event_publish", "file_business", "file_metadata", "log"};

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
        String sourceDir = System.getProperty("user.dir") + "/src/test/java";
        globalConfig.setSourceDir(sourceDir);

        // 通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);
        JdbcTypeMapping.registerMapping(Timestamp.class, Instant.class);
        // 生成代码
        generator.generate();
    }

}