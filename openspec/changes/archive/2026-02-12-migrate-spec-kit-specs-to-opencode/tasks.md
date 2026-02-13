# 规范迁移至OpenSpec工作流 - 任务清单

## 1. 基础设施准备

- [x] 1.1 确认OpenSpec工作流配置正确
- [x] 1.2 创建迁移指南文档
- [x] 1.3 准备培训材料和快速开始指南
- [x] 1.4 创建变更记录模板

---

## 2. P0规范迁移（3-5天）

- [x] 2.1 创建security.md规范文档（OpenSpec spec格式）
  - 包含：SQL注入防护、XSS防护、CSRF防护、认证授权规范、敏感数据处理、依赖安全
- [x] 2.2 修复constitution.md的§VII验证优先级原则
  - 移除"禁止合并Maven命令"的限制
  - 保留Maven lifecycle正确使用说明
- [x] 2.3 在constitution.md中新增"安全原则"章节
  - 引用security.md的P0要求
- [x] 2.4 更新verification-workflow.md，增加安全扫描步骤
  - 添加依赖漏洞扫描命令（OWASP Dependency Check）
- [x] 2.5 归档P0阶段完成的变更
  - 使用 `/opsx-archive` 命令归档

---

## 3. P1规范迁移 - 性能优化（2天）

- [x] 3.1 创建performance-optimization.md规范文档
  - 包含：N+1查询识别和解决、缓存策略、数据库索引设计、慢查询监控、异步处理
- [x] 3.2 在constitution.md中新增"性能优化原则"章节
  - 引用performance-optimization.md的P1要求

---

## 4. P1规范迁移 - API设计（2天）

- [x] 4.1 创建api-design.md规范文档
  - 包含：API命名规范、版本控制策略、分页/排序/过滤、响应格式、幂等性、限流与降级
- [x] 4.2 在constitution.md中新增"API设计原则"章节
  - 引用api-design.md的P1要求

---

## 5. P1规范迁移 - 错误码设计（2天）

- [x] 5.1 创建error-code-design.md规范文档
  - 包含：错误码体系设计、国际化错误信息、错误日志记录、客户端友好错误、服务端调试错误
- [ ] 5.2 在constitution.md中新增"错误码设计原则"章节（可选，如constitution已有相关内容可跳过）

---

## 6. P1规范迁移 - 监控与日志（2天）

- [x] 6.1 创建monitoring-logging.md规范文档
  - 包含：指标监控、链路追踪、日志分级、结构化日志、告警规则
- [x] 6.2 在constitution.md中新增"监控与日志原则"章节
  - 引用monitoring-logging.md的P1要求

---

## 7. P1规范迁移 - CI/CD（2天）

- [x] 7.1 创建ci-cd.md规范文档
  - 包含：代码质量检查、自动化测试、自动化部署、环境管理、回滚策略
- [x] 7.2 在constitution.md中新增"CI/CD原则"章节
  - 引用ci-cd.md的P1要求

---

## 8. P1规范迁移 - 合规性（2天）

- [x] 8.1 创建compliance.md规范文档
  - 包含：数据隐私、审计日志、安全合规、代码审查、变更管理
- [ ] 8.2 在constitution.md中新增"合规性原则"章节
  - 引用compliance.md的P1要求

---

## 9. 现有规范转换（5-7天）

- [ ] 9.1 转换001-test-standardization为OpenSpec spec格式
  - 保持内容，调整格式以符合OpenSpec要求
  - 检查并修正scenario的4个hashtag要求
- [ ] 9.2 转换1-fix-logging-output为OpenSpec spec格式
  - 保持内容，调整格式以符合OpenSpec要求
  - 检查并修正scenario的4个hashtag要求
- [ ] 9.3 转换002-constitution-compliance为OpenSpec spec格式
  - 保持内容，调整格式以符合OpenSpec要求
  - 检查并修正scenario的4个hashtag要求
- [ ] 9.4 转换001-guava-desensitize为OpenSpec spec格式
  - 保持内容，调整格式以符合OpenSpec要求
  - 检查并修正scenario的4个hashtag要求
- [ ] 9.5 转换002-test-structure-refactor为OpenSpec spec格式（如存在）
  - 保持内容，调整格式以符合OpenSpec要求
  - 检查并修正scenario的4个hashtag要求
- [ ] 9.6 归档现有规范转换完成的变更
  - 使用 `/opsx-archive` 命令归档每个转换完成的项目

---

## 10. 修改constitution.md - 补充章节（1-2天）

- [x] 10.1 在constitution.md中补充API设计原则章节
  - 引用api-design.md
- [x] 10.2 在constitution.md中补充性能优化原则章节
  - 引用performance-optimization.md
- [x] 10.3 在constitution.md中补充监控与日志原则章节
  - 引用monitoring-logging.md
- [x] 10.4 在constitution.md中补充CI/CD原则章节
  - 引用ci-cd.md
- [x] 10.5 在constitution.md中补充安全原则章节
  - 引用security.md

---

## 11. 修改verification-workflow.md - 补充步骤（1天）

- [x] 11.1 在verification-workflow.md中增加安全扫描步骤
  - 添加依赖漏洞扫描命令（OWASP Dependency Check）
- [x] 11.2 在verification-workflow.md中增加性能基准测试步骤
  - 添加性能测试命令和基准要求
- [x] 11.3 在verification-workflow.md中增加日志格式验证步骤
  - 添加日志格式检查和验证命令

---

## 12. 文档和培训材料（持续）

- [ ] 12.1 创建迁移指南文档
  - 说明新旧规范文档的对应关系
  - 提供迁移步骤和常见问题解答
- [ ] 12.2 创建快速开始指南
  - 如何使用OpenSpec工作流创建规范
  - 如何遵循新规范进行开发
- [ ] 12.3 创建培训PPT或文档
  - P0规范培训：安全要求
  - P1规范培训：性能、API、日志、CI/CD等

---

## 13. 团队推广和执行（持续）

- [ ] 13.1 组织培训会议
  - 介绍OpenSpec工作流和新规范
  - P0规范培训重点：安全要求
  - P1规范培训重点：性能、API、监控、CI/CD
- [ ] 13.2 在Code Review中关注新规范遵循情况
  - 安全规范：SQL注入防护、XSS防护、CSRF防护
  - 性能规范：N+1查询、缓存使用、索引设计
  - API规范：RESTful命名、版本控制、响应格式
- [ ] 13.3 收集团队反馈，持续改进规范
  - 定期收集规范使用中的问题和建议
  - 评估是否需要新增或修改规范
  - 记录规范的演进历史

---

## 14. 验证和归档

- [x] 14.1 验证所有创建的spec文档
  - 检查格式是否符合OpenSpec要求
  - 检查scenario是否使用4个hashtag
  - 检查是否所有需求都有对应的scenario
- [x] 14.2 验证design文档的完整性
  - 检查所有决策都有理由
  - 检查风险缓解措施是否合理
- [x] 14.3 验证tasks.md的完整性
  - 检查所有任务是否可追踪
  - 检查任务之间的依赖关系是否正确
- [x] 14.4 归档变更
  - 使用 `/opsx-archive migrate-spec-kit-specs-to-opencode` 命令归档
  - 确保所有artifacts都已完成
  - 生成归档总结报告
