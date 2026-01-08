package org.smm.archetype.infrastructure.common.log;

import lombok.RequiredArgsConstructor;
import org.smm.archetype.domain.common.log.Log;
import org.smm.archetype.domain.common.log.LogDataAccessor;
import org.smm.archetype.domain.common.log.handler.persistence.PersistenceHandler;
import org.smm.archetype.domain.common.log.handler.persistence.PersistenceType;
import org.springframework.stereotype.Component;

/**
 * 数据库持久化处理器
 *
 * <p>应用层服务，直接调用 DataAccessor 完成持久化。
 *
 * <p>转换逻辑由 DataAccessor 实现内部处理。
 * @author Leonardo
 * @since 2025/7/15
 */
@Component
@RequiredArgsConstructor
public class DbPersistenceHandler implements PersistenceHandler {

    private final LogDataAccessor logDataAccessor;

    @Override
    public PersistenceType getPersistenceType() {
        return PersistenceType.DB;
    }

    @Override
    public void persist(Log log) {
        // 直接调用 DataAccessor，转换逻辑由 DataAccessor 内部处理
        logDataAccessor.save(log);
    }

}
