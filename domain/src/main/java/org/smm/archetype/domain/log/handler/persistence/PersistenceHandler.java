package org.smm.archetype.domain.log.handler.persistence;

import org.smm.archetype.domain.log.LogDto;

/**
 * 日志持久化处理器接口
 *
 * 定义日志持久化处理器的标准接口，不同的持久化方式需要实现此接口。
 * 通过工厂模式根据持久化类型获取对应的处理器实例。
 */
public interface PersistenceHandler {

    /**
     * 获取持久化类型
     *
     * 返回当前处理器支持的持久化类型，用于在处理器工厂中建立类型与处理器的映射关系。
     * @return 持久化类型枚举值
     */
    PersistenceType getPersistenceType();

    /**
     * 执行持久化操作
     *
     * 根据具体的持久化策略，将日志信息保存到相应的存储介质中。
     * @param LogDto 日志数据传输对象，包含待持久化的日志信息
     */
    void persist(LogDto LogDto);

}
