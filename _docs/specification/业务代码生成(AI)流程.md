# 业务代码生成(AI)流程（TDD版）

> **目标**：确保每次代码修改都能正常运行，不破坏现有功能，采用TDD（测试驱动开发）模式

## 核心原则

1. **TDD模式**：先写测试，再写代码，最后重构
2. **需求优先**：每次开发前必须反复询问用户，明确所有细节
3. **用例通过**：每个生成的测试用例必须通过才能继续
4. **强制验证**：4步验证流程，每步必须通过才能进入下一步

---

## 📋 完整流程概览

```
Phase 0: 需求确认与TDD规划（新增）⭐
    │
    ├─ 反复询问用户，明确需求细节
    ├─ 编写测试用例（TDD红阶段）
    └─ 确认测试用例完整性
    │
Phase 1: 编写单元测试（TDD红阶段）
    │
    ├─ 编写测试用例（如果Phase 0未完成）
    └─ 验证测试用例失败（红）
    │
Phase 2: 编译验证
    │
    ├─ 生成MapStruct代码
    └─ 编译通过
    │
Phase 3: 执行单元测试（TDD绿阶段）
    │
    ├─ 实现业务逻辑
    ├─ 测试通过（绿）
    └─ 覆盖率达标
    │
Phase 4: 启动验证（最关键）⭐
    │
    ├─ Spring上下文启动成功
    └─ 无循环依赖
```

---

## Phase 0：需求确认与TDD规划（新增）⭐

### 0.1 反复询问用户，明确需求细节

**强制要求**：在开始任何代码编写之前，必须反复询问用户，了解每一个细节。

#### 需要确认的方面

**业务细节**：

- [ ] 功能的目标和用途是什么？
- [ ] 涉及哪些业务实体？
- [ ] 有哪些业务规则和约束？
- [ ] 异常情况如何处理？
- [ ] 边界条件是什么？

**技术细节**：

- [ ] 涉及哪些技术组件（数据库、缓存、消息队列等）？
- [ ] 有没有性能要求（并发、响应时间）？
- [ ] 是否需要事务控制？
- [ ] 数据持久化方案是什么？

**架构细节**：

- [ ] 这个功能应该放在哪个层（Domain/App/Adapter/Infra）？
- [ ] 需要定义哪些接口？
- [ ] 是否需要领域事件？
- [ ] 如何与其他模块交互？

**其他方面**：

- [ ] 有没有安全要求（权限、认证）？
- [ ] 日志记录的级别和内容？
- [ ] 是否需要监控指标？
- [ ] 有没有特殊格式要求（JSON、XML等）？

#### 询问方式示例

```
用户：我要实现一个创建订单的功能。

AI回复：
我需要了解一些细节才能开始实现：

1. **业务方面**：
   - 创建订单需要哪些字段？（客户ID、商品列表、金额等）
   - 订单创建后的初始状态是什么？（已创建、待支付等）
   - 是否需要验证客户是否存在？
   - 如果商品库存不足，应该如何处理？

2. **技术方面**：
   - 订单数据需要持久化到数据库吗？
   - 是否需要发送通知（短信、邮件）？
   - 是否需要事务控制？

3. **架构方面**：
   - 创建订单后是否需要发布领域事件？
   - 是否需要考虑与支付模块的集成？

请您提供更多细节，我会根据您的回答来实现功能。
```

**重要提醒**：

- **不要假设**：任何不确定的细节都要问清楚
- **多次询问**：一次问不清楚就分多次问
- **记录确认**：每次确认后记录下来，避免遗漏

### 0.2 编写测试用例（TDD红阶段）

**TDD原则**：在编写任何业务代码之前，先编写测试用例。

#### 测试用例清单

根据确认的需求细节，列出所有需要测试的场景：

**正常流程**：

- [ ] 场景1：xxx
- [ ] 场景2：xxx

**异常流程**：

- [ ] 场景3：xxx
- [ ] 场景4：xxx

**边界条件**：

- [ ] 场景5：xxx
- [ ] 场景6：xxx

#### 测试用例示例

