package org.smm.archetype.infrastructure.shared.dal.generated;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import org.smm.archetype.infrastructure.shared.dal.BaseDO;

import java.nio.file.Paths;
import java.util.Collections;

/**
 * MyBatis Plus 代码生成器，在当前目录生成 Mapper 和 DO 对象。
 *
 * <p>功能与原 MyBatis-Flex 代码生成器完全一致：
 * <ul>
 *   <li>生成 DO 实体类（继承 BaseDO，使用 Lombok）</li>
 *   <li>生成 Mapper 接口（继承 BaseMapper）</li>
 *   <li>自动配置 @TableName、@TableId、@TableField 注解</li>
 *   <li>支持 @TableLogic 逻辑删除</li>
 *   <li>支持 @TableField 自动填充</li>
 *   <li>不生成 Controller（遵循 DDD 架构，Controller 在 Adapter 层）</li>
 * </ul>
 */
public class MybatisPlusGenerator {

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
    private static final String[] TABLES = {"event", "file_metadata", "file_business"};

    /**
     * 代码生成输出目录
     */
    private static final String SOURCE_DIR = Paths.get(System.getProperty("user.dir")) + "/infrastructure/src/main/java";

    /**
     * 生成代码
     */
    static void main(String[] args) {
        FastAutoGenerator.create(DATABASE_URL, USERNAME, PASSWORD)
                // ========== 全局配置 ==========
                .globalConfig(builder -> {
                    builder.author("CodeGenerator")
                            .outputDir(SOURCE_DIR)
                            .dateType(DateType.TIME_PACK)
                            .disableOpenDir();
                })
                // ========== 包配置 ==========
                .packageConfig(builder -> {
                    builder.parent(PACKAGE)
                            .entity("entity")
                            .mapper("mapper")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, ""));
                })
                // ========== 策略配置 ==========
                .strategyConfig(builder -> {
                    builder.addInclude(TABLES)
                            .addTablePrefix("")
                            // Entity 策略配置
                            .entityBuilder()
                            .superClass(BaseDO.class)
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel)
                            .enableLombok()
                            .enableTableFieldAnnotation()
                            .logicDeleteColumnName("delete_time")
                            .logicDeletePropertyName("deleteTime")
                            .addSuperEntityColumns("id", "create_time", "update_time",
                                    "create_user", "update_user")
                            .formatFileName("%sDO")
                            .idType(IdType.ASSIGN_ID)
                            .addTableFills(
                                    new Column("create_time", FieldFill.INSERT),
                                    new Column("update_time", FieldFill.INSERT_UPDATE),
                                    new Column("create_user", FieldFill.INSERT),
                                    new Column("update_user", FieldFill.INSERT_UPDATE)
                            )
                            .enableFileOverride()  // 启用 Entity 文件覆盖
                            // Mapper 策略配置
                            .mapperBuilder()
                            .superClass(com.baomidou.mybatisplus.core.mapper.BaseMapper.class)
                            .mapperAnnotation(org.apache.ibatis.annotations.Mapper.class).formatMapperFileName("%sMapper")
                            .enableFileOverride()  // 启用 Mapper 文件覆盖
                            // Controller 策略配置（禁用，遵循 DDD 架构）
                            .controllerBuilder()
                            .disable();
                })
                // ========== 模板引擎配置 ==========
                .templateEngine(new FreemarkerTemplateEngine())
                // ========== 执行生成 ==========
                .execute();

        System.out.println("代码生成完成！输出目录：" + SOURCE_DIR);
    }

}
