/**
 * 适配层（Adapter Layer）
 *
 * <p>职责：作为系统的最外层，负责与外部系统的交互和适配</p>
 *
 * <h3>核心职责</h3>
 * <ul>
 *   <li>接收外部请求（HTTP、消息队列、RPC等）</li>
 *   <li>参数验证和转换</li>
 *   <li>调用应用服务</li>
 *   <li>返回响应结果</li>
 * </ul>
 *
 * <h3>子包结构</h3>
 * <ul>
 *   <li>{@code _shared} - 共享组件（枚举、返回结果、工具类）</li>
 *   <li>{@code access} - 接入适配器（Web、Listener、Schedule等）</li>
 * </ul>
 *
 * <h3>架构约束</h3>
 * <ul>
 *   <li>✅ 可以调用：App层、Domain层</li>
 *   <li>❌ 禁止：直接调用Infrastructure层</li>
 *   <li>❌ 禁止：包含业务逻辑</li>
 *   <li>❌ 禁止：直接返回DO/Entity</li>
 * </ul>
 * @see org.smm.archetype.app 应用层
 * @see org.smm.archetype.domain 领域层
 */
package org.smm.archetype.adapter;
