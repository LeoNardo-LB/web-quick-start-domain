# 业务代码生成(AI)流程

> **目标**：确保每次代码修改都能正常运行，不破坏现有功能，采用TDD（测试驱动开发）模式

## 核心原则

1. **TDD模式**：先写测试，再写代码，最后重构
2. **需求优先**：每次开发前必须反复询问用户，明确所有细节
3. **用例通过**：每个生成的测试用例必须通过才能继续
4. **强制验证**：5步验证流程，每步必须通过才能进入下一步
5. **文档对齐**：代码修改后，必须同步更新相关文档，保持文档与代码一致性

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
    │
Phase 5: 代码与文档对齐（文档同步）⭐
    │
    ├─ 识别改动的代码范围
    ├─ 识别涉及的文档
    ├─ 更新相关文档
    └─ 验证文档与代码一致性
```

---

## Phase 0：需求确认与TDD规划⭐

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

## Phase 5：代码与文档对齐（文档同步）⭐

### 5.1 识别改动的代码范围

**目标**：明确本次改动了哪些代码文件

**方法**：

- 使用 Git 查看改动的文件列表
- 识别新增、修改、删除的文件

**命令**：

```bash
# 查看改动的文件
git status

# 查看具体的改动
git diff --name-only

# 查看新增的文件
git diff --name-only --diff-filter=A

# 查看修改的文件
git diff --name-only --diff-filter=M
```

**输出示例**：

```
修改的文件：
M domain/src/main/java/org/smm/archetype/domain/_example/order/model/aggregateroot/OrderAggr.java
M app/src/main/java/org/smm/archetype/app/_example/order/OrderAppService.java

新增的文件：
A domain/src/main/java/org/smm/archetype/domain/_example/order/model/valueobject/OrderId.java
A adapter/src/main/java/org/smm/archetype/adapter/_example/order/web/api/OrderController.java
```

### 5.2 识别涉及的文档

**目标**：找到与改动代码相关的文档

**对齐规则**：

| 代码改动类型                          | 需要更新的文档                   | 更新内容           |
|---------------------------------|---------------------------|----------------|
| **新增实体/值对象/聚合根**                | `业务代码编写规范.md`（第3章：领域建模规范） | 添加新的实体/值对象说明   |
| **新增/修改接口（Controller/Service）** | `_docs/business/接口文档.md`  | 添加/更新 API 接口定义 |
| **新增/修改数据库表结构**                 | `_docs/business/数据设计.md`  | 添加/更新表结构、索引    |
| **新增/修改配置类（*Configure）**        | 相关架构文档                    | 添加配置说明、依赖关系    |
| **新增领域事件**                      | `_docs/business/实现方案.md`  | 添加事件定义、处理逻辑    |
| **新增/修改业务流程**                   | `_docs/business/实现方案.md`  | 添加流程图、关键点说明    |
| **新增/修改测试**                     | `测试代码编写与示例指南.md`          | 添加测试示例、场景说明    |

**检查清单**：

- [ ] 是否新增了领域模型（实体/值对象/聚合根）？
- [ ] 是否新增/修改了 API 接口？
- [ ] 是否新增/修改了数据库表结构？
- [ ] 是否新增/修改了配置类？
- [ ] 是否新增了领域事件？
- [ ] 是否有新的业务流程？
- [ ] 是否有新的测试用例/场景？

### 5.3 更新相关文档

**目标**：确保文档与代码保持一致

**更新流程**：

1. **定位文档章节**
    - 找到需要更新的文档
    - 找到对应的章节

2. **更新文档内容**
    - 根据代码改动更新文档
    - 保持文档格式一致
    - 更新版本号和更新时间

3. **验证文档完整性**
    - 检查描述是否准确
    - 检查示例代码是否正确
    - 检查链接是否有效

**更新示例**：

#### 示例1：新增领域模型

**代码改动**：新增 `OrderAggr.java`

**文档更新**：`业务代码编写规范.md` - 第3章：领域建模规范

```markdown
### 订单聚合根（OrderAggr）

**职责**：订单聚合根，管理订单生命周期

**字段**：
- `orderId` - 订单ID
- `customerId` - 客户ID
- `status` - 订单状态
- `totalAmount` - 订单总金额
- `items` - 订单项列表

**方法**：
- `create()` - 创建订单
- `pay()` - 支付订单
- `cancel()` - 取消订单

**示例**：
```java
OrderAggr order = OrderAggr.create("CUST001", new Money(100.00, "CNY"));
order.pay();
```

```

#### 示例2：新增 API 接口

**代码改动**：新增 `OrderController.createOrder()`

**文档更新**：`_docs/business/接口文档.md`

```markdown
### 创建订单

**接口**：POST /api/v1/order/create

**请求头**：
- `Authorization: Bearer {token}`

**请求体**：
```json
{
  "customerId": "CUST001",
  "items": [
    {
      "productId": "PROD001",
      "quantity": 2,
      "price": 100.00
    }
  ]
}
```

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": "ORD-20260121-0001",
    "orderNo": "2026012100001"
  }
}
```

**错误码**：

- 400: 参数错误
- 401: 未登录
- 404: 客户不存在

```

#### 示例3：新增数据库表

