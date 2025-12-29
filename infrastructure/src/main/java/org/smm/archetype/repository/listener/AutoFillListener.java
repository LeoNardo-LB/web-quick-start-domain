package org.smm.archetype.repository.listener;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import org.smm.archetype.common.context.ContextHolder;
import org.smm.archetype.dto.UserAuthInfo;
import org.smm.archetype.repository.entity.BaseDO;

import java.lang.reflect.Type;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public class AutoFillListener implements InsertListener, UpdateListener {

    @Override
    public void onInsert(Object entity) {
        if (entity instanceof BaseDO baseDO) {
            ContextHolder.get(UserAuthInfo.class).map(UserAuthInfo::getUserId).ifPresent(userId -> {
                baseDO.setCreateUser(userId);
                baseDO.setUpdateUser(userId);
            });
        }
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseDO baseDO) {
            ContextHolder.get(UserAuthInfo.class).map(UserAuthInfo::getUserId).ifPresent(baseDO::setUpdateUser);
        }
    }

}
