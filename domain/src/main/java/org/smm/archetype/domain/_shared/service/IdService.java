package org.smm.archetype.domain._shared.service;

/**
 *
 *
 * @author Leonardo
 * @since 2026/1/7
 */
public interface IdService {

    /**
     * 生成id
     * @param type id 类型
     * @return id
     */
    String generateId(Type type);

    /**
     * id 类型
     */
    enum Type {

    }

}
