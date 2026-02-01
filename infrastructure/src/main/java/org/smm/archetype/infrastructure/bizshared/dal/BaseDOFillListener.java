package org.smm.archetype.infrastructure.bizshared.dal;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import org.smm.archetype.infrastructure.bizshared.util.context.MyContext;

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
            Optional.ofNullable(MyContext.getUserId()).ifPresent(userId -> {
                baseDO.setCreateUser(userId);
                baseDO.setUpdateUser(userId);
            });
        }
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseDO baseDO) {
            baseDO.setUpdateTime(Instant.now());
            Optional.ofNullable(MyContext.getUserId()).ifPresent(baseDO::setUpdateUser);
        }
    }

}
