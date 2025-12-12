package org.smm.archetype.domain.log.handler.persistence;

/**
 * 日志持久化类型枚举
 *
 * 定义支持的日志持久化方式，包括文件存储和数据库存储。
 */
public enum PersistenceType {
    /**
     * 文件存储
     *
     * 将日志信息以文件形式存储，通常用于开发环境或简单的日志记录需求。
     */
    FILE,
    /**
     * 数据库存储
     *
     * 将日志信息存储到数据库中，适用于需要长期保存和查询分析的日志记录需求。
     */
    DB
}
