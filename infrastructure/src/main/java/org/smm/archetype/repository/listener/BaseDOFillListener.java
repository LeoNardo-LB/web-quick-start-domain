package org.smm.archetype.repository.listener;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import org.smm.archetype.common.context.ContextHolder;
import org.smm.archetype.common.event.AccessEvent;
import org.smm.archetype.repository.entity.BaseDO;

import java.time.Instant;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public class BaseDOFillListener implements InsertListener, UpdateListener {

    @Override
    public void onInsert(Object entity) {
        Instant now = Instant.now();
        if (entity instanceof BaseDO baseDO) {
            baseDO.setCreateTime(now);
            baseDO.setUpdateTime(now);
            ContextHolder.get(AccessEvent.class).map(AccessEvent::getUserId).ifPresent(userId -> {
                baseDO.setCreateUser(userId);
                baseDO.setUpdateUser(userId);
            });
        }
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseDO baseDO) {
            baseDO.setUpdateTime(Instant.now());
            ContextHolder.get(AccessEvent.class).map(AccessEvent::getUserId).ifPresent(baseDO::setUpdateUser);
        }
    }

}
