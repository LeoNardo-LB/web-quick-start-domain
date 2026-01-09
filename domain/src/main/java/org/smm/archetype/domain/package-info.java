/**
 * 领域层（Domain Layer）
 *
 * <p>职责：核心业务逻辑层，包含所有业务规则和领域模型</p>
 *
 * <h3>核心职责</h3>
 * <ul>
 *   <li>领域模型（Domain Model）</li>
 *   <li>业务规则（Business Rules）</li>
 *   <li>领域服务（Domain Services）</li>
 *   <li>领域事件（Domain Events）</li>
 *   <li>仓储接口（Repository Interfaces）</li>
 * </ul>
 *
 * <h3>子包结构</h3>
 * <ul>
 *   <li>{@code _shared} - 共享领域组件（基类、事件、异常、规格等）</li>
 *   <li>{@code _example} - 示例业务模块（订单等）</li>
 *   <li>{@code common} - 通用领域组件（文件、日志、通知等）</li>
 * </ul>
 *
 * <h3>架构约束</h3>
 * <ul>
 *   <li>✅ 纯净的业务逻辑</li>
 *   <li>✅ 无外部依赖（不依赖任何外层）</li>
 *   <li>✅ 可独立测试</li>
 *   <li>❌ 禁止：依赖Adapter、App、Infrastructure层</li>
 *   <li>❌ 禁止：包含技术实现细节</li>
 * </ul>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li>DDD（领域驱动设计）</li>
 *   <li>聚合根模式（Aggregate Root）</li>
 *   <li>值对象模式（Value Object）</li>
 *   <li>规格模式（Specification）</li>
 *   <li>领域事件模式（Domain Event）</li>
 * </ul>
 *
 * <h3>核心概念</h3>
 * <ul>
 *   <li><b>聚合根（Aggregate Root）</b>：聚合的入口点，维护一致性边界</li>
 *   <li><b>实体（Entity）</b>：有唯一标识、有生命周期的对象</li>
 *   <li><b>值对象（Value Object）</b>：无标识、不可变的对象</li>
 *   <li><b>领域服务（Domain Service）</b>：不属于特定聚合的业务逻辑</li>
 *   <li><b>仓储（Repository）</b>：持久化抽象接口</li>
 * </ul>
 */
package org.smm.archetype.domain;
