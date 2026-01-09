package org.smm.archetype.domain._shared.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 领域事件类型枚举
 *
 * <p>定义系统中所有领域事件的类型，提供类型安全和标准化。
 *
 * <p>枚举值命名规则：
 * <ul>
 *   <li>使用大写字母和下划线</li>
 *   <li>对应领域事件类的简单名称（大写+下划线）</li>
 *   <li>例如：OrderCreatedEvent → ORDER_CREATED</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 在DomainEvent中使用
 * public class OrderCreatedEvent extends DomainEvent {
 *     public OrderCreatedEvent() {
 *         super(EventType.ORDER_CREATED);
 *     }
 * }
 *
 * // 从字符串转换（数据库、HTTP等外部来源）
 * EventType type = EnumUtils.fromString("ORDER_CREATED", EventType.class, EventType.UNKNOWN);
 * }</pre>
 * @author Leonardo
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum EventType {

    /**
     * 订单创建事件
     */
    ORDER_CREATED("OrderCreatedEvent", "订单创建", EventPriority.HIGH),

    /**
     * 订单取消事件
     */
    ORDER_CANCELLED("OrderCancelledEvent", "订单取消", EventPriority.HIGH),

    /**
     * 订单支付事件
     */
    ORDER_PAID("OrderPaidEvent", "订单支付", EventPriority.HIGH),

    /**
     * 支付完成事件
     */
    PAYMENT_COMPLETED("PaymentCompletedEvent", "支付完成", EventPriority.HIGH),

    /**
     * 库存更新事件
     */
    INVENTORY_UPDATED("InventoryUpdatedEvent", "库存更新", EventPriority.HIGH),

    /**
     * 物流更新事件
     */
    SHIPPING_UPDATED("ShippingUpdatedEvent", "物流更新", EventPriority.HIGH),

    /**
     * 通知发送事件
     */
    NOTIFICATION_SENT("NotificationSentEvent", "通知发送", EventPriority.HIGH),

    /**
     * 日志创建事件
     */
    LOG_CREATED("LogCreatedEvent", "日志创建", EventPriority.LOW),

    /**
     * 统计更新事件
     */
    STATISTICS_UPDATED("StatisticsUpdatedEvent", "统计更新", EventPriority.LOW),

    /**
     * 文件上传事件
     */
    FILE_UPLOADED("FileUploadedEvent", "文件上传", EventPriority.LOW),

    /**
     * 文件删除事件
     */
    FILE_DELETED("FileDeletedEvent", "文件删除", EventPriority.HIGH),

    /**
     * 未知事件类型
     * <p>用于无法识别的事件类型的默认值
     */
    UNKNOWN("UnknownEvent", "未知事件", EventPriority.LOW);

    /**
     * 事件类名（对应的Java类名）
     */
    private final String eventClassName;

    /**
     * 事件描述（中文描述）
     */
    private final String description;

    /**
     * 事件优先级
     */
    private final EventPriority priority;

    /**
     * 根据事件类名获取事件类型
     * @param eventClassName 事件类名（如"OrderCreatedEvent"）
     * @return 事件类型枚举，未找到返回UNKNOWN
     */
    public static EventType fromEventClassName(String eventClassName) {
        if (eventClassName == null || eventClassName.isBlank()) {
            return UNKNOWN;
        }

        for (EventType type : values()) {
            if (type.eventClassName.equals(eventClassName)) {
                return type;
            }
        }

        return UNKNOWN;
    }

    /**
     * 根据事件类对象获取事件类型
     * @param clazz 事件类对象（如 OrderCreatedEvent.class）
     * @return 事件类型枚举，未找到返回UNKNOWN
     */
    public static EventType fromClass(Class<?> clazz) {
        if (clazz == null) {
            return UNKNOWN;
        }

        String className = clazz.getSimpleName();
        return fromEventClassName(className);
    }

    /**
     * 判断是否为核心业务事件
     * @return true-核心业务事件（订单、支付），false-非核心事件
     */
    public boolean isCoreBusiness() {
        return this == ORDER_CREATED || this == PAYMENT_COMPLETED;
    }

    /**
     * 判断是否为系统事件
     * @return true-系统事件（日志、统计），false-业务事件
     */
    public boolean isSystemEvent() {
        return this == LOG_CREATED || this == STATISTICS_UPDATED;
    }

    /**
     * 判断是否为文件事件
     * @return true-文件相关事件
     */
    public boolean isFileEvent() {
        return this == FILE_UPLOADED || this == FILE_DELETED;
    }

}
