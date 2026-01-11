## DDD订单系统Demo展示内容框架

这套demo展示了DDD的核心价值：**将复杂业务逻辑清晰地组织在领域模型中，通过分层架构和设计模式实现可维护、可扩展的系统**
。每个组件都有明确的职责边界，业务规则集中在领域层，技术细节隔离在基础设施层。

## 设计与内容

### **1. 分层架构设计**

- **领域层**：包含聚合根、实体、值对象、领域服务、领域事件、仓储接口
- **应用层**：包含应用服务、DTO、命令/查询处理器、协调领域对象
- **基础设施层**：包含仓储实现、外部服务适配器、事件发布器
- **接口层**：包含API控制器、请求/响应模型

### **2. CQRS架构实现**

- **命令侧**：
    - `CreateOrderCommand`：创建订单命令
    - `PayOrderCommand`：支付订单命令
    - `CancelOrderCommand`：取消订单命令
    - 命令处理器负责调用应用服务，处理写操作
- **查询侧**：
    - `GetOrderByIdQuery`：根据ID查询订单
    - `GetOrdersByCustomerQuery`：根据客户查询订单列表
    - 查询处理器直接访问读模型，不经过领域层
- **分离原则**：命令和查询使用不同的模型，命令处理事务性操作，查询优化读取性能

### **3. 聚合根与对象设计**

- **聚合根**：`Order`（订单）
    - 包含订单项、地址、状态等核心业务逻辑
    - 通过工厂方法创建：`Order.create()` 确保业务规则验证
    - 保护聚合边界：外部只能通过聚合根访问内部实体
- **实体**：`OrderItem`（订单项）
    - 有唯一标识，可独立变化
    - 业务方法：计算商品总价、验证库存等
- **值对象**：
    - `Money`：金额（包含货币类型、金额值，不可变）
    - `Address`：地址（省份、城市、详细地址，值相等性）
    - `ContactInfo`：联系信息（姓名、电话、邮箱）
- **仓储接口**：`IOrderRepository`
    - 方法：`findById()`, `save()`, `findByCustomer()`
    - 聚合根级别的操作，不暴露内部实体

### **4. 六边形架构实现（外部接口调用）**

- **端口（接口）定义**：
    - `IPaymentGateway`：支付网关接口（领域层定义）
    - `IInventoryService`：库存服务接口（领域层定义）
    - `INotificationService`：通知服务接口（领域层定义）
- **适配器实现**（基础设施层）：
    - `StripePaymentAdapter`：实现支付网关接口，调用Stripe API
    - `InventoryServiceAdapter`：实现库存服务接口，调用微服务
    - `EmailNotificationAdapter`：实现通知服务接口，发送邮件
- **防腐层职责**：
    - 将外部服务的数据格式转换为领域对象
    - 处理外部服务的异常，转换为领域异常
    - 隔离外部变化对核心领域的影响

### **5. 领域服务 vs 应用服务**

- **领域服务**（`OrderDomainService`）：
    - 职责：封装跨聚合根的业务规则
    - 方法：`validateOrderItems()` 验证商品库存和价格
    - 特点：无状态，纯业务逻辑，可测试性强
- **应用服务**（`OrderApplicationService`）：
    - 职责：协调领域对象，处理事务，编排工作流
    - 方法：`createOrder()`、`processPayment()`
    - 特点：处理基础设施依赖，管理事务边界
- **职责区分**：
    - 领域服务：回答"业务规则是什么"
    - 应用服务：回答"如何完成这个用例"
    - 应用服务调用领域服务，但领域服务不依赖应用服务

### **6. 领域事件发布与消费**

- **事件定义**：
    - `OrderCreatedEvent`：订单创建事件
    - `OrderPaidEvent`：订单支付事件
    - `OrderCancelledEvent`：订单取消事件
- **事件发布**：
    - 聚合根内触发：`order.addDomainEvent(new OrderCreatedEvent())`
    - 应用服务发布：`eventPublisher.publish(order.releaseEvents())`
