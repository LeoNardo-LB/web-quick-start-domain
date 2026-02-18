package org.smm.archetype.infrastructure.shared.dal;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.smm.archetype.infrastructure.shared.util.context.ScopedThreadContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * MyBatis-Plus 元数据自动填充处理器。
 * 替代原 MyBatis-Flex 的 BaseDOFillListener，实现 createTime、updateTime 等字段的自动填充。
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Instant now = Instant.now();
        this.strictInsertFill(metaObject, "createTime", Instant.class, now);
        this.strictInsertFill(metaObject, "updateTime", Instant.class, now);
        Optional.ofNullable(ScopedThreadContext.getUserId()).ifPresent(userId -> {
            this.strictInsertFill(metaObject, "createUser", String.class, userId);
            this.strictInsertFill(metaObject, "updateUser", String.class, userId);
        });
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", Instant.class, Instant.now());
        Optional.ofNullable(ScopedThreadContext.getUserId())
                .ifPresent(userId -> this.strictUpdateFill(metaObject, "updateUser", String.class, userId));
    }

}
