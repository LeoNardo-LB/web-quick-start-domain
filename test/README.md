# Test模块 README

## 1. 模块概述

Test（测试模块）是项目的独立测试模块，包含所有单元测试和集成测试。

### 核心理念

- **独立测试模块**：测试代码与业务代码分离
- **双层测试架构**：单元测试（纯Mock）+ 集成测试（Spring + H2）
- **快速执行**：单元测试0毫秒启动，集成测试秒级启动
- **数据隔离**：使用DBUnit管理测试数据，事务回滚保证隔离性

### 关键特点

- 单元测试纯Mock，不启动Spring
- 集成测试启动Spring，使用H2内存数据库
- DBUnit管理测试数据
- 事务自动回滚
- 覆盖率要求：行≥95%，分支100%

### 架构定位

```
┌─────────────────────────────────────────┐
│         Test (测试模块) ★ 本模块        │
│  ┌──────────────────────────────────┐   │
│  │  support/                        │   │
│  │    ├─ UnitTestBase.java          │   │
│  │    └─ ITestBase.java             │   │
│  │  cases/                          │   │
│  │    ├─ unittest/                  │   │
│  │    └─ integrationtest/           │   │
│  │  datasets/                       │   │
│  │    └─ integration/               │   │
│  │         └─ *.xml (DBUnit数据集)   │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

---

## 2. 目录结构

```
test/
├── src/test/java/
│   ├── support/                      # 测试基类
│   │   ├── UnitTestBase.java          # 单元测试基类
│   │   └── ITestBase.java            # 集成测试基类
│   │
│   └── cases/                        # 测试用例
│       ├── unittest/                 # 单元测试
│       │   ├── domain/
│       │   │   └── _example/
│       │   │       └── order/
│       │   │           └── OrderAggrUTest.java
│       │   ├── app/
│       │   └── infrastructure/
│       │
│       └── integrationtest/          # 集成测试
│           ├── domain/
│           ├── app/
│           └── adapter/
│
├── src/test/resources/
│   ├── config/                       # 测试配置
│   │   └── application-integration.yaml
│   └── datasets/                     # DBUnit数据集
│       └── integration/
│           ├── OrderAggrTest.xml
│           └── BlogAggrTest.xml
│
└── README.md                          # 本文档
```

### 包结构说明

- **support/**：测试基类，提供测试基础设施
- **cases/unittest/**：单元测试，纯Mock，不启动Spring
- **cases/integrationtest/**：集成测试，启动Spring，使用H2数据库
- **datasets/**：DBUnit测试数据集

---

## 3. 核心职责与边界

### 3.1 核心职责

**单元测试**

- 测试纯业务逻辑
- 使用Mock模拟依赖
- 快速执行，无外部依赖
- 覆盖所有分支

**集成测试**

- 测试端到端流程
- 测试Spring集成
- 测试数据库操作
- 测试HTTP请求处理

**测试数据管理**

- DBUnit管理测试数据
- 数据隔离
- 事务回滚

### 3.2 能力边界

**✅ Test模块能做什么**

- 编写单元测试和集成测试
- 管理测试数据
- 验证代码质量
- 检查测试覆盖率

**❌ Test模块不能做什么**

- 修改业务代码
- 修改DDL（数据库表结构）
- 影响生产环境

---

## 4. 关键组件类型

### 4.1 单元测试（UnitTestBase）

**作用**

- 提供单元测试基础设施
- 纯Mock环境，不启动Spring
- 0毫秒启动时间

**职责**

- 提供Mock支持
- 初始化测试环境
- 隔离测试依赖

**编写要点**

1. 继承UnitTestBase
2. 使用@Mock注解模拟依赖
3. 使用when().thenReturn()配置Mock行为
4. 覆盖所有分支

**伪代码示例**

```java
class XxxAggrUTest extends UnitTestBase {

    @Mock
    private XxxRepository repository;

    @Test
    void testCreateXxx() {
        // Given（准备）
        String name = "Test";
        when(repository.existsByName(name)).thenReturn(false);

        // When（执行）
        XxxAggr aggr = XxxAggr.create(name);

        // Then（验证）
        assertEquals(name, aggr.getName());
        assertEquals(Status.CREATED, aggr.getStatus());
        assertTrue(aggr.hasUncommittedEvents());
    }

