package org.smm.archetype.infrastructure.bizshared.dal;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import org.smm.archetype.infrastructure.bizshared.context.ContextHolder;
import org.smm.archetype.infrastructure.bizshared.context.impl.AccessContext;

import java.time.Instant;
import java.util.Optional;

/**
 * 属性自动注入handler
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
            Optional.ofNullable(ContextHolder.get(AccessContext.class)).map(AccessContext::getData).ifPresent(userId -> {
                baseDO.setCreateUser(userId);
                baseDO.setUpdateUser(userId);
            });
        }
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseDO baseDO) {
            baseDO.setUpdateTime(Instant.now());
            Optional.ofNullable(ContextHolder.get(AccessContext.class)).map(AccessContext::getData).ifPresent(baseDO::setUpdateUser);
        }
    }

}