```java
@Test
@DisplayName("创建订单 - 成功")
void testCreateOrder_success() {
    // Given
    String customerId = "CUST001";
    List<OrderItem> items = List.of(
        new OrderItem("PROD001", 2, new Money(100.00, "CNY")),
        new OrderItem("PROD002", 1, new Money(50.00, "CNY"))
    );

    // When
    OrderAggr order = OrderAggr.create(customerId, items);

    // Then
    assertThat(order.getId()).isNotNull();
    assertThat(order.getCustomerId()).isEqualTo(customerId);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(order.getTotalAmount()).isEqualTo(new Money(250.00, "CNY"));
}

@Test
@DisplayName("创建订单 - 客户ID为空")
void testCreateOrder_customerIdNull() {
    // Given
    String customerId = null;
    List<OrderItem> items = List.of();

    // When & Then
    assertThatThrownBy(() -> OrderAggr.create(customerId, items))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("客户ID不能为空");
}

@Test
@DisplayName("创建订单 - 商品列表为空")
void testCreateOrder_itemsEmpty() {
    // Given
    String customerId = "CUST001";
    List<OrderItem> items = List.of();

    // When & Then
    assertThatThrownBy(() -> OrderAggr.create(customerId, items))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("商品列表不能为空");
}
```

### 0.3 确认测试用例完整性

**检查清单**：

- [ ] 测试用例覆盖所有业务场景
- [ ] 测试用例覆盖所有异常情况
- [ ] 测试用例覆盖所有边界条件
- [ ] 测试用例命名清晰（Given-When-Then）
- [ ] 测试用例可独立运行

**用户确认**：

- 将测试用例清单展示给用户
- 询问：是否还有遗漏的场景？
- 确认无误后，才能进入Phase 1

---

## Phase 1：编写单元测试（TDD红阶段）

如果Phase 0已经完成测试用例编写，此步骤可跳过。

### 1.1 编写测试用例

**目标**：编写能够验证业务逻辑的测试用例。

### 1.2 运行测试，验证失败（红）

**TDD核心**：在编写业务代码之前，测试用例必须运行失败。

```bash
mvn test -Dtest=XxxTest
```

**预期结果**：

```
Tests run: 3, Failures: 3, Errors: 0, Skipped: 0
```

**原因**：因为业务代码还没有实现，所以测试必须失败。

**重要提醒**：

- ✅ 测试失败说明测试用例设计合理
- ❌ 如果测试通过，说明测试用例有问题
- ✅ 确保每个测试用例都失败后，才能进入Phase 2

---

## Phase 2：编译验证

### 2.1 编译项目

```bash
mvn clean compile
```

### 2.2 验证目标

✅ **编译通过**：无语法错误
✅ **MapStruct生成**：Converter实现类已生成
✅ **无警告**：忽略JDK警告，关注业务代码警告

### 2.3 预期输出

```
[INFO] BUILD SUCCESS
[INFO] Total time: XX s
```

### 2.4 常见编译错误

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

## Phase 3：执行单元测试（TDD绿阶段）

### 3.1 实现业务逻辑

根据Phase 0确认的需求和Phase 1编写的测试用例，实现业务逻辑。

### 3.2 运行测试，验证通过（绿）

```bash
mvn test -Dtest=XxxTest
```

### 3.3 验证目标

✅ **测试通过率100%**：`Failures: 0, Errors: 0`
✅ **无跳过测试**：`Skipped: 0`

### 3.4 预期输出

```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 3.5 测试失败处理

**强制要求**：**每个测试用例必须通过，才能进入Phase 4**

#### 场景1：测试失败 - 业务逻辑错误

**原因**：业务逻辑实现错误

**解决步骤**：

1. 检查业务逻辑是否符合需求
2. 修正业务逻辑
3. 重新运行测试：`mvn test -Dtest=xxx`
4. **必须所有测试通过才能继续**

#### 场景2：测试失败 - 测试用例错误

**原因**：测试用例设计错误

**解决步骤**：

1. 回到Phase 0，重新确认需求
2. 修改测试用例
3. 重新运行测试
4. **必须所有测试通过才能继续**

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

### 3.6 代码覆盖率

**强制要求**：代码覆盖率必须达标，才能进入Phase 4

查看覆盖率报告：
```bash
mvn verify -pl test
# 报告位置：test/target/site/jacoco/index.html
```

**覆盖率要求**：
- 行覆盖率 ≥ 95%
- 分支覆盖率 = 100%

**不足时的处理**：

1. 分析未覆盖的代码分支
2. 补充测试用例覆盖这些分支
3. 重新运行测试
4. **必须覆盖率达标才能继续**

---

## Phase 4：启动验证（最关键）⭐

### 4.1 运行启动测试

```bash
mvn test -Dtest=ApplicationStartupTests -pl test
```

### 4.2 验证目标

✅ **Spring上下文启动成功**
✅ **所有Bean正确加载**
✅ **无循环依赖**
✅ **无BeanCreationException**

### 4.3 预期输出

```
✅ EsClient Bean successfully loaded: DisabledEsClientImpl
✅ Application context started successfully
✅ SearchService Bean successfully loaded: SearchServiceImpl

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 4.4 启动失败处理