**代码改动**：新增 `order` 表

**文档更新**：`_docs/business/数据设计.md`

```sql
CREATE TABLE `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待支付，2-已支付，3-已取消',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_customer_id` (`customer_id`),
  INDEX `idx_order_no` (`order_no`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

**版本更新**：

- 文档版本：v1.1 → v1.2
- 最后更新：2026-02-03
- 变更记录：新增订单聚合根、创建订单接口、订单表

### 5.4 验证文档与代码一致性

**目标**：确保文档准确反映代码实现

**验证方法**：

1. **完整性检查**
    - 文档是否覆盖所有新增/修改的代码？
    - 示例代码是否能正常运行？
    - 参数、返回值是否与代码一致？

2. **准确性检查**
    - 描述是否准确？
    - 字段类型、约束是否正确？
    - 业务规则是否一致？

3. **可用性检查**
    - 文档是否易于理解？
    - 示例是否清晰？
    - 链接是否有效？

**验证清单**：

- [ ] 所有改动的代码都已在文档中体现
- [ ] 示例代码能够正常运行
- [ ] 参数、返回值与代码实现一致
- [ ] 文档描述清晰、准确
- [ ] 文档版本号已更新

**验证命令**：

```bash
# 检查文档中的代码示例是否有语法错误
# （需要手动执行）

# 检查文档中的链接是否有效
# （需要手动执行）

# 检查文档格式是否正确
# （需要手动执行）
```

**常见问题**：

**Q1: 文档更新很麻烦，可以跳过吗？**
**A**: **绝对不可以**。文档是项目的知识资产，必须保持与代码同步。

**Q2: 只改了一个文件，也需要更新文档吗？**
**A**: 如果这个改动涉及公开接口或业务逻辑，必须更新文档。如果是内部实现细节，可以不更新。

**Q3: 如何确保不遗漏文档更新？**
**A**: 使用 5.1 和 5.2 的检查清单，逐项确认。

**Q4: 文档更新后需要验证吗？**
**A**: 必须验证。使用 5.4 的验证方法，确保文档准确。

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

# Phase 5: 代码与文档对齐（文档同步）⭐
# 1. 识别改动的代码范围：git status
# 2. 识别涉及的文档：根据改动类型
# 3. 更新相关文档：保持同步
# 4. 验证文档一致性：检查准确性和完整性

# 或者一键执行（推荐）
mvn clean compile test && mvn test -Dtest=ApplicationStartupTests -pl test && git status
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
    ├─ Phase 4: 启动验证 ⭐
    │   ├─ mvn test -Dtest=ApplicationStartupTests -pl test
    │   ├─ 成功 → 进入Phase 5
    │   └─ 失败 → 修复启动错误 → 重新验证
    │
    └─ Phase 5: 代码与文档对齐 ⭐
        ├─ 识别改动的代码范围：git status
        ├─ 识别涉及的文档：根据改动类型
        ├─ 更新相关文档
        │   ├─ 需要更新？
        │   │   ├─ 是 → 更新文档 → 继续验证
        │   │   └─ 否 → 跳过 → 继续
        │   └─ 文档更新成功？
        │       ├─ 是 → 验证一致性
        │       └─ 否 → 重新更新
        ├─ 验证文档一致性
        └─ ✅ 完整流程通过
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

### Phase 5 检查清单

- [ ] ✅ 改动的代码范围已识别（git status）
- [ ] ✅ 涉及的文档已识别（根据代码改动类型）
- [ ] ✅ 相关文档已更新
- [ ] ✅ 文档版本号已更新
- [ ] ✅ 文档与代码一致性已验证

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

### Q9: Phase 5 文档对齐需要多长时间？

**A**:

- 简单改动（1-3个文件）：5-10分钟
- 中等改动（4-10个文件）：15-30分钟
- 复杂改动（10+个文件）：30-60分钟

**时间分布**：

- 5.1 识别改动范围：1-2分钟
- 5.2 识别涉及的文档：2-5分钟
- 5.3 更新相关文档：2-50分钟（取决于改动量）
- 5.4 验证文档一致性：5-10分钟

**重要性**：文档是项目的知识资产，必须保持与代码同步。

### Q10: 文档更新很麻烦，可以跳过吗？

**A**: **绝对不可以**。

**原因**：

1. 文档是项目的知识资产，后续开发依赖文档
2. 文档与代码不同步会导致：
    - 新成员上手困难
    - 重复沟通成本
    - 潜在的开发错误
3. 文档更新是开发流程的一部分，不是可选项

**例外**：

- ❌ 仅修改内部实现细节，不影响公开接口或业务逻辑
- ✅ 即使是内部实现，也应该考虑是否需要更新架构文档

**提示**：

- 使用 5.1 和 5.2 的检查清单，确保不遗漏
- 批量更新多个相关文档，提高效率
- 保持文档格式一致，减少更新时间

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

# Phase 5: 查看改动的文件
git status

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
| 文档与代码不一致   | Phase 5 | `git status` + 手动检查                                   | 识别改动 → 更新文档 → 验证一致性                     |

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

**文档版本**: v2.1（TDD版 + 文档对齐）
**最后更新**: 2026-02-03
**维护者**: Leonardo
