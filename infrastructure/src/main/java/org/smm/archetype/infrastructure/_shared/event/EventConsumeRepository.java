package org.smm.archetype.infrastructure._shared.event;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventConsumeDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventConsumeMapper;

import java.time.Instant;
import java.util.List;

/**
 * 事件消费仓储
 *
 * <p>封装 EventConsumeMapper，提供业务语义的查询方法和乐观锁更新。
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@RequiredArgsConstructor
public class EventConsumeRepository {

    private final EventConsumeMapper eventConsumeMapper;

    /**
     * 根据幂等键查询消费记录
     * @param idempotentKey 幂等键
     * @return 消费记录，不存在则返回 null
     */
    public EventConsumeDO findByIdempotentKey(String idempotentKey) {
        return eventConsumeMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("idempotent_key = ?", idempotentKey)
        );
    }

    /**
     * 插入消费记录
     * @param consumeDO 消费记录
     * @return 插入成功的行数
     */
    public int insert(EventConsumeDO consumeDO) {
        return eventConsumeMapper.insert(consumeDO);
    }

    /**
     * 乐观锁更新状态
     * @param consumeDO 消费记录（必须包含 id 和 version）
     * @return true-更新成功，false-版本冲突
     */
    public boolean updateStatusWithVersion(EventConsumeDO consumeDO) {
        int rows = eventConsumeMapper.updateByQuery(
                consumeDO,
                QueryWrapper.create()
                        .where("id = ? AND version = ?", consumeDO.getId(), consumeDO.getVersion())
        );
        return rows > 0;
    }

    /**
     * 查询待处理事件（READY 或 RETRY 状态）
     * @param consumeStatus 消费状态列表
     * @param priority      优先级（HIGH/LOW）
     * @param limit         限制数量
     * @return 消费记录列表
     */
    public List<EventConsumeDO> findPendingEvents(List<String> consumeStatus, String priority, int limit) {
        return eventConsumeMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("consume_status IN (" + String.join(",", consumeStatus.stream().map(s -> "'" + s + "'").toList()) + ")")
                        .and("priority = ?", priority)
                        .and("(next_retry_time IS NULL OR next_retry_time <= ?)", Instant.now())
                        .orderBy("retry_times", false)
                        .orderBy("create_time", true)
                        .limit(limit)
        );
    }

    /**
     * 查询待重试事件
     * @param limit 限制数量
     * @return 消费记录列表
     */
    public List<EventConsumeDO> findRetryEvents(int limit) {
        return eventConsumeMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("consume_status = 'RETRY'")
                        .and("(next_retry_time IS NULL OR next_retry_time <= ?)", Instant.now())
                        .orderBy("retry_times", false)
                        .orderBy("create_time", true)
                        .limit(limit)
        );
    }

    /**
     * 查询失败事件
     * @param limit 限制数量
     * @return 消费记录列表
     */
    public List<EventConsumeDO> findFailedEvents(int limit) {
        return eventConsumeMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("consume_status = 'FAILED'")
                        .orderBy("create_time", false)
                        .limit(limit)
        );
    }

    /**
     * 根据事件 ID 查询消费记录
     * @param eventId 事件 ID
     * @return 消费记录列表
     */
    public List<EventConsumeDO> findByEventId(String eventId) {
        return eventConsumeMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("event_id = ?", eventId)
        );
    }

    /**
     * 根据事件 ID 和消费者组查询消费记录
     * @param eventId       事件 ID
     * @param consumerGroup 消费者组
     * @param consumerName  消费者名称
     * @return 消费记录，不存在则返回 null
     */
    public EventConsumeDO findByEventIdAndConsumer(String eventId, String consumerGroup, String consumerName) {
        return eventConsumeMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("event_id = ? AND consumer_group = ? AND consumer_name = ?",
                                eventId, consumerGroup, consumerName)
        );
    }

}
