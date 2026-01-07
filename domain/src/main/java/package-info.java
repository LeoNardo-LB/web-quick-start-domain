/**
 * 领域层（Domain Layer）
 *
 * <p><strong>核心职责：</strong></p>
 * <ul>
 *   <li>承载核心业务逻辑和业务规则</li>
 *   <li>定义业务实体、值对象、聚合根等领域模型</li>
 *   <li>实现领域服务，处理跨实体的业务逻辑</li>
 *   <li>定义仓储接口，抽象数据访问</li>
 *   <li>发布和处理领域事件，实现业务解耦</li>
 *   <li>维护业务不变性约束和业务规则验证</li>
 * </ul>
 *
 * <p><strong>典型类/组件：</strong></p>
 * <ul>
 *   <li>实体（Entity）：具有唯一标识的业务对象</li>
 *   <li>值对象（Value Object）：无标识的不可变对象</li>
 *   <li>聚合根（Aggregate Root）：一致性边界控制</li>
 *   <li>领域服务（Domain Service）：跨聚合的业务逻辑</li>
 *   <li>仓储接口（Repository Interface）：数据访问抽象</li>
 *   <li>领域事件（Domain Event）：业务状态变更通知</li>
 *   <li>工厂（Factory）：复杂对象创建</li>
 *   <li>规格模式（Specification）：业务规则封装</li>
 * </ul>
 *
 * <p><strong>分包策略：</strong></p>
 * <ul>
 *   <li>按业务界限上下文（Bounded Context）分包</li>
 *   <li>按业务能力或业务模块组织</li>
 *   <li>典型包结构：<code>com.example.domain.model.customer</code></li>
 *   <li>按领域概念分包：<code>entity</code>, <code>valueobject</code>, <code>service</code>, <code>repository</code>, <code>event</code></li>
 *   <li>避免按技术类型分包，保持业务语义</li>
 * </ul>
 *
 * <p><strong>界限区分：</strong></p>
 * <ul>
 *   <li><strong>完全独立</strong>：不依赖任何其他层，是系统的核心</li>
 *   <li><strong>业务纯净</strong>：不包含任何技术细节、框架依赖</li>
 *   <li><strong>接口抽象</strong>：只定义接口，不提供具体实现</li>
 *   <li><strong>依赖方向</strong>：其他层可以依赖Domain层，但Domain层不依赖任何外部层</li>
 *   <li><strong>测试独立</strong>：可以独立于基础设施进行单元测试</li>
 * </ul>
 * @author DDD架构团队
 * @version 1.0.0
 * @since 1.0.0
 */