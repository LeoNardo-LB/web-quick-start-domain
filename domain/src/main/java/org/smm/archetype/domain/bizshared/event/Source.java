package org.smm.archetype.domain.bizshared.event;

import lombok.Getter;

/**
 * 来源，标识上游系统
 */
@Getter
public enum Source {

    /**
     * 内部
     */
    DOMAIN,

    // xxx外部具体系统...
}
