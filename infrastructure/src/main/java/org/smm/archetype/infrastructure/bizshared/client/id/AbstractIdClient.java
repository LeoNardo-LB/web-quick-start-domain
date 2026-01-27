package org.smm.archetype.infrastructure.bizshared.client.id;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.client.IdClient;

/**
 * ID 生成服务抽象基类
 *
 * <p>实现 ID 生成的通用流程模板，定义扩展点供子类实现具体算法。
 *
 * <p>核心功能：
 * <ul>
 *   <li>ID 类型校验</li>
 *   <li>ID 生成流程模板</li>
 *   <li>异常处理与日志记录</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
public abstract class AbstractIdClient implements IdClient {

    /**
     * 生成 ID（模板方法）
     *
     * <p>定义 ID 生成的通用流程：
     * <ol>
     *   <li>校验 ID 类型是否为空</li>
     *   <li>调用子类实现的具体生成算法</li>
     *   <li>记录日志</li>
     * </ol>
     * @param type ID 类型
     * @return ID 字符串
     * @throws IllegalArgumentException 当 type 为 null 时抛出
     */
    @Override
    public final String generateId(Type type) {
        // 1. 参数校验
        if (type == null) {
            log.error("ID type cannot be null");
            throw new IllegalArgumentException("ID type cannot be null");
        }

        // 2. 调用子类实现的具体算法
        String id = doGenerateId(type);

        // 3. 记录日志
        log.debug("Generated ID: type={}, id={}", type, id);

        return id;
    }

    /**
     * 执行实际的 ID 生成逻辑（由子类实现）
     *
     * <p>扩展点：子类实现具体的 ID 生成算法，如：
     * <ul>
     *   <li>Snowflake 算法</li>
     *   <li>UUID 算法</li>
     *   <li>数据库自增</li>
     *   <li>Redis 序列</li>
     * </ul>
     * @param type ID 类型
     * @return ID 字符串
     */
    protected abstract String doGenerateId(Type type);

}
