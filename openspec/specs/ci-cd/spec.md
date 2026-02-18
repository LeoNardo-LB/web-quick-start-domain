# ci-cd Specification

## Purpose
TBD - created by archiving change migrate-spec-kit-specs-to-opencode. Update Purpose after archive.
## Requirements
### Requirement: 代码质量检查

The CI pipeline MUST execute code quality checks to ensure code compliance with standards.

#### Scenario: 静态代码分析

- **WHEN** 代码提交到代码仓库
- **THEN** 必须执行静态代码分析（使用SonarQube或Checkstyle）
- **THEN** 必须检查代码规范符合度（基于constitution.md的规则）
- **THEN** 必须检查代码重复度（重复率>5%必须阻断构建）
- **THEN** 必须检查代码复杂度（圈复杂度>15必须警告，>20必须阻断）
- **THEN** 分析报告必须包含质量评分（A/B/C/D等级，D级必须阻断）

#### Scenario: 安全漏洞扫描

- **WHEN** 代码进行构建
- **THEN** 必须执行依赖漏洞扫描（使用OWASP Dependency Check或Snyk）
- **THEN** 必须扫描代码中的安全问题（如硬编码密码、SQL注入风险）
- **THEN** 发现高危漏洞（CVSS≥7.0）必须阻断构建
- **THEN** 扫描结果必须包含在构建报告中

#### Scenario: 代码风格检查

- **WHEN** 代码进行构建
- **THEN** 必须执行代码风格检查（使用SpotBugs、PMD）
- **THEN** 必须检查空指针风险、资源未关闭、并发问题
- **THEN** 禁止使用 `@SuppressWarnings` 忽略警告（必须有合理说明）
- **THEN** 代码风格检查结果必须影响构建状态（严重问题必须阻断）

---

### Requirement: 自动化测试

The CI pipeline MUST execute comprehensive automated tests to ensure code quality.

#### Scenario: 单元测试执行

- **WHEN** 代码提交或合并请求
- **THEN** 必须执行所有单元测试
- **THEN** 测试失败时必须阻断构建
- **THEN** 单元测试必须在5分钟内完成（超时标记为失败）
- **THEN** 单元测试必须不依赖外部环境（纯Mock，不启动Spring）

#### Scenario: 集成测试执行

- **WHEN** 代码合并到主分支
- **THEN** 必须执行集成测试
- **THEN** 集成测试必须启动Spring上下文（@SpringBootTest）
- **THEN** 集成测试必须包含数据库、Redis、消息队列的测试
- **THEN** 集成测试必须在15分钟内完成（超时标记为失败）

#### Scenario: E2E测试执行

- **WHEN** 发布新版本到生产环境
- **THEN** 必须执行端到端（E2E）测试
- **THEN** E2E测试必须模拟真实用户场景（如创建订单、支付、查询订单）
- **THEN** E2E测试必须在测试环境或预发布环境执行
- **THEN** E2E测试失败时必须阻断发布
- **THEN** E2E测试必须包含性能测试（API响应时间<500ms）

---

### Requirement: 自动化部署

The CI pipeline MUST support automated deployment to improve release efficiency.

#### Scenario: Docker镜像构建

- **WHEN** 代码构建成功
- **THEN** 必须构建Docker镜像（使用Dockerfile）
- **THEN** Docker镜像必须使用多阶段构建（减少镜像大小）
- **THEN** 基础镜像必须固定版本（禁止使用 `latest` 标签）
- **THEN** Docker镜像必须包含健康检查（HEALTHCHECK指令）
- **THEN** Docker镜像必须推送到镜像仓库（Harbor、Docker Hub）

#### Scenario: Kubernetes部署

- **WHEN** 部署应用到K8s集群
- **THEN** 必须使用Helm或Kustomize进行部署
- **THEN** 必须配置资源限制（CPU、Memory、限制资源防止OOM）
- **THEN** 必须配置探针（livenessProbe、readinessProbe）
- **THEN** 必须配置滚动更新策略（滚动更新，零停机）
- **THEN** 部署必须记录版本号和配置（便于回滚）

#### Scenario: 蓝绿发布（Blue-Green Deployment）

- **WHEN** 发布新版本到生产环境
- **THEN** 必须使用蓝绿发布策略（减少发布风险）
- **THEN** 必须并行运行新版本（Green）和旧版本（Blue）
- **THEN** 新版本健康检查通过后，必须切换流量（如Nginx权重调整）
- **THEN** 旧版本必须保留至少30分钟（便于快速回滚）
- **THEN** 回滚时必须立即切换流量到旧版本

---

### Requirement: 环境管理

The CI pipeline MUST manage multi-environment configurations to ensure environment isolation and consistency.

#### Scenario: 环境配置管理

- **WHEN** 配置应用环境
- **THEN** 必须区分环境（Dev、Test、Stage、Prod）
- **THEN** 每个环境必须有独立的配置文件（`application-dev.yml`、`application-prod.yml`）
- **THEN** 敏感配置（数据库密码、API密钥）必须使用环境变量或密钥管理系统
- **THEN** 禁止将生产配置提交到代码仓库（使用 `.env` 文件或密钥管理）
- **THEN** 环境变量必须命名规范（如 `DB_HOST`、`DB_PORT`、`REDIS_HOST`）

#### Scenario: 环境隔离

- **WHEN** 执行CI/CD流程
- **THEN** Dev环境必须独立部署，禁止与Test/Prod混用
- **THEN** Test环境必须有测试数据（非生产数据）
- **THEN** Stage环境必须使用接近生产的配置（预发布验证）
- **THEN** Prod环境必须有严格的访问控制（仅授权人员可操作）
- **THEN** 环境之间的数据迁移必须有记录和审计

---

### Requirement: 回滚策略

The system MUST support rapid rollback to minimize the impact of release failures.

#### Scenario: 快速回滚

- **WHEN** 新版本发布后发现严重问题
- **THEN** 必须能够在一键回滚到上一个稳定版本
- **THEN** 回滚必须切换流量（无需重新部署）
- **THEN** 回滚时间必须<5分钟（包括流量切换和健康检查）
- **THEN** 回滚时必须保留问题版本的日志和快照（便于分析）
- **THEN** 回滚必须发送告警通知（通知运维和开发团队）

#### Scenario: 数据库回滚

- **WHEN** 发布包含数据库变更（如Schema更新）
- **THEN** 必须提供数据库回滚脚本（Migration Script的Rollback）
- **THEN** 数据库变更必须使用事务（失败自动回滚）
- **THEN** 数据库回滚脚本必须在发布前验证
- **THEN** 禁止在回滚脚本中使用 `DROP`、`DELETE`（仅使用数据修正）

#### Scenario: 配置回滚

- **WHEN** 发布包含配置变更
- **THEN** 必须保留旧版本的配置（配置版本控制）
- **THEN** 回滚时必须恢复旧版本的配置
- **THEN** 配置回滚必须验证（健康检查通过后才标记回滚成功）
- **THEN** 配置回滚必须记录到审计日志（回滚原因、时间、操作人）

---

