package org.smm.archetype.common.event;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.smm.archetype.shared.base.BaseEvent;
import org.smm.archetype.common.event.dal.entity.EventPublishDO;
import org.smm.archetype.common.event.dal.mapper.EventPublishMapper;
import org.springframework.stereotype.Repository;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/31
 */
@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private final EventPublishMapper eventPublishMapper;

    @Override
    public void insert(BaseEvent<?> model) {
        EventPublishDO publishDO = new EventPublishDO();
        publishDO.setPrevId(model.getPrevId());
        publishDO.setStep(model.getStep());
        publishDO.setSource(model.getSource().name());
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

}