**强制要求**：启动测试必须通过，才能结束流程

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
        return new XxxServiceImpl(...)
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
# Phase 0: 需求确认与TDD规划（手动）
# 反复询问用户，明确需求细节
# 编写测试用例，确认测试用例失败（红）

# Phase 1: 编写单元测试（如果Phase 0未完成）
# 编写测试用例，确认测试用例失败（红）

# Phase 2: 编译验证
mvn clean compile

# Phase 3: 执行单元测试（实现业务逻辑，测试通过绿）
mvn test

# Phase 4: 启动验证（最关键）
mvn test -Dtest=ApplicationStartupTests -pl test

# 或者一键执行（推荐）
mvn clean compile test && mvn test -Dtest=ApplicationStartupTests -pl test
```

---

## TDD流程决策树

```
开始开发
    │
    ├─ Phase 0: 需求确认与TDD规划 ⭐
    │   ├─ 反复询问用户？
    │   │   ├─ 是 → 记录需求细节 → 继续下一步
    │   │   └─ 否 → 继续询问 → 直到确认所有细节
    │   │
    │   ├─ 编写测试用例？
    │   │   ├─ 是 → 继续下一步
    │   │   └─ 否 → 编写测试用例 → 继续下一步
    │   │
    │   └─ 用户确认测试用例？
    │       ├─ 是 → 进入Phase 1
    │       └─ 否 → 修改测试用例 → 重新确认
    │
    ├─ Phase 1: 编写单元测试（红）
    │   ├─ 测试失败（红）？
    │   │   ├─ 是 → 进入Phase 2
    │   │   └─ 否 → 修正测试用例 → 重新运行
    │
    ├─ Phase 2: 编译验证
    │   ├─ mvn clean compile
    │   ├─ 成功 → 进入Phase 3
    │   └─ 失败 → 修复编译错误 → 重新编译
    │
    ├─ Phase 3: 执行单元测试（绿）
    │   ├─ 实现业务逻辑
    │   ├─ mvn test
    │   ├─ 测试通过（绿）？
    │   │   ├─ 是 → 检查覆盖率
    │   │   │   ├─ 达标 → 进入Phase 4
    │   │   │   └─ 不达标 → 补充测试用例 → 重新测试
    │   │   └─ 否 → 修复业务逻辑 → 重新测试
    │
    └─ Phase 4: 启动验证 ⭐
        ├─ mvn test -Dtest=ApplicationStartupTests -pl test
        ├─ 成功 → ✅ 完整流程通过
        └─ 失败 → 修复启动错误 → 重新验证
```

---

## 质量检查清单

在提交代码前，确保：

### Phase 0 检查清单

- [ ] ✅ 用户需求细节已确认（业务、技术、架构等）
- [ ] ✅ 测试用例已编写完整（正常、异常、边界）
- [ ] ✅ 测试用例已通过用户确认
- [ ] ✅ 测试用例运行失败（红阶段）

### Phase 1 检查清单

- [ ] ✅ 测试用例已编写
- [ ] ✅ 测试用例运行失败（红阶段）

### Phase 2 检查清单
- [ ] ✅ 编译通过：`mvn clean compile`
- [ ] ✅ MapStruct代码已生成

### Phase 3 检查清单
- [ ] ✅ 单元测试通过：`mvn test`（100%通过率）
- [ ] ✅ 代码覆盖率达标：行≥95%，分支100%
- [ ] ✅ 每个测试用例都通过

### Phase 4 检查清单
- [ ] ✅ 启动测试通过：`mvn test -Dtest=ApplicationStartupTests -pl test`
- [ ] ✅ 无循环依赖
- [ ] ✅ 无编译警告（忽略JDK警告）
- [ ] ✅ 符合编码规范：参考[业务代码编写规范.md](业务代码编写规范.md)

---

## 常见问题FAQ

### Q1: 可以跳过某个步骤吗？

**A**: **绝对不可以**。这是强制流程，确保代码质量。

**例外**：

- 只修改注释/文档：可以跳过Phase 1和Phase 3（单元测试）
- 只修改配置文件：可以跳过Phase 1和Phase 3（单元测试）

**注意**：即使跳过Phase 1和Phase 3，Phase 0和Phase 4仍然必须执行。

### Q2: Phase 0 需要多长时间？

**A**:

- 简单功能：5-10分钟
- 复杂功能：15-30分钟
- **重要性**：Phase 0是整个流程的基础，宁可多花时间确认清楚，也不要匆忙开始。

### Q3: 测试失败但代码看起来没问题？

**A**:
1. 检查是否修改了核心业务逻辑
2. 检查测试用例是否需要更新
3. 回到Phase 0，重新确认需求
4. 修正测试用例或业务逻辑

### Q4: 启动测试失败但单元测试通过？

**A**: 说明存在Spring配置问题：
- 检查@Bean方法是否正确
- 检查依赖注入是否正确
- 检查是否存在循环依赖

### Q5: 如何只运行某个测试类？

**A**:
```bash
# 运行单个测试类
mvn test -Dtest=OrderAppServiceTest

