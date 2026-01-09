/**
 * 应用层（Application Layer）
 *
 * <p>职责：应用服务层，负责用例编排和事务管理</p>
 *
 * <h3>核心职责</h3>
 * <ul>
 *   <li>用例编排（Orchestration）</li>
 *   <li>事务管理（Transaction Management）</li>
 *   <li>DTO转换（DTO Conversion）</li>
 *   <li>调用领域服务（Domain Service Invocation）</li>
 *   <li>事件发布（Event Publishing）</li>
 * </ul>
 *
 * <h3>子包结构</h3>
 * <ul>
 *   <li>{@code _shared} - 共享应用层组件</li>
 *   <li>{@code _example} - 示例业务模块</li>
 *   <li>{@code common} - 通用组件</li>
 * </ul>
 *
 * <h3>架构约束</h3>
 * <ul>
 *   <li>✅ 可以调用：Domain层、Infrastructure层</li>
 *   <li>❌ 禁止：被Domain层依赖</li>
 *   <li>❌ 禁止：包含核心业务逻辑（应在Domain层）</li>
 *   <li>❌ 禁止：直接访问数据库（应通过Repository）</li>
 * </ul>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li>CQRS（命令查询职责分离）</li>
 *   <li>应用服务模式（Application Service）</li>
 * </ul>
 * @see org.smm.archetype.domain 领域层
 * @see org.smm.archetype.infrastructure 基础设施层
 */
package org.smm.archetype.app;
