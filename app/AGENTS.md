# APP 层

应用编排层 - CQRS、事务边界、用例协调。

## 概述

职责：用例编排、CQRS 分离、DTO 转换。
依赖规则：依赖 Domain，被 Adapter 调用。
规模：30+ Java 文件，应用服务与 DTO 转换集中。

## 结构

```
app/
├── bizshared/       # 共享应用服务（result 包装器、查询服务基类）
└── _example/        # 示例应用服务
    └── order/
        ├── OrderAppService.java     # 用例编排
        ├── command/                 # CQRS Commands
        ├── query/                   # CQRS Queries
        └── dto/                     # 应用层 DTO
```

## 关键位置

| 任务            | 位置                      | 备注                     |
|---------------|-------------------------|------------------------|
| 应用服务          | app/**/*AppService.java | 用例编排                   |
| CQRS Commands | app/**/command/         | 写操作输入 DTO              |
| CQRS Queries  | app/**/query/           | 读操作输入 DTO              |
| 应用层 DTO       | app/**/dto/             | 数据传输对象                 |
| 结果包装器         | app/bizshared/result/   | BaseResult, PageResult |

## 约定

仅编排：无业务规则（由 Domain 强制不变量）。
事务边界：ApplicationService 方法使用 `@Transactional`。
CQRS：分离 Command/Query DTO。
转换器：MapStruct 做 App DTO ↔ Domain/Response DTO 转换。

## 反模式

❌ 业务规则、直接访问基础设施、领域对象暴露给 Adapter、配置类、`@Data`。

