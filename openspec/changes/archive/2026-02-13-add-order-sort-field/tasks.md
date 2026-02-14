## 0. 前置准备

- [ ] 0.1 阅读 AGENTS.md 了解技能加载要求
- [ ] 0.2 使用 `/tdd-workflow` 命令加载 TDD 流程

## 1. Domain 层实现

- [ ] 1.1 在 OrderAggr 中添加 sortOrder 字段（Integer 类型）
- [ ] 1.2 修改 create 工厂方法，添加 sortOrder 参数（默认值 0）
- [ ] 1.3 添加 updateSortOrder 业务方法，仅允许 CREATED 状态修改
- [ ] 1.4 添加 getSortOrder 查询方法

## 2. 阶段 2：单元测试

- [ ] 2.1 创建 OrderAggrSortOrderUTest 单元测试类
- [ ] 2.2 测试默认 sortOrder 为 0
- [ ] 2.3 测试创建时指定 sortOrder
- [ ] 2.4 测试 CREATED 状态下修改 sortOrder
- [ ] 2.5 测试非 CREATED 状态下修改 sortOrder 抛出异常
- [ ] 2.6 运行 `tsx scripts/run-unit-tests.ts --diff HEAD~1` 验证通过

## 3. 阶段 3：集成测试

- [ ] 3.1 创建 OrderAggrSortOrderITest 集成测试类（如需要）
- [ ] 3.2 运行 `tsx scripts/run-integration-tests.ts --diff HEAD~1` 验证通过

## 4. 阶段 4：覆盖率验证

- [ ] 4.1 运行 `mvn verify -pl test` 验证覆盖率达标

## 5. 阶段 5：最终抽检

- [ ] 5.1 运行 `tsx scripts/run-sample-tests.ts` 抽检通过

## 6. 提交

- [ ] 6.1 提交代码：`feat(order): add sortOrder field to OrderAggr`
