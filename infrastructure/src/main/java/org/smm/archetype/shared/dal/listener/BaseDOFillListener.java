package org.smm.archetype.shared.dal.listener;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import org.smm.archetype.shared.base.BaseModel;
import org.smm.archetype.shared.context.ContextHolder;
import org.smm.archetype.shared.context.impl.AccessContext;
import org.smm.archetype.shared.context.impl.EventContext;
import org.smm.archetype.shared.dal.entity.BaseDO;

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
            Optional.ofNullable(ContextHolder.get(AccessContext.class))
                    .map(EventContext::getData)
                    .map(BaseModel::getCreateUser)
                    .ifPresent(userId -> {
                        baseDO.setCreateUser(userId);
                        baseDO.setUpdateUser(userId);
                    });
        }
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseDO baseDO) {
            baseDO.setUpdateTime(Instant.now());
            Optional.ofNullable(ContextHolder.get(AccessContext.class))
                    .map(AccessContext::getData)
                    .map(BaseModel::getCreateUser)
                    .ifPresent(baseDO::setUpdateUser);
        }
    }

}