# 运行单个测试方法
mvn test -Dtest=OrderAppServiceTest#testCreateOrder
```

### Q6: 验证流程需要多长时间？

**A**:

- Phase 0（需求确认）：5-30分钟（取决于功能复杂度）
- Phase 2（编译）：30秒 - 1分钟
- Phase 3（单元测试）：1-2分钟
- Phase 4（启动测试）：10-20秒
- **总计**：约5-35分钟（不含Phase 0）

### Q7: TDD真的有必要吗？

**A**: **非常有必要**。

**TDD的优势**：

1. **提高代码质量**：测试驱动确保代码符合需求
2. **减少Bug**：提前发现问题，降低修复成本
3. **改善设计**：编写测试用例的过程本身就是设计过程
4. **增强信心**：有完整的测试覆盖，重构更有信心
5. **文档作用**：测试用例就是最好的文档

**TDD的代价**：

- 前期投入时间较多
- 需要改变开发习惯

**结论**：对于本项目，TDD是强制要求，必须严格执行。

### Q8: 如果用户需求不明确怎么办？

**A**: 反复询问，直到明确。

**询问策略**：

1. 分阶段询问：先问大方向，再问细节
2. 举例说明：用具体例子引导用户思考
3. 重复确认：确认后再重复一遍
4. 提供选项：给出几个可能的方案供用户选择

**重要提醒**：

- ✅ 宁可多花时间确认，也不要假设
- ❌ 不要猜测用户的需求
- ✅ 确认后记录下来，作为参考

---

## 附录：快速参考

### Maven命令速查

```bash
# Phase 2: 编译
mvn clean compile

# Phase 3: 测试
mvn test

# Phase 4: 启动测试
mvn test -Dtest=ApplicationStartupTests -pl test

# 查看覆盖率报告
mvn verify -pl test

# 生产环境运行
mvn spring-boot:run -pl start

# 打包
mvn clean package
```

### 验证失败快速索引

| 错误类型       | Phase   | 诊断命令                                                  | 解决方案                                    |
|------------|---------|-------------------------------------------------------|-----------------------------------------|
| 需求不明确      | Phase 0 | 手动确认                                                  | 反复询问用户，明确需求                             |
| 测试通过（应该失败） | Phase 1 | `mvn test -Dtest=xxx`                                 | 修正测试用例                                  |
| 编译失败       | Phase 2 | `mvn clean compile -X`                                | 检查语法、MapStruct配置                        |
| 测试失败       | Phase 3 | `mvn test -Dtest=xxx`                                 | 检查业务逻辑、Mock配置                           |
| 覆盖率不足      | Phase 3 | `mvn verify -pl test`                                 | 查看报告：test/target/site/jacoco/index.html |
| 启动失败       | Phase 4 | `mvn test -Dtest=ApplicationStartupTests -pl test -X` | 检查Bean配置、循环依赖                           |

### TDD红-绿-重构循环

```
红（Red）：编写测试，运行失败
    ↓
绿（Green）：编写最小代码，使测试通过
    ↓
重构（Refactor）：优化代码，保持测试通过
    ↓
回到红（继续下一个功能）
```

---

## 相关文档

- [业务代码编写规范.md](业务代码编写规范.md) - 编码标准
- [测试代码编写与示例指南.md](测试代码编写与示例指南.md) - 测试规范和示例
- `test/README.md` - 测试指南（在项目根目录的 test 模块）
- `CLAUDE.md` - AI开发指南（在项目根目录）

---

**文档版本**: v2.0.0（TDD版）
**最后更新**: 2026-02-03
**维护者**: Leonardo
