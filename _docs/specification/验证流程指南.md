# 代码验证流程

> **目标**：确保每次代码修改都能正常运行，不破坏现有功能

## 4步强制验证流程

这是项目**强制执行**的代码质量保障流程，**每次代码修改后必须执行**。

---

## 步骤1：编写单元测试（可选）

如果你的代码修改了业务逻辑，**必须先编写单元测试**。

### 1.1 单元测试示例

```java
@Test
@DisplayName("创建订单")
void testCreateOrder() {
    // Given
    String customerId = "CUST001";
    Money totalAmount = new Money(100.00, "CNY");

    // When
    OrderAggr order = OrderAggr.create(customerId, totalAmount);

    // Then
    assertThat(order.getCustomerId()).isEqualTo(customerId);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(order.getTotalAmount()).isEqualTo(totalAmount);
}
```

### 1.2 测试基类

- **UnitTestBase**：纯Mock测试，不启动Spring
- **ITestBase**：集成测试，启动Spring + H2数据库

参考：`test/README.md`（在项目根目录的 test 模块）

### 1.3 跳过条件

以下情况**可以跳过**此步骤：
- 修改注释、文档、配置文件
- 重构（不改变行为）
- Bug修复（现有测试覆盖）

---

## 步骤2：编译验证（必须）

```bash
mvn clean compile
```

### 验证目标

✅ **编译通过**：无语法错误
✅ **MapStruct生成**：Converter实现类已生成
✅ **无警告**：忽略JDK警告，关注业务代码警告

### 预期输出

```
[INFO] BUILD SUCCESS
[INFO] Total time: XX s
```

### 常见编译错误

#### 错误1：MapStruct转换器未生成

**症状**：
```
[ERROR] cannot find symbol: class XxxConverterImpl
```

**解决方案**：
```bash
# 重新生成MapStruct代码
mvn clean compile
```

#### 错误2：类型转换错误

**症状**：
```
Can't map property "XXX" from "YYY" to "ZZZ"
```

**解决方案**：使用`@Mapping`注解
```java
@Mapping(target = "fieldName", expression = "java(source.getField())")
```

#### 错误3：循环依赖

**症状**：
```
The dependencies of some of the beans form a cycle
```

**解决方案**：参考 [业务代码编写规范.md](业务代码编写规范.md) 2.6节

---

## 步骤3：执行单元测试（必须）

```bash
mvn test
```

### 验证目标

✅ **测试通过率100%**：`Failures: 0, Errors: 0`
✅ **无跳过测试**：`Skipped: 0`
✅ **测试数量正确**：`Tests run: 180+`

### 预期输出

```
Tests run: 180, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 测试失败处理

#### 场景1：新编写的测试失败

**原因**：业务逻辑错误或测试用例错误

**解决步骤**：
1. 检查业务逻辑是否正确
2. 检查测试用例是否合理
3. 修复后重新运行：`mvn test -Dtest=xxx`

#### 场景2：现有测试失败

**原因**：代码修改破坏了现有功能

**解决步骤**：
1. 回滚代码修改
2. 重新分析需求
3. 修改后重新运行测试

#### 场景3：Mock未生效

**症状**：Mock对象返回实际值而非期望值

**解决方案**：
```java
// 确保Mock设置在调用之前
when(repository.findById(any())).thenReturn(Optional.of(entity));

// 或者使用@MockBean（集成测试）
@MockBean
private OrderRepository orderRepository;
```

### 代码覆盖率

查看覆盖率报告：
```bash
mvn verify -pl test
# 报告位置：test/target/site/jacoco/index.html
```

**覆盖率要求**：
- 行覆盖率 ≥ 95%
- 分支覆盖率 = 100%

---

## 步骤4：启动验证（最关键）⭐

```bash
mvn test -Dtest=ApplicationStartupTests -pl test
```

### 验证目标

✅ **Spring上下文启动成功**
✅ **所有Bean正确加载**
✅ **无循环依赖**
✅ **无BeanCreationException**

### 预期输出

```
✅ EsClient Bean successfully loaded: DisabledEsClientImpl
✅ Application context started successfully
✅ SearchService Bean successfully loaded: SearchServiceImpl

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 启动失败处理

#### 错误1：BeanCreationException

**症状**：
```
org.springframework.beans.factory.BeanCreationException
```

**常见原因**：
1. 配置类缺少@Bean方法
2. 构造器注入循环依赖
3. 条件装配不满足

**解决方案**：
```bash
# 查看详细错误堆栈
mvn test -Dtest=ApplicationStartupTests -pl test -X

# 定位失败Bean
grep -r "BeanCreationException" target/surefire-reports/
```

#### 错误2：NoSuchBeanDefinitionException