- **事件消费**：
    - 库存服务：监听`OrderCreatedEvent`，锁定库存
    - 通知服务：监听`OrderPaidEvent`，发送支付成功通知
    - 分析服务：监听所有订单事件，更新业务报表
- **事件处理模式**：
    - 同步处理：关键业务（如库存锁定）
    - 异步处理：非关键业务（如通知、分析）

### **7. 完整订单链路流程**

1. **创建订单**：
    - API接收创建订单请求 → 命令处理器 → 应用服务
    - 应用服务调用领域服务验证商品
    - 聚合根工厂创建Order对象
    - 仓储保存订单，发布OrderCreatedEvent
    - 事件处理器锁定库存

2. **支付订单**：
    - API接收支付请求 → 命令处理器 → 应用服务
    - 应用服务通过防腐层调用支付网关
    - 聚合根确认支付，状态变更
    - 仓储保存，发布OrderPaidEvent
    - 事件处理器发送通知、更新报表

3. **查询订单**：
    - API接收查询请求 → 查询处理器
    - 直接访问读模型（优化的查询表）
    - 返回DTO，不经过领域层
    - 高性能读取，与写模型完全分离

### **8. 关键设计原则体现**

- **聚合根设计原则**：一个事务只修改一个聚合根
- **领域对象富血原则**：业务逻辑在领域对象内，而非服务层
- **依赖倒置原则**：高层模块不依赖低层模块，都依赖抽象
- **单一职责原则**：每个层、每个服务职责清晰
- **最终一致性**：通过领域事件实现跨服务数据一致性

### **9. 测试策略**

- **领域层测试**：纯单元测试，不依赖基础设施
- **应用层测试**：模拟仓储和外部服务
- **集成测试**：验证端到端流程，包含事件处理
- **性能测试**：验证CQRS分离后的查询性能提升

## demo DDL