    @Test
    void testCreateXxx_WhenNameExists_ShouldThrowException() {
        // Given
        String name = "Test";
        when(repository.existsByName(name)).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            XxxAggr.create(name);
        });
    }

    @Test
    void testBusinessMethod_WhenConditionMet_ShouldSucceed() {
        // Given
        XxxAggr aggr = createTestAggr();

        // When
        Result result = aggr.businessMethod(params);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(Status.NEW_STATUS, aggr.getStatus());
    }

    // 私有辅助方法
    private XxxAggr createTestAggr() {
        return XxxAggr.create("Test");
    }
}
```

**边界**

- **不启动Spring**：纯Mock环境
- **不访问数据库**：所有依赖都是Mock
- **快速执行**：每个测试应该毫秒级完成

---

### 4.2 集成测试（ITestBase）

**作用**

- 提供集成测试基础设施
- 启动完整Spring上下文
- 使用H2内存数据库
- DBUnit管理测试数据

**职责**

- 启动Spring上下文
- 加载测试数据
- 提供MockMvc
- 事务自动回滚

**编写要点**

1. 继承ITestBase
2. 使用@Autowired注入Bean
3. 使用MockMvc测试HTTP请求
4. 实现getDataSetFile()指定测试数据
5. 每个测试独立（数据隔离）

**伪代码示例**

```java
class XxxControllerITest extends ITestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private XxxRepository repository;

    @Test
    void testCreateXxx() throws Exception {
        // Given
        String requestBody = """
            {
                "name": "Test",
                "quantity": 10
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/xxx")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.name").value("Test"))
            .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    void testGetXxxById() throws Exception {
        // Given - 数据集已预加载数据

        // When & Then
        mockMvc.perform(get("/api/xxx/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").isNotEmpty());
    }

    @Test
    void testUpdateXxx() throws Exception {
        // Given
        String requestBody = """
            {
                "name": "Updated Name"
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/xxx/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Override
    protected String getDataSetFile() {
        return "XxxTest.xml"; // 测试数据文件
    }
}
```

**边界**

- **启动Spring**：完整的Spring上下文
- **H2数据库**：内存数据库，测试后自动清理
- **事务回滚**：每个测试方法后自动回滚
- **数据隔离**：每个测试独立运行

---

### 4.3 测试数据管理（DBUnit）

**作用**

- 预加载测试数据
- 保证测试数据隔离
- 支持数据集复用

**职责**

- 使用XML格式定义测试数据
- 在测试前加载数据
- 测试后清理数据

**编写要点**

1. 创建数据集XML文件
2. 放在datasets/integration/目录
3. 使用Flat XML格式
4. 定义表名和字段值

**伪代码示例**

**数据集格式**：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
    <!-- 表名 -->
    xxx_table
        id="1"
        name="Test Name"
        status="CREATED"
        amount="1000.00"
        create_time="2026-01-13 10:00:00"
    />

    <!-- 多行数据 -->
    xxx_table
        id="2"
        name="Another Test"
        status="PAID"
        amount="2000.00"
    />

    <!-- 关联表 -->
    xxx_item_table
        id="1"
        xxx_id="1"
        item_name="Item 1"
        quantity="10"
    />
</dataset>
```

**在测试类中使用**：

```java
class XxxControllerITest extends ITestBase {

    @Test
    void testWithPreloadedData() throws Exception {
        // Given - 数据集已预加载
        // xxx_table中已有id=1的记录

        // When
        mockMvc.perform(get("/api/xxx/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("Test Name"));
    }

    @Override
    protected String getDataSetFile() {
        return "XxxTest.xml";
    }
}
```

**边界**

- **数据集位置**：放在datasets/integration/目录
- **文件格式**：Flat XML格式
- **数据隔离**：每个测试独立，互不影响
- **不能修改DDL**：DDL由MyBatis-Flex自动生成

---

## 5. 设计模式和原则

### 5.1 核心设计模式

**模板方法模式**

- UnitTestBase和ITestBase提供测试模板
- 子类实现具体测试逻辑

**Mock模式**

- 单元测试使用Mock模拟依赖
- 隔离外部依赖

**数据集模式**

- DBUnit管理测试数据
- 数据预加载和清理

### 5.2 测试原则

**单元测试原则**

- 快速执行（毫秒级）
- 完全隔离（无外部依赖）
- 覆盖所有分支
- 纯Mock环境

**集成测试原则**

- 端到端测试
- 真实环境模拟
- 数据隔离
- 事务回滚

**覆盖率原则**

- 行覆盖率≥95%
- 分支覆盖率100%
- 关键路径100%

---

## 6. 开发指南

### 6.1 编写单元测试

**步骤1：继承UnitTestBase**

```java
package cases.unittest.domain._example.blog;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import support.UnitTestBase;

class BlogAggrUTest extends UnitTestBase {
    // 测试方法
}
```

**步骤2：编写测试方法**

```java
@Test
void testCreateBlog() {
    // Given
    String title = "First Blog";
    String content = "Blog Content";

    // When
    BlogAggr blog = BlogAggr.create(title, content);

    // Then
    assertEquals(title, blog.getTitle());
    assertEquals(content, blog.getContent());
    assertEquals(BlogStatus.DRAFT, blog.getStatus());
}

@Test
void testPublishBlog_WhenBlogIsDraft_ShouldSucceed() {
    // Given
    BlogAggr blog = BlogAggr.create("Title", "Content");

    // When
    blog.publish();

    // Then
    assertEquals(BlogStatus.PUBLISHED, blog.getStatus());
}

@Test
void testPublishBlog_WhenBlogIsPublished_ShouldThrowException() {
    // Given
    BlogAggr blog = BlogAggr.create("Title", "Content");
    blog.publish(); // 已发布

    // When & Then
    assertThrows(IllegalStateException.class, () -> {
        blog.publish(); // 再次发布应该抛异常
    });
}
```

**步骤3：覆盖所有分支**

确保测试覆盖所有分支和边界情况。

---

### 6.2 编写集成测试

**步骤1：继承ITestBase**

```java
package cases.integrationtest.adapter._example.blog.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import support.ITestBase;

class BlogControllerITest extends ITestBase {
    // 测试方法
}
```

**步骤2：准备测试数据**

**数据集文件：BlogTest.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
    blog_table
        id="1"
        title="Test Blog"
        content="Test Content"
        status="DRAFT"
        create_time="2026-01-13 10:00:00"
    />
</dataset>
```

**步骤3：实现getDataSetFile()**

```java
@Override
private String getDataSetFile() {
    return "BlogTest.xml";
}
```

**步骤4：编写测试方法**

```java
@Test
void testGetBlogById() throws Exception {
    // Given - 数据集已预加载

    // When & Then
    mockMvc.perform(get("/api/blogs/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.title").value("Test Blog"));
}

@Test
void testCreateBlog() throws Exception {
    // Given
    String requestBody = """
        {
            "title": "New Blog",
            "content": "New Content"
        }
        """;

    // When & Then
    mockMvc.perform(post("/api/blogs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.title").value("New Blog"));
}
```

---

### 6.3 使用DBUnit管理数据

**步骤1：创建数据集文件**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
    table_name
        column1="value1"
        column2="value2"
    />
</dataset>
```

**步骤2：放在正确位置**

数据集文件放在：`src/test/resources/datasets/integration/`

**步骤3：在测试类中引用**

```java
@Override
private String getDataSetFile() {
    return "YourTest.xml";
}
```

---

## 7. 配置说明

### 测试配置文件

**application-integration.yaml**：`src/test/resources/config/application-integration.yaml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:

mybatis-flex:
  mapper-locations: classpath*/**/mapper/*.xml
  type-aliases-package: org.smm.archetype.infrastructure._shared.generated.repository.entity

logging:
  level:
    root: INFO
    org.smm.archetype: DEBUG
```

---

## 8. 常见问题FAQ

### Q1: 为什么测试代码在test模块？

**A**: 测试代码独立在test模块的原因：

1. **职责分离**：测试代码与业务代码分离
2. **避免包污染**：业务模块不包含测试代码
3. **独立部署**：测试代码不打包到生产环境
4. **便于管理**：所有测试集中管理

---

### Q2: 单元测试和集成测试的区别？

**A**:

| 特征        | 单元测试（UnitTestBase） | 集成测试（ITestBase） |
|-----------|--------------------|-----------------|
| 启动时间      | 0毫秒                | 秒级（2-5秒）        |
| Spring上下文 | 不启动                | 启动完整上下文         |
| 数据库       | 无                  | H2内存数据库         |
| Mock      | Mock所有依赖           | Mock最少依赖        |
| 事务        | 无事务                | 事务自动回滚          |
| 数据准备      | 直接构造对象             | DBUnit数据集       |
| 用途        | 纯业务逻辑测试            | 端到端测试           |

---

### Q3: 如何使用DBUnit管理数据？

**A**:

1. 创建数据集文件（XML格式）
2. 放在`datasets/integration/`目录
3. 在测试类中实现`getDataSetFile()`方法
4. 测试前自动加载数据

---

### Q4: 为什么不能修改DDL？

**A**: 测试不能修改DDL的原因：

1. **保持一致性**：DDL由MyBatis-Flex自动生成，不应手动修改
2. **避免冲突**：手动修改DDL会被生成代码覆盖
3. **可重复性**：测试应该基于固定的DDL结构

---

### Q5: 如何达到覆盖率要求？

**A**: 达到覆盖率要求的方法：

1. **单元测试**：覆盖所有业务逻辑分支
2. **边界测试**：测试边界值、异常情况
3. **集成测试**：覆盖端到端流程
4. **使用JaCoCo**：检查覆盖率报告

---

## 9. 相关文档

- [项目根README.md](../README.md) - 项目整体架构说明
- [业务代码编写规范.md](../业务代码编写规范.md) - 编码标准详细参考
- [测试代码编写规范.md](../测试代码编写规范.md) - 测试规范详细参考
- [domain/README.md](../domain/README.md) - 领域层开发指南

---

**文档版本**: v2.0 (概念指导版)
**最后更新**: 2026-01-13
**维护者**: Leonardo
