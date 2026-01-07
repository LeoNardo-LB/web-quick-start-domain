/**
 * 应用层（Application Layer）
 *
 * <p><strong>核心职责：</strong></p>
 * <ul>
 *   <li>协调领域对象完成具体业务用例</li>
 *   <li>管理事务边界，确保数据一致性</li>
 *   <li>处理DTO与领域对象之间的转换</li>
 *   <li>实现用例级别的业务逻辑编排</li>
 *   <li>提供面向外部的应用服务接口</li>
 *   <li>处理跨聚合的业务逻辑协调</li>
 *   <li>实现CQRS模式的命令和查询处理</li>
 * </ul>
 *
 * <p><strong>典型类/组件：</strong></p>
 * <ul>
 *   <li>应用服务（Application Service）：用例协调器</li>
 *   <li>命令处理器（Command Handler）：处理写操作</li>
 *   <li>查询处理器（Query Handler）：处理读操作</li>
 *   <li>DTO（Data Transfer Object）：数据传输对象</li>
 *   <li>DTO转换器（DTO Converter）：对象映射</li>
 *   <li>用例类（Use Case）：单一业务功能封装</li>
 *   <li>事务管理器（Transaction Manager）：事务控制</li>
 *   <li>安全服务（Security Service）：权限控制</li>
 * </ul>
 *
 * <p><strong>分包策略：</strong></p>
 * <ul>
 *   <li>按业务用例或功能模块分包</li>
 *   <li>按CQRS模式分离命令和查询</li>
 *   <li>典型包结构：<code>com.example.application.customer.command</code></li>
 *   <li>按功能分包：<code>service</code>, <code>usecase</code>, <code>dto</code>, <code>converter</code></li>
 *   <li>保持包结构与业务语义一致</li>
 * </ul>
 *
 * <p><strong>界限区分：</strong></p>
 * <ul>
 *   <li><strong>依赖Domain层</strong>：使用领域模型和业务规则</li>
 *   <li><strong>被Adapter层依赖</strong>：为外部请求提供服务</li>
 *   <li><strong>接口依赖Infrastructure</strong>：通过接口调用基础设施</li>
 *   <li><strong>薄层设计</strong>：避免业务逻辑泄露到此层</li>
 *   <li><strong>无状态</strong>：不保存业务状态，便于扩展</li>
 *   <li><strong>事务边界</strong>：在此层定义事务范围</li>
 * </ul>
 * @author DDD架构团队
 * @version 1.0.0
 * @since 1.0.0
 */