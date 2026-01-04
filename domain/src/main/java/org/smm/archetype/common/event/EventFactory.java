package org.smm.archetype.common.event;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import org.smm.archetype.shared.base.BaseEvent;
import org.smm.archetype.shared.base.BaseEvent.Source;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/31
 */
public class EventFactory {

    private static final SnowflakeGenerator generator = new SnowflakeGenerator();

    public static AccessEvent createAccessEvent(Source source, String userCode) {
        return AccessEvent.builder()
                       .setId(generator.next())
                       .setCreateUser(userCode)
                       .setSource(source)
                       .setType(BaseEvent.Type.ACCESS)
                       .build();
    }

}
