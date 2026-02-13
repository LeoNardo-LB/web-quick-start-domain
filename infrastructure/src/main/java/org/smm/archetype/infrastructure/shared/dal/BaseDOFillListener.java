package org.smm.archetype.infrastructure.shared.dal;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import org.smm.archetype.infrastructure.shared.util.context.ScopedThreadContext;

import java.time.Instant;
import java.util.Optional;

/**
 * 属性自动注入Handler，设置创建和更新时间。


 */
public class BaseDOFillListener implements InsertListener, UpdateListener {

    @Override
    public void onInsert(Object entity) {
        Instant now = Instant.now();
        if (entity instanceof BaseDO baseDO) {
            baseDO.setCreateTime(now);
            baseDO.setUpdateTime(now);
            Optional.ofNullable(ScopedThreadContext.getUserId()).ifPresent(userId -> {
                baseDO.setCreateUser(userId);
                baseDO.setUpdateUser(userId);
            });
        }
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseDO baseDO) {
            baseDO.setUpdateTime(Instant.now());
            Optional.ofNullable(ScopedThreadContext.getUserId()).ifPresent(baseDO::setUpdateUser);
        }
    }

}
