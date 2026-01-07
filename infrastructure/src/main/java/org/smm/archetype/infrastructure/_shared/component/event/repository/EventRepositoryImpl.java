package org.smm.archetype.infrastructure._shared.component.event.repository;

import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.smm.archetype.domain._shared.component.event.EventRepository;
import org.smm.archetype.domain._shared.base.BaseEvent;
import org.smm.archetype.infrastructure._shared.component.event.DOEventFactory;
import org.smm.archetype.infrastructure._shared.component.event.repository.entity.EventPublishDO;
import org.smm.archetype.infrastructure._shared.component.event.repository.mapper.EventPublishMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static org.smm.archetype.infrastructure.common.event.repository.entity.table.EventPublishDOTableDef.EVENT_PUBLISH_DO;

/**
 * 事件仓储实现
 * @author Leonardo
 * @since 2025/12/31
 */
@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    // 字段列常量
    private final EventPublishMapper eventPublishMapper;

    private final DOEventFactory<BaseEvent<?>> doEventFactory;

    @Override
    public void insert(BaseEvent<?> model) {
        EventPublishDO publishDO = new EventPublishDO();
        publishDO.setPrevId(model.getPrevId());
        publishDO.setStep(model.getStep());
        publishDO.setType(model.getType().name());
        publishDO.setData(JSON.toJSONString(model.getData()));
        publishDO.setStatus(model.getStatus().name());
        publishDO.setId(model.getId());
        publishDO.setCreateTime(model.getCreateTime());
        publishDO.setUpdateTime(model.getUpdateTime());
        publishDO.setCreateUser(model.getCreateUser());
        publishDO.setUpdateUser(model.getUpdateUser());
        eventPublishMapper.insert(publishDO);
    }

    @Override
    public List<BaseEvent<?>> findReadyEvents(int limit) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .select()
                                            .from(EventPublishDO.class)
                                            .where(EVENT_PUBLISH_DO.STATUS.eq(BaseEvent.Status.READY.name()))
                                            .orderBy(EVENT_PUBLISH_DO.CREATE_TIME, true)
                                            .limit(limit);

        List<EventPublishDO> list = eventPublishMapper.selectListByQuery(queryWrapper);
        return list.stream()
                       .map(doEventFactory::createEvent)
                       .collect(Collectors.toList());
    }

}
