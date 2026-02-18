package org.smm.archetype.infrastructure.shared.dal.generated.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.EventDO;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.EventMapper;
import org.smm.archetype.infrastructure.shared.dal.generated.service.IEventService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 事件发布表 服务实现类
 * </p>
 * @author CodeGenerator
 * @since 2026-02-17
 */
@Service
public class EventServiceImpl extends ServiceImpl<EventMapper, EventDO> implements IEventService {

}
