package org.smm.archetype.infrastructure._shared.context;

import cn.hutool.core.bean.BeanUtil;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public interface Context<T> {

    T getData();

    default Context<?> export() {
        return BeanUtil.toBean(this, this.getClass());
    }

}
