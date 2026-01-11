package org.smm.archetype.domain._shared.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * 实体基类
 *
 * <p>实体特征：
 * <ul>
 *   <li>有唯一标识</li>
 *   <li>可变性</li>
 *   <li>通过业务方法修改状态，而不是直接setter</li>
 *   <li>封装业务规则</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>移除@Setter以保护封装性</li>
 *   <li>通过业务方法修改状态</li>
 *   <li>在业务方法中维护不变性约束</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public class Order extends AggregateRoot {
 *     private OrderStatus status;
 *     private BigDecimal totalAmount;
 *
 *     // 通过业务方法修改状态，而不是直接setter
 *     public void pay(PaymentMethod method) {
 *         if (this.status != OrderStatus.CREATED) {
 *             throw new IllegalStateException("Only created orders can be paid");
 *         }
 *         this.status = OrderStatus.PAID;
 *         this.paymentMethod = method;
 *         this.paymentTime = Instant.now();
 *
 *         // 发布领域事件
 *         addDomainEvent(new OrderPaidEvent(this.orderId, this.totalAmount));
 *     }
 * }
 * }</pre>
 * @author Leonardo
 * @since 2025/7/14
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public abstract class Entity implements Identifier {

    /**
     * 受保护的默认构造函数
     * <p>供子类工厂方法使用
     */
    protected Entity() {
    }

    /**
     * 唯一标识
     */
    protected Long id;

    /**
     * 创建时间
     */
    protected Instant createTime;

    /**
     * 更新时间
     */
    protected Instant updateTime;

    /**
     * 创建人
     */
    protected String createUser;

    /**
     * 更新人
     */
    protected String updateUser;

    /**
     * 版本号（用于乐观锁）
     */
    protected Long version;

    /**
     * 标记为已创建
     *
     * <p>在实体创建时调用此方法设置创建时间。
     */
    protected void markAsCreated() {
        if (this.createTime == null) {
            this.createTime = Instant.now();
            this.updateTime = this.createTime;
            this.version = 0L;
        }
    }

    /**
     * 标记为已更新
     *
     * <p>在实体的业务方法中修改状态后调用此方法更新时间戳和版本号。
     */
    protected void markAsUpdated() {
        this.updateTime = Instant.now();
        if (this.version != null) {
            this.version++;
        }
    }

    /**
     * 检查是否为新创建的实体
     * @return 如果ID为null返回true
     */
    public boolean isNew() {
        return this.id == null;
    }

    /**
     * 检查是否已持久化
     * @return 如果ID不为null返回true
     */
    public boolean isPersisted() {
        return this.id != null;
    }

}