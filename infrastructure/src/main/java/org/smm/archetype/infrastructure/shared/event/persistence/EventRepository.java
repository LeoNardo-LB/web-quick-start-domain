package org.smm.archetype.infrastructure.shared.event.persistence;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.event.Action;
import org.smm.archetype.domain.shared.event.Source;
import org.smm.archetype.domain.shared.event.Status;
import org.smm.archetype.domain.shared.event.Type;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.EventDO;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.EventMapper;
import org.smm.archetype.infrastructure.shared.event.EventRecordConverter;

import java.time.Instant;
import java.util.List;

/**
 * 事件仓储
 *
 封装 EventMapper，提供事件发布和消费的持久化操作。
 

 */
@Slf4j
@RequiredArgsConstructor
public class EventRepository {

    private final EventMapper eventMapper;
    private final EventRecordConverter recordConverter;

    // ==================== 通用方法 ====================

    /**
     * 插入事件记录
     * @param eventDO 事件记录
     * @return 插入成功的行数
     */
    public int insert(EventDO eventDO) {
        return eventMapper.insert(eventDO);
    }

    /**
     * 乐观锁更新状态
     * @param eventDO 事件记录（必须包含 id 和 version）
     * @return true-更新成功，false-版本冲突
     */
    public boolean updateStatusWithVersion(EventDO eventDO) {
        int rows = eventMapper.updateByQuery(
                eventDO,
                QueryWrapper.create()
                        .where("id = ?", eventDO.getId())
        );
        return rows > 0;
    }

    // ==================== 发布相关方法 ====================

    /**
     * 根据事件ID查询事件发布记录
     * @param eventId 事件ID
     * @return 事件发布记录，不存在则返回 null
     */
    public EventDO findPublishByEventId(String eventId) {
        return eventMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("eid = ?", eventId)
                        .and("action = 'PUBLISH'")
        );
    }

    // ==================== 消费相关方法 ====================

    /**
     * 根据事件 ID 查询消费记录
     * @param eventId 事件 ID
     * @return 消费记录，不存在则返回 null
     */
    public EventDO findConsumeByEventId(String eventId) {
        return eventMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("eid = ?", eventId)
                        .and("action = 'CONSUME'")
        );
    }

    /**
     * 根据事件 ID 和消费者组查询消费记录
     * @param eventId       事件 ID
     * @param consumerGroup 消费者组
     * @param consumerName  消费者名称
     * @return 消费记录，不存在则返回 null
     */
    public EventDO findByEventIdAndConsumer(String eventId, String consumerGroup, String consumerName) {
        return eventMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("eid = ? AND executor_group = ? AND executor = ?",
                                eventId, consumerGroup, consumerName)
                        .and("action = 'CONSUME'")
        );
    }

    /**
     * 查询待处理事件（READY 或 RETRY 状态）
     * @param consumeStatus 消费状态列表
     * @param limit         限制数量
     * @return 消费记录列表
     */
    public List<EventDO> findPendingConsumeEvents(List<String> consumeStatus, int limit) {
        String statusIn = String.join(",",
                consumeStatus.stream().map(s -> "'" + s + "'").toList());
        return eventMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status IN (" + statusIn + ")")
                        .and("action = 'CONSUME'")
                        .and("(next_retry_time IS NULL OR next_retry_time <= ?)", Instant.now())
                        .orderBy("retry_times", false)
                        .orderBy("create_time", true)
                        .limit(limit)
        );
    }

    /**
     * 根据幂等键查询消费记录
     *
    幂等键组成：eid + action + executor_group + delete_time
     * @param eventId       事件ID
     * @param action        动作类型（CONSUME）
     * @param executorGroup 执行者组
     * @param deleteTime    删除时间（0表示未删除）
     * @return 消费记录，不存在则返回 null
     */
    public EventDO findByIdempotentKey(String eventId, String action, String executorGroup, Long deleteTime) {
        return eventMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("eid = ?", eventId)
                        .and("action = ?", action)
                        .and("executor_group = ?", executorGroup)
                        .and("delete_time = ?", deleteTime)
        );
    }

    /**
     * 查询待重试事件（RETRYING 状态且到达重试时间）
     *
     * 返回 EventConsumeRecord 而非 EventDO，避免上层直接依赖 Infrastructure 层实体。
     * @param limit 限制数量
     * @return 消费记录列表
     */
    public List<EventConsumeRecord> findRetryConsumeEvents(int limit) {
        List<EventDO> eventDOs = eventMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status = 'RETRYING'")
                        .and("action = 'CONSUME'")
                        .and("next_retry_time <= ?", Instant.now())
                        .and("delete_time = 0")
                        .orderBy("retry_times", true)
                        .orderBy("create_time", true)
                        .limit(limit)
        );
        return eventDOs.stream().map(recordConverter::from).toList();
    }

    // ==================== 转换方法 ====================
    /**
     * 将 EventDO 转换为 EventConsumeRecord
     *
     * 用于避免上层直接依赖 Infrastructure 层实体。
     * @param eventDO 事件 DO
     * @return 事件消费记录
     */
    private EventConsumeRecord toConsumeRecordOld(EventDO eventDO) {
        // 查询关联的发布记录以获取 payload
        EventDO publishDO = findPublishByEventId(eventDO.getEid());
        String payload = publishDO != null ? publishDO.getPayload() : null;

        return EventConsumeRecord.builder()
                       .setEid(eventDO.getEid())
                       .setAction(Action.valueOf(eventDO.getAction()))
                       .setSource(Source.valueOf(eventDO.getSource()))
                       .setType(Type.valueOf(eventDO.getType()))
                       .setStatus(Status.valueOf(eventDO.getStatus()))
                       .setPayload(payload)
                       .setExecutor(eventDO.getExecutor())
                       .setExecutorGroup(eventDO.getExecutorGroup())
                       .setMessage(eventDO.getMessage())
                       .setTraceId(eventDO.getTraceId())
                       .setRetryTimes(eventDO.getRetryTimes())
                       .setNextRetryTime(eventDO.getNextRetryTime())
                       .setMaxRetryTimes(eventDO.getMaxRetryTimes())
                       .build();
    }

    // ==================== 转换方法 ====================

    /**
     * 查询失败事件
     * @param limit 限制数量
     * @return 消费记录列表
     */
    public List<EventDO> findFailedConsumeEvents(int limit) {
        return eventMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status = 'FAILED'")
                        .and("action = 'CONSUME'")
                        .orderBy("create_time", false)
                        .limit(limit)
        );
    }

}
