package org.smm.archetype.domain._shared.base;

import lombok.Getter;
import org.smm.archetype.domain._shared.event.EventPriority;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件基类
 *
 * <p>领域事件特征：
 * <ul>
 *   <li>不可变性（Immutable）</li>
 *   <li>表示领域内已发生的事实</li>
 *   <li>使用过去式命名（如OrderCreated、PaymentCompleted）</li>
 *   <li>包含事件发生时间和唯一标识</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public class OrderCreatedEvent extends DomainEvent {
 *     private final Long orderId;
 *     private final CustomerId customerId;
 *     private final BigDecimal totalAmount;
 *
 *     public OrderCreatedEvent(Long orderId, CustomerId customerId, BigDecimal totalAmount) {
 *         super();
 *         this.orderId = orderId;
 *         this.customerId = customerId;
 *         this.totalAmount = totalAmount;
 *     }
 * }
 * }</pre>
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public abstract class DomainEvent extends ValueObject {

    /**
     * 事件唯一标识
     */
    private final String eventId;

    /**
     * 事件发生时间
     */
    private final Instant occurredOn;

    /**
     * 事件类型（类名的简单名称）
     */
    private final String eventType;

    /**
     * 聚合根ID（事件所属的聚合根）
     */
    private String aggregateId;

    /**
     * 聚合根类型
     */
    private String aggregateType;

    /**
     * 事件优先级（默认低优先级）
     */
    private EventPriority priority = EventPriority.LOW;

    /**
     * 最大重试次数（默认3次）
     * <p>由发布方根据事件类型决定
     */
    private Integer maxRetryTimes = 3;

    protected DomainEvent() {
        this.eventId = generateEventId();
        this.occurredOn = Instant.now();
        this.eventType = this.getClass().getSimpleName();
    }

    /**
     * 设置事件优先级
     * @param priority 优先级
     */
    public void setPriority(EventPriority priority) {
        if (this.priority == null || this.priority == EventPriority.LOW) {
            this.priority = priority;
        }
    }

    /**
     * 设置最大重试次数
     * @param maxRetryTimes 最大重试次数
     */
    public void setMaxRetryTimes(Integer maxRetryTimes) {
        if (this.maxRetryTimes == null || this.maxRetryTimes == 3) {
            this.maxRetryTimes = maxRetryTimes;
        }
    }

    /**
     * 生成事件唯一ID
     * @return 事件ID
     */
    private String generateEventId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 设置聚合根信息
     *
     * <p>此方法由框架在事件发布时调用，用于记录事件与聚合根的关联关系。
     * @param aggregateId   聚合根ID
     * @param aggregateType 聚合根类型
     */
    public void setAggregateInfo(String aggregateId, String aggregateType) {
        if (this.aggregateId == null) {
            this.aggregateId = aggregateId;
            this.aggregateType = aggregateType;
        }
    }

}
