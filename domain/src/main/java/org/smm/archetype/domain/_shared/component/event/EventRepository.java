package org.smm.archetype.domain._shared.component.event;

import org.smm.archetype.domain._shared.base.BaseEvent;
import org.smm.archetype.domain._shared.base.BaseRepository;

import java.util.List;

/**
 * 事件仓储接口
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public interface EventRepository extends BaseRepository<BaseEvent<?>> {

    /**
     * 查询处于就绪状态的事件
     * @param limit 限制数量
     * @return 事件列表
     */
    List<BaseEvent<?>> findReadyEvents(int limit);

}