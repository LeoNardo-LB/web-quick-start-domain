package org.smm.archetype.domain.example.order.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain._shared.base.AggregateRoot;
import org.smm.archetype.domain.example.order.event.OrderCancelledEvent;
import org.smm.archetype.domain.example.order.event.OrderCompletedEvent;
import org.smm.archetype.domain.example.order.event.OrderCreatedEvent;
import org.smm.archetype.domain.example.order.event.OrderPaidEvent;
import org.smm.archetype.domain.example.order.event.OrderShippedEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 订单聚合根
 *
 * <p>订单是整个订单管理的聚合根，负责：
 * <ul>
 *   <li>维护订单的完整性和一致性</li>
 *   <li>封装订单业务规则</li>
 *   <li>发布订单领域事件</li>
 * </ul>
 *
 * <p>重要原则：
 * <ul>
 *   <li>不要暴露内部集合（防御性拷贝）</li>
 *   <li>通过业务方法修改状态</li>
 *   <li>在业务方法中维护不变性约束</li>
 *   <li>发布领域事件记录重要变化</li>
 * </ul>
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public class Order extends AggregateRoot {

    /**
     * 订单项列表
     * 注意：不要直接暴露这个列表，使用防御性拷贝
     */
    @Getter(AccessLevel.NONE)
    private final List<OrderItem> items = new ArrayList<>();
    /**
     * 订单ID
     */
    private Long orderId;
    /**
     * 客户ID
     */
    private Long customerId;
    /**
     * 订单总金额
     */
    private Money totalAmount;

    /**
     * 订单状态
     */
    private OrderStatus status;

    /**
     * 收货地址
     */
    private String shippingAddress;

    /**
     * 联系电话
     */
    private String phoneNumber;

    /**
     * 支付时间
     */
    private Instant paymentTime;

    /**
     * 发货时间
     */
    private Instant shippingTime;

    /**
     * 完成时间
     */
    private Instant completedTime;

    /**
     * 取消时间
     */
    private Instant cancelledTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 工厂方法：创建订单
     * @param customerId      客户ID
     * @param items           订单项列表
     * @param shippingAddress 收货地址
     * @param phoneNumber     联系电话
     * @return 新创建的订单
     */
    public static Order create(Long customerId, List<OrderItem> items, String shippingAddress, String phoneNumber) {
        OrderBuilder<?, ?> builder = Order.builder();
        builder.orderId = null; // 暂时为null，保存后由数据库生成
        builder.customerId = customerId;
        builder.totalAmount = calculateTotal(items);
        builder.status = OrderStatus.CREATED;
        builder.shippingAddress = shippingAddress;
        builder.phoneNumber = phoneNumber;
        Order order = builder.build();
        order.items.addAll(items);
        order.markAsCreated();

        // 发布订单创建事件
        order.addDomainEvent(new OrderCreatedEvent(order.orderId, customerId, order.totalAmount));

        return order;
    }

    /**
     * 计算订单总金额
     * @param items 订单项列表
     * @return 总金额
     */
    private static Money calculateTotal(List<OrderItem> items) {
        return items.stream()
                       .map(OrderItem::getSubtotal)
                       .reduce(Money.zero(), Money::add);
    }

    /**
     * 重建订单（用于从数据库加载）
     *
     * <p>这是一个public静态方法，供Repository实现类使用。
     * @param orderId         订单ID
     * @param customerId      客户ID
     * @param items           订单项列表
     * @param totalAmount     总金额
     * @param status          订单状态
     * @param shippingAddress 收货地址
     * @param phoneNumber     联系电话
     * @param paymentTime     支付时间
     * @param shippingTime    发货时间
     * @param completedTime   完成时间
     * @param cancelledTime   取消时间
     * @param cancelReason    取消原因
     * @param id              数据库ID
     * @param createTime      创建时间
     * @param updateTime      更新时间
     * @param createUser      创建人
     * @param updateUser      更新人
     * @param version         版本号
     * @return 重建的订单对象
     */
    public static Order reconstruct(
            Long orderId,
            Long customerId,
            List<OrderItem> items,
            Money totalAmount,
            OrderStatus status,
            String shippingAddress,
            String phoneNumber,
            Instant paymentTime,
            Instant shippingTime,
            Instant completedTime,
            Instant cancelledTime,
            String cancelReason,
            Long id,
            Instant createTime,
            Instant updateTime,
            String createUser,
            String updateUser,
            Long version
    ) {
        // 使用反射设置值，因为builder字段是private的
        Order order = Order.builder()
                              .setOrderId(orderId)
                              .setCustomerId(customerId)
                              .setTotalAmount(totalAmount)
                              .setStatus(status)
                              .setShippingAddress(shippingAddress)
                              .setPhoneNumber(phoneNumber)
                              .setPaymentTime(paymentTime)
                              .setShippingTime(shippingTime)
                              .setCompletedTime(completedTime)
                              .setCancelledTime(cancelledTime)
                              .setCancelReason(cancelReason)
                              .setCreateTime(createTime)
                              .setUpdateTime(updateTime)
                              .setCreateUser(createUser)
                              .setUpdateUser(updateUser)
                              .setVersion(version)
                              .build();

        // 手动设置id（因为set方法需要Long而不是Long）
        order.id = id;

        // 添加订单项
        order.items.addAll(items);

        return order;
    }

    /**
     * 支付订单
     * @param paymentMethod 支付方式
     */
    public void pay(String paymentMethod) {
        if (!status.canPay()) {
            throw new IllegalStateException("Order cannot be paid in current status: " + status);
        }

        this.status = OrderStatus.PAID;
        this.paymentTime = Instant.now();
        this.markAsUpdated();

        // 发布订单支付事件
        addDomainEvent(new OrderPaidEvent(this.orderId, this.customerId, this.totalAmount, paymentMethod));
    }

    /**
     * 发货
     * @param trackingNumber 物流单号
     */
    public void ship(String trackingNumber) {
        if (!status.canShip()) {
            throw new IllegalStateException("Order cannot be shipped in current status: " + status);
        }

        this.status = OrderStatus.SHIPPED;
        this.shippingTime = Instant.now();
        this.markAsUpdated();

        // 发布订单发货事件
        addDomainEvent(new OrderShippedEvent(this.orderId, this.customerId, trackingNumber));
    }

    /**
     * 完成订单
     */
    public void complete() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order cannot be completed in current status: " + status);
        }

        this.status = OrderStatus.COMPLETED;
        this.completedTime = Instant.now();
        this.markAsUpdated();

        // 发布订单完成事件
        addDomainEvent(new OrderCompletedEvent(this.orderId, this.customerId));
    }

    /**
     * 取消订单
     * @param reason 取消原因
     */
    public void cancel(String reason) {
        if (status.canCancel()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + status);
        }

        this.status = OrderStatus.CANCELLED;
        this.cancelledTime = Instant.now();
        this.cancelReason = reason;
        this.markAsUpdated();

        // 发布订单取消事件
        addDomainEvent(new OrderCancelledEvent(this.orderId, this.customerId, reason));
    }

    /**
     * 修改收货地址
     * @param newAddress 新地址
     */
    public void changeShippingAddress(String newAddress) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Shipping address can only be changed in CREATED status");
        }

        this.shippingAddress = newAddress;
        this.markAsUpdated();
    }

    /**
     * 获取订单项列表（防御性拷贝）
     * @return 订单项列表的不可修改视图
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * 获取订单项数量
     * @return 订单项数量
     */
    public int getItemCount() {
        return items.size();
    }

}
