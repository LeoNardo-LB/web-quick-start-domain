/**
 * 基础设施层（Infrastructure Layer）
 *
 * <p>职责：提供技术实现和外部系统集成</p>
 *
 * <h3>核心职责</h3>
 * <ul>
 *   <li>数据持久化（Data Persistence）</li>
 *   <li>外部服务集成（External Service Integration）</li>
 *   <li>消息中间件（Messaging Middleware）</li>
 *   <li>缓存实现（Cache Implementation）</li>
 *   <li>配置管理（Configuration Management）</li>
 * </ul>
 *
 * <h3>子包结构</h3>
 * <ul>
 *   <li>{@code _shared} - 共享基础设施组件（配置、事件发布、ID生成等）</li>
 *   <li>{@code _example} - 示例业务模块的基础设施实现</li>
 *   <li>{@code common} - 通用基础设施实现（文件、日志、通知等）</li>
 * </ul>
 *
 * <h3>架构约束</h3>
 * <ul>
 *   <li>✅ 可以实现：Domain层定义的接口</li>
 *   <li>✅ 可以依赖：技术框架（Spring、MyBatis、Kafka等）</li>
 *   <li>❌ 禁止：包含业务逻辑（应在Domain层）</li>
 *   <li>❌ 禁止：被Domain层依赖</li>
 * </ul>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li>仓储模式（Repository Pattern）</li>
 *   <li>依赖倒置（Dependency Inversion）</li>
 *   <li>适配器模式（Adapter Pattern）</li>
 * </ul>
 *
 * <h3>技术实现</h3>
 * <ul>
 *   <li>持久化：MyBatis-Flex</li>
 *   <li>消息队列：Kafka</li>
 *   <li>缓存：Redis</li>
 *   <li>对象存储：OSS（可替换）</li>
 * </ul>
 * @see org.smm.archetype.domain 领域层
 */
package org.smm.archetype.infrastructure;
