## 0. 前置准备

- [x] 0.1 阅读 AGENTS.md 了解技能加载要求
- [x] 0.2 加载 tdd-workflow skill 了解 TDD 流程（`/tdd-workflow`）

## 1. 阶段 1 - 正常编码

- [x] 1.1 查看现有 OrderStatus 枚举定义
- [x] 1.2 查看现有 OrderAggr 聚合根结构
- [x] 1.3 在 OrderAggr 中添加 canTransitionTo 方法签名

## 2. 阶段 2 - 单元测试 (TDD)

- [x] 2.1 创建 OrderAggrStatusTransitionUTest 测试类
- [x] 2.2 编写 PENDING 状态流转测试用例
- [x] 2.3 编写 CONFIRMED 状态流转测试用例
- [x] 2.4 编写 PAID 状态流转测试用例
- [x] 2.5 编写 SHIPPED 状态流转测试用例
- [x] 2.6 编写 COMPLETED 终态测试用例
- [x] 2.7 编写 CANCELLED 终态测试用例
- [x] 2.8 编写相同状态不可流转测试用例
- [x] 2.9 实现 canTransitionTo 方法使所有测试通过
- [x] 2.10 运行单元测试验证 100% 通过

## 3. 阶段 3 - 集成测试

- [x] 3.1 确认无需集成测试（纯领域逻辑）

## 4. 阶段 4 - 覆盖率验证

- [x] 4.1 运行覆盖率检查（已由 Maven JaCoCo 自动执行）

## 5. 阶段 5 - 最终抽检

- [x] 5.1 执行随机抽检（脚本验证通过）
