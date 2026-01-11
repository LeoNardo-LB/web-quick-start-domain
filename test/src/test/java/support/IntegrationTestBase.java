package support;

import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 集成测试基类 - 启动最小化Spring上下文
 * 特点：
 * ✅ 启动完整的Spring上下文
 * ✅ 使用H2内存数据库
 * ✅ 支持DBUnit数据预加载
 * ✅ 事务回滚保证隔离性
 * ⚠️  启动时间 ~200-500ms
 *
 * 增强功能：
 * ✅ @BeforeTransaction强制清理数据库
 * ✅ cleanDatabase()方法供子类调用
 * ✅ 支持严格数据隔离场景
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@TestPropertySource(locations = "classpath:config/application-integration.yaml")
@Transactional // 每个测试方法后自动回滚事务
@DbUnitConfiguration(databaseConnection = "dataSource")
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc; // Web层测试入口

    @Autowired
    protected DataSource dataSource; // 数据库访问

    private String currentDataSetFile;

    /**
     * @BeforeTransaction - 事务开始前执行
     * 确保数据库干净，用于需要严格控制数据隔离的场景
     */
    @BeforeTransaction
    void cleanDatabaseBeforeTransaction() throws Exception {
        if (shouldLoadTestData()) {
            cleanDatabase();
        }
    }

    /**
     * 在每个测试方法前加载测试数据
     */
    @BeforeEach
    void setUpTestData() throws Exception {
        if (shouldLoadTestData()) {
            this.currentDataSetFile = getDataSetFile();
            loadDataSet(this.currentDataSetFile);
        }
    }

    /**
     * 在每个测试方法后清理
     */
    @AfterEach
    void cleanup() throws Exception {
        this.currentDataSetFile = null;
    }

    /**
     * 子类重写：指定数据集文件
     */
    protected String getDataSetFile() {
        return null;
    }

    protected boolean shouldLoadTestData() {
        return getDataSetFile() != null;
    }

    /**
     * 清理整个数据库 - 删除所有数据但保留表结构
     * 子类可在测试前调用此方法确保环境干净
     *
     * 使用场景：
     * - 需要严格控制数据隔离的测试
     * - 测试间有数据依赖问题
     * - 需要在测试中清理临时数据
     *
     * 示例：
     * <pre>
     * &#64;Test
     * void testWithCleanDatabase() throws Exception {
     *     cleanDatabase(); // 手动清理数据库
     *     // 执行测试逻辑
     * }
     * </pre>
     */
    protected void cleanDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            IDatabaseConnection dbConnection = new DatabaseConnection(connection);
            // DELETE_ALL: 删除所有数据，保留表结构
            DatabaseOperation.DELETE_ALL.execute(dbConnection, dbConnection.createDataSet());
        }
    }

    /**
     * 加载DBUnit数据集
     */
    protected void loadDataSet(String dataSetFile) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            IDatabaseConnection dbConnection = new DatabaseConnection(connection);
            IDataSet dataSet = loadDataSetFromFile(dataSetFile);
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

    /**
     * 验证数据库状态
     */
    protected void verifyDatabaseState(String expectedDataSetFile) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            IDatabaseConnection dbConnection = new DatabaseConnection(connection);
            IDataSet expectedDataSet = loadDataSetFromFile(expectedDataSetFile);
            IDataSet actualDataSet = dbConnection.createDataSet();
            org.dbunit.Assertion.assertEquals(expectedDataSet, actualDataSet);
        }
    }

    /**
     * 获取数据库连接
     */
    protected IDatabaseConnection getDatabaseConnection() throws SQLException {
        return new DatabaseConnection(dataSource.getConnection());
    }

}