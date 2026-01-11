package support;

import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;

/**
 * 集成测试基类 - 启动完整Spring上下文
 *
 * <p>特点：
 * <ul>
 *   <li>✅ 启动完整的Spring上下文</li>
 *   <li>✅ 使用H2内存数据库</li>
 *   <li>✅ MyBatis-Flex自动建表</li>
 *   <li>✅ 支持DBUnit数据预加载</li>
 *   <li>✅ 事务回滚保证隔离性</li>
 * </ul>
 */
@SpringBootTest(classes = org.smm.archetype.test.TestBootstrap.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@TestPropertySource(locations = "classpath:config/application-integration.yaml")
@Transactional // 每个测试方法后自动回滚事务
@DbUnitConfiguration(databaseConnection = "dataSource")
public abstract class ITestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected DataSource dataSource;

    /**
     * 子类重写：指定数据集文件
     */
    protected String getDataSetFile() {
        return null;
    }

    /**
     * 在每个测试方法前加载测试数据
     */
    @BeforeEach
    void setUpTestData() throws Exception {
        if (shouldLoadTestData()) {
            loadDataSet(getDataSetFile());
        }
    }

    protected boolean shouldLoadTestData() {
        return getDataSetFile() != null;
    }

    /**
     * 加载DBUnit数据集
     */
    protected void loadDataSet(String dataSetFile) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            IDatabaseConnection dbConnection = new DatabaseConnection(connection);
            IDataSet dataSet = loadDataSetFromFile(dataSetFile);
            // 使用CLEAN_INSERT：先清空表，再插入数据
            DatabaseOperation.CLEAN_INSERT.execute(dbConnection, dataSet);
        }
    }

    /**
     * 从文件加载数据集
     */
    protected IDataSet loadDataSetFromFile(String fileName) throws Exception {
        try (InputStream inputStream = new ClassPathResource("datasets/integration/" + fileName).getInputStream()) {
            return new FlatXmlDataSetBuilder()
                           .setColumnSensing(true)
                           .build(inputStream);
        }
    }

}