```mysql
  -- ============================================================
-- 订单系统表结构 DDL
-- 符合项目代码编写规范
-- ============================================================

-- ------------------------------------------------------------
-- 1. 订单表（order）- 聚合根
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`       VARCHAR(64)    NOT NULL COMMENT '订单编号',
    `customer_id`    VARCHAR(64)    NOT NULL COMMENT '客户ID',
    `customer_name`  VARCHAR(128)   NOT NULL COMMENT '客户姓名',
    `status`         VARCHAR(32)    NOT NULL COMMENT '订单状态：CREATED-已创建, PAID-已支付, CANCELLED-已取消, SHIPPED-已发货, COMPLETED-已完成',
    `total_amount`   DECIMAL(18, 2) NOT NULL COMMENT '订单总金额',
    `currency`       VARCHAR(16)    NOT NULL DEFAULT 'CNY' COMMENT '货币类型',
    `payment_method` VARCHAR(32)             DEFAULT NULL COMMENT '支付方式',
    `remark`         VARCHAR(512)            DEFAULT NULL COMMENT '备注',

    -- 审计字段（必须）
    `create_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`    TIMESTAMP      NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`    VARCHAR(64)             DEFAULT NULL COMMENT '创建人ID',
    `update_user`    VARCHAR(64)             DEFAULT NULL COMMENT '更新人ID',
    `delete_user`    VARCHAR(64)             DEFAULT NULL COMMENT '删除人ID',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_order_no` (`order_no`),
    KEY `idx_order_customer_id` (`customer_id`),
    KEY `idx_order_status` (`status`),
    KEY `idx_order_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单表-聚合根';

-- ------------------------------------------------------------
-- 2. 订单项表（order_item）- 实体
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_item`
(
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
    `order_id`     BIGINT         NOT NULL COMMENT '订单ID',
    `product_id`   VARCHAR(64)    NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(256)   NOT NULL COMMENT '商品名称',
    `sku_code`     VARCHAR(64)    NOT NULL COMMENT 'SKU编码',
    `unit_price`   DECIMAL(18, 2) NOT NULL COMMENT '单价',
    `currency`     VARCHAR(16)    NOT NULL DEFAULT 'CNY' COMMENT '货币类型',
    `quantity`     INT            NOT NULL COMMENT '数量',
    `subtotal`     DECIMAL(18, 2) NOT NULL COMMENT '小计金额',

    -- 审计字段（必须）
    `create_time`  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`  TIMESTAMP      NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`  VARCHAR(64)             DEFAULT NULL COMMENT '创建人ID',
    `update_user`  VARCHAR(64)             DEFAULT NULL COMMENT '更新人ID',
    `delete_user`  VARCHAR(64)             DEFAULT NULL COMMENT '删除人ID',

    PRIMARY KEY (`id`),
    KEY `idx_order_item_order_id` (`order_id`),
    KEY `idx_order_item_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单项表-实体';

-- ------------------------------------------------------------
-- 3. 订单地址表（order_address）- 值对象
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_address`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `order_id`       BIGINT       NOT NULL COMMENT '订单ID',
    `province`       VARCHAR(64)  NOT NULL COMMENT '省份',
    `city`           VARCHAR(64)  NOT NULL COMMENT '城市',
    `district`       VARCHAR(64)           DEFAULT NULL COMMENT '区县',
    `detail_address` VARCHAR(512) NOT NULL COMMENT '详细地址',
    `postal_code`    VARCHAR(16)           DEFAULT NULL COMMENT '邮政编码',

    -- 审计字段（必须）
    `create_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`    TIMESTAMP    NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`    VARCHAR(64)           DEFAULT NULL COMMENT '创建人ID',
    `update_user`    VARCHAR(64)           DEFAULT NULL COMMENT '更新人ID',
    `delete_user`    VARCHAR(64)           DEFAULT NULL COMMENT '删除人ID',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_address_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单地址表-值对象';

-- ------------------------------------------------------------
-- 4. 订单联系信息表（order_contact_info）- 值对象
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_contact_info`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '联系信息ID',
    `order_id`      BIGINT       NOT NULL COMMENT '订单ID',
    `contact_name`  VARCHAR(128) NOT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(32)  NOT NULL COMMENT '联系人电话',
    `contact_email` VARCHAR(128)          DEFAULT NULL COMMENT '联系人邮箱',

    -- 审计字段（必须）
    `create_time`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`   TIMESTAMP    NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`   VARCHAR(64)           DEFAULT NULL COMMENT '创建人ID',
    `update_user`   VARCHAR(64)           DEFAULT NULL COMMENT '更新人ID',
    `delete_user`   VARCHAR(64)           DEFAULT NULL COMMENT '删除人ID',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_contact_info_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单联系信息表-值对象';

-- ------------------------------------------------------------
-- 5. 支付记录表（payment）- 支付记录
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `payment`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
    `payment_no`     VARCHAR(64)    NOT NULL COMMENT '支付编号',
    `order_id`       BIGINT         NOT NULL COMMENT '订单ID',
    `order_no`       VARCHAR(64)    NOT NULL COMMENT '订单编号',
    `payment_method` VARCHAR(32)    NOT NULL COMMENT '支付方式：ALIPAY-支付宝, WECHAT-微信, STRIPE-Stripe',
    `amount`         DECIMAL(18, 2) NOT NULL COMMENT '支付金额',
    `currency`       VARCHAR(16)    NOT NULL DEFAULT 'CNY' COMMENT '货币类型',
    `status`         VARCHAR(32)    NOT NULL COMMENT '支付状态：PENDING-待支付, SUCCESS-成功, FAILED-失败, REFUNDED-已退款',
    `transaction_id` VARCHAR(128)            DEFAULT NULL COMMENT '第三方交易ID',
    `failed_reason`  VARCHAR(512)            DEFAULT NULL COMMENT '失败原因',

    -- 审计字段（必须）
    `create_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_time`    TIMESTAMP      NULL     DEFAULT NULL COMMENT '删除时间',
    `create_user`    VARCHAR(64)             DEFAULT NULL COMMENT '创建人ID',
    `update_user`    VARCHAR(64)             DEFAULT NULL COMMENT '更新人ID',
    `delete_user`    VARCHAR(64)             DEFAULT NULL COMMENT '删除人ID',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_payment_no` (`payment_no`),
    KEY `idx_payment_order_id` (`order_id`),
    KEY `idx_payment_order_no` (`order_no`),
    KEY `idx_payment_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='支付记录表';
```

## 展示计划

### 实现概述

基于项目现有四层架构，实现完整的DDD订单系统，展示所有DDD核心概念。

**代码位置**: 所有代码放在 `_example` 包下

- `domain/_example/order/` - 领域层
- `app/_example/order/` - 应用层
- `infrastructure/_example/order/` - 基础设施层
- `adapter/_example/order/` - 接口层

### 已生成资源

✅ **DO类** (infrastructure/_shared/generated/repository/entity)

- OrderAggrDO, OrderItemDO, OrderAddressDO, OrderContactInfoDO, PaymentDO

✅ **Mapper接口** (infrastructure/_shared/generated/repository/mapper)

- OrderAggrMapper, OrderItemMapper, OrderAddressMapper, OrderContactInfoMapper, PaymentMapper

✅ **复用现有表**

- event_publish - 事件发布表
- event_consume - 事件消费表

### 类清单 (共87个)

#### 领域层 (28个类)

```
model/
├── OrderAggr.java                      【聚合根】订单聚合根
├── OrderItem.java                      【实体】订单项
├── OrderStatus.java                    【枚举】订单状态
├── PaymentStatus.java                  【枚举】支付状态
├── PaymentMethod.java                  【枚举】支付方式
├── OrderId.java                        【值对象】订单ID
├── valueobject/
│   ├── Money.java                      【值对象】金额（带货币）
│   ├── Address.java                    【值对象】地址
│   ├── ContactInfo.java                【值对象】联系信息
│   └── OrderItemInfo.java              【值对象】订单项信息
└── event/
    ├── OrderCreatedEvent.java          【领域事件】订单创建
    ├── OrderPaidEvent.java             【领域事件】订单支付
    └── OrderCancelledEvent.java        【领域事件】订单取消

repository/
└── OrderAggrRepository.java            【仓储接口】订单仓储

service/
├── OrderDomainService.java             【领域服务】订单领域服务
├── PaymentGateway.java                 【端口接口】支付网关
└── InventoryService.java               【端口接口】库存服务
```

#### 应用层 (14个类)

```
OrderApplicationService.java            【应用服务】订单应用服务

command/
├── CreateOrderCommand.java             【命令】创建订单
├── PayOrderCommand.java                【命令】支付订单
├── CancelOrderCommand.java             【命令】取消订单
└── ShipOrderCommand.java               【命令】发货订单

query/
├── GetOrderByIdQuery.java              【查询】查询订单详情
└── GetOrdersByCustomerQuery.java       【查询】查询客户订单列表

dto/
├── OrderDTO.java                       【DTO】订单DTO
├── OrderItemDTO.java                   【DTO】订单项DTO
├── AddressDTO.java                     【DTO】地址DTO
└── ContactInfoDTO.java                 【DTO】联系信息DTO
```

#### 基础设施层 (18个类)

```
persistence/
├── OrderAggrRepositoryImpl.java        【仓储实现】订单仓储实现
└── converter/
    ├── OrderAggrConverter.java         【MapStruct】订单转换器
    ├── OrderItemConverter.java         【MapStruct】订单项转换器
    ├── AddressConverter.java           【MapStruct】地址转换器
    └── ContactInfoConverter.java       【MapStruct】联系信息转换器

adapter/
├── StripePaymentAdapter.java           【适配器】Stripe支付适配器
├── MockInventoryServiceAdapter.java    【适配器】模拟库存服务
└── notification/
    └── OrderNotificationAdapter.java   【适配器】订单通知适配器
```

#### 接口层 (15个类)

```
web/api/
└── OrderController.java                【Controller】订单控制器

web/dto/request/
├── CreateOrderRequest.java             【Request】创建订单请求
├── PayOrderRequest.java                【Request】支付订单请求
└── CancelOrderRequest.java             【Request】取消订单请求

web/dto/response/
└── OrderResponse.java                  【Response】订单响应

listener/
├── OrderCreatedEventListener.java      【事件监听器】订单创建事件（真实调用库存服务）
├── OrderPaidEventListener.java         【事件监听器】订单支付事件（Mock调用通知服务）
└── OrderCancelledEventListener.java    【事件监听器】订单取消事件（真实调用库存释放+Mock通知）
```

#### 集成测试 (3个类)

```
start/src/test/java/integration/order/
├── OrderIntegrationTest.java           【集成测试】端到端流程测试
├── OrderCQRSTest.java                  【集成测试】CQRS分离测试
└── OrderEventTest.java                 【集成测试】事件发布与消费测试
```

### API端点设计

| 端点                        | 方法   | 描述     | 用例                       |
|---------------------------|------|--------|--------------------------|
| `/api/orders`             | POST | 创建订单   | CreateOrderCommand       |
| `/api/orders/{id}/pay`    | POST | 支付订单   | PayOrderCommand          |
| `/api/orders/{id}/cancel` | POST | 取消订单   | CancelOrderCommand       |
| `/api/orders/{id}/ship`   | POST | 发货订单   | ShipOrderCommand         |
| `/api/orders/{id}`        | GET  | 查询订单详情 | GetOrderByIdQuery        |
| `/api/orders`             | GET  | 查询订单列表 | GetOrdersByCustomerQuery |

### 实施计划 (5个阶段)

**阶段1: 领域层基础** (枚举、值对象、实体、事件、仓储接口)
**阶段2: 聚合根和领域服务** (OrderAggr、OrderDomainService)
**阶段3: 基础设施层** (仓储实现、MapStruct转换器、外部服务适配器)
**阶段4: 应用层和接口层** (应用服务、命令查询、Controller、事件监听器)
**阶段5: 集成测试和文档** (集成测试、更新本文档)

### 关键设计决策

- **聚合根设计**: OrderAggr作为唯一聚合根，一个事务只修改一个聚合根
- **值对象不可变性**: Money、Address、ContactInfo构造后不可修改
- **事件发布时机**: 聚合根内addDomainEvent()，应用服务保存后publish()
- **CQRS实现**: 命令侧execute()，查询侧query()，查询直接访问DO
- **六边形架构**: 端口接口定义在领域层，适配器实现在基础设施层
- **事务边界**: 应用服务方法使用@Transactional，一个用例一个事务
- **事件监听器混合实现**: 关键业务（库存）真实调用，非关键业务（通知）Mock实现

### 验证标准

每个阶段完成后执行：

1. `mvn clean compile` - 编译通过
2. `mvn test` - 单元测试通过
3. `mvn test -Dtest=ApplicationStartupTests -pl start` - 启动测试通过

最终交付：

- ✅ 完整的四层架构代码 (87个类)
- ✅ 可运行的集成测试 (3个测试类)
- ✅ 更新"项目结果"表格 (填写关联的类和实现方式)

## 项目结果

项目展示demo所能达到的程度

| 展示的功能           | 关联的类 | 实现方式 |
|-----------------|------|------|
| 分层架构设计          |      |      |
| CQRS架构实现        |      |      |
| 聚合根与对象设计        |      |      |
| 六边形架构实现（外部接口调用） |      |      |
| 领域服务 vs 应用服务    |      |      |
| 领域事件发布与消费       |      |      |
| 完整订单链路流程        |      |      |
| 关键设计原则体现        |      |      |
| 测试策略            |      |      |

// TODO 更加完整与详细的各类说明