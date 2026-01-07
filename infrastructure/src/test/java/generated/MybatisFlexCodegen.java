package generated;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.config.TableConfig;
import com.mybatisflex.codegen.dialect.JdbcTypeMapping;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.smm.archetype.infrastructure._shared.dal.entity.BaseDO;
import org.smm.archetype.infrastructure._shared.dal.listener.BaseDOFillListener;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * MybatisFlex 代码生成器，默认在当前目录下生成 repository 包，包含 Mapper与DO对象
 */
public class MybatisFlexCodegen {

    /**
     * 目录地址，精确到src地址
     */
    private static final String   DATABASE_URI  = "127.0.0.1:3306";
    private static final String   DATABASE_NAME = "testdb";
    private static final String   USERNAME      = "root";
    private static final String   PASSWORD      = "leonardo123";
    private static final String   PACKAGE       = "generated.repository";
    private static final String[] tables        = {"log", "event_consume", "event_publish"};

    public static GlobalConfig createGlobalConfigUseStyle1() {
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

    @Test
    public void generate() {
        // 配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        String jdbcUrlTemplate = "jdbc:mysql://%s/%s?characterEncoding=utf-8&useInformationSchema=true";
        dataSource.setJdbcUrl(jdbcUrlTemplate.formatted(DATABASE_URI, DATABASE_NAME));
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        // 创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfigUseStyle1();
        String sourceDir = System.getProperty("user.dir") + "/src/test/java";
        globalConfig.setSourceDir(sourceDir);

        // 通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);
        JdbcTypeMapping.registerMapping(Timestamp.class, Instant.class);
        // 生成代码
        generator.generate();
    }

}