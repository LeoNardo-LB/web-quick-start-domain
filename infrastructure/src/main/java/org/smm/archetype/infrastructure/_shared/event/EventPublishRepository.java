package org.smm.archetype.infrastructure._shared.event;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventPublishDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;

/**
 * 事件发布仓储
 *
 * <p>负责事件发布数据的持久化操作。
 * @author Leonardo
 * @since 2026/01/10
 */
@Slf4j
public class EventPublishRepository {

    private final EventPublishMapper eventPublishMapper;

    public EventPublishRepository(EventPublishMapper eventPublishMapper) {
        this.eventPublishMapper = eventPublishMapper;
    }

    /**
     * 根据事件ID查询事件发布记录
     * @param eventId 事件ID
     * @return 事件发布记录，不存在则返回 null
     */
    public EventPublishDO findByEventId(String eventId) {
        return eventPublishMapper.selectOneByQuery(
                QueryWrapper.create().where("event_id = ?", eventId)
        );
    }

    /**
     * 插入事件发布记录
     * @param publishDO 事件发布记录
     * @return 插入成功的行数
     */
    public int insert(EventPublishDO publishDO) {
        return eventPublishMapper.insert(publishDO);
    }

    /**
     * 乐观锁更新状态
     * @param publishDO 事件发布记录
     * @return true-更新成功，false-版本冲突
     */
    public boolean updateStatusWithVersion(EventPublishDO publishDO) {
        int rows = eventPublishMapper.updateByQuery(
                publishDO,
                QueryWrapper.create()
                        .where("id = ? AND version = ?", publishDO.getId(), publishDO.getVersion())
        );
        return rows > 0;
    }

}