**症状**：
```
No qualifying bean of type 'com.xxx.XxxService' available
```

**原因**：配置类缺少@Bean方法

**解决方案**：
```java
@Configuration
public class XxxConfigure {
    @Bean
    public XxxService xxxService(...) {
        return new XxxServiceImpl(...);
    }
}
```

#### 错误3：循环依赖

**症状**：
```
The dependencies of some of the beans in the application context form a cycle
```

**❌ 绝对禁止的解决方法**：
- 使用@Lazy注解
- 使用ObjectProvider延迟注入
- 使用ApplicationContext.getBean()依赖查找

**✅ 正确解决方法**：
- 重构代码、解耦依赖
- 跨配置类：使用构造器注入 + Optional
- 同配置类：使用@Bean方法参数注入

参考：[业务代码编写规范.md](业务代码编写规范.md) 2.6节

---

## 完整验证命令（一键执行）

```bash
# 1. 编译验证
mvn clean compile

# 2. 单元测试验证
mvn test

# 3. 启动验证（最关键）
mvn test -Dtest=ApplicationStartupTests -pl test

# 或者一键执行（推荐）
mvn clean compile test && mvn test -Dtest=ApplicationStartupTests -pl test
```

---

## 验证流程决策树

```
开始验证
    │
    ├─ 需要编写测试？
    │   ├─ 是 → 编写单元测试 → 继续下一步
    │   └─ 否 → 继续
    │
    ├─ mvn clean compile
    │   ├─ 成功 → 继续
    │   └─ 失败 → 修复编译错误 → 重新编译
    │
    ├─ mvn test
    │   ├─ 成功 → 继续
    │   └─ 失败 → 修复测试错误 → 重新测试
    │
    └─ mvn test -Dtest=ApplicationStartupTests -pl start ⭐
        ├─ 成功 → ✅ 验证通过
        └─ 失败 → 修复启动错误 → 重新验证
```

---

## 质量检查清单

在提交代码前，确保：

- [ ] ✅ 编译通过：`mvn clean compile`
- [ ] ✅ 单元测试通过：`mvn test`（100%通过率）
- [ ] ✅ 启动测试通过：`mvn test -Dtest=ApplicationStartupTests -pl test`
- [ ] ✅ 代码覆盖率达标：行≥95%，分支100%
- [ ] ✅ 无编译警告（忽略JDK警告）
- [ ] ✅ 符合编码规范：参考[业务代码编写规范.md](业务代码编写规范.md)

---

## 常见问题FAQ

### Q1: 可以跳过某个步骤吗？

**A**: **绝对不可以**。这是强制流程，确保代码质量。

**例外**：
- 只修改注释/文档：可以跳过步骤1（单元测试）
- 只修改配置文件：可以跳过步骤1（单元测试）

### Q2: 测试失败但代码看起来没问题？

**A**:
1. 检查是否修改了核心业务逻辑
2. 检查测试用例是否需要更新
3. 回滚代码，重新分析需求

### Q3: 启动测试失败但单元测试通过？

**A**: 说明存在Spring配置问题：
- 检查@Bean方法是否正确
- 检查依赖注入是否正确
- 检查是否存在循环依赖

### Q4: 如何只运行某个测试类？

**A**:
```bash
# 运行单个测试类
mvn test -Dtest=OrderAppServiceTest

# 运行单个测试方法
mvn test -Dtest=OrderAppServiceTest#testCreateOrder
```

### Q5: 验证流程需要多长时间？

**A**:
- 编译：30秒 - 1分钟
- 单元测试：1-2分钟
- 启动测试：10-20秒
- **总计**：约2-3分钟

---

## 附录：快速参考

### Maven命令速查

```bash
# 编译
mvn clean compile

# 测试
mvn test

# 启动测试
mvn test -Dtest=ApplicationStartupTests -pl test

# 覆盖率报告
mvn verify -pl test

# 生产环境运行
mvn spring-boot:run -pl start

# 打包
mvn clean package
```

### 验证失败快速索引

| 错误类型 | 诊断命令 | 解决方案 |
|---------|---------|---------|
| 编译失败 | `mvn clean compile -X` | 检查语法、MapStruct配置 |
| 测试失败 | `mvn test -Dtest=xxx` | 检查业务逻辑、Mock配置 |
| 启动失败 | `mvn test -Dtest=ApplicationStartupTests -pl test -X` | 检查Bean配置、循环依赖 |
| 覆盖率不足 | `mvn verify -pl test` | 查看报告：test/target/site/jacoco/index.html |

---

## 相关文档

- [业务代码编写规范.md](业务代码编写规范.md) - 编码标准
- `test/README.md` - 测试指南（在项目根目录的 test 模块）
- `CLAUDE.md` - AI开发指南（在项目根目录）
