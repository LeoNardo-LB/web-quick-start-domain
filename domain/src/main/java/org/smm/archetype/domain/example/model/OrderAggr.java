package org.smm.archetype.domain.example.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.base.AggregateRoot;
import org.smm.archetype.domain.example.model.event.OrderCancelledEventDTO;
import org.smm.archetype.domain.example.model.event.OrderCreatedEventDTO;
import org.smm.archetype.domain.example.model.event.OrderCreatedEventDTO.OrderCreatedEventDTOBuilder;
import org.smm.archetype.domain.example.model.event.OrderPaidEventDTO;
import org.smm.archetype.domain.example.model.event.OrderShippedEventDTO;
import org.smm.archetype.domain.example.model.valueobject.Address;
import org.smm.archetype.domain.example.model.valueobject.ContactInfo;
import org.smm.archetype.domain.example.model.valueobject.Money;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * 订单聚合根
 *
 * <p>职责：
 * <ul>
 *   <li>管理订单的生命周期（创建、支付、发货、取消、完成）</li>
 *   <li>维护订单状态转换的业务规则</li>
 *   <li>发布领域事件</li>
 *   <li>维护订单项、地址、联系信息等聚合边界</li>
 * </ul>
 *
 * <p>状态转换：
 * <pre>
 * CREATED → PAID → SHIPPED → COMPLETED
 *     ↓
 * CANCELLED
 * </pre>
 * @author Leonardo
 * @since 2026/1/11
 */
@Slf4j
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "OABuilder")
public class OrderAggr extends AggregateRoot {

    private String               orderNo;
    private String               customerId;
    private String               customerName;
    private OrderStatus          status;
    private Money                totalAmount;
    private String               currency;
    private PaymentMethod        paymentMethod;
    private Instant              paymentTime;
    private String               remark;
    private ArrayList<OrderItem> items;
    private Address              shippingAddress;
    private ContactInfo          contactInfo;
    private Instant              shippedTime;
    private Instant              completedTime;
    private Instant              cancelledTime;
    private String               cancelReason;

    // ==================== 工厂方法 ====================

    /**
     * 创建订单
     * @param orderNo         订单编号
     * @param customerId      客户ID
     * @param customerName    客户名称
     * @param items           订单项列表
     * @param totalAmount     总金额
     * @param shippingAddress 收货地址
     * @param contactInfo     联系信息
     * @param remark          备注
     * @return 订单聚合根
     */
    public static OrderAggr create(
            String orderNo,
            String customerId,
            String customerName,
            ArrayList<OrderItem> items,
            Money totalAmount,
            Address shippingAddress,
            ContactInfo contactInfo,
            String remark
    ) {
        OrderAggrBuilder<?, ?> builder = OrderAggr.OABuilder();
        builder.orderNo = orderNo;
        builder.customerId = customerId;
        builder.customerName = customerName;
        builder.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        builder.totalAmount = totalAmount;
        builder.currency = totalAmount.getCurrency();
        builder.shippingAddress = shippingAddress;
        builder.contactInfo = contactInfo;
        builder.remark = remark;
        builder.status = OrderStatus.CREATED;
        OrderAggr orderAggr = builder.build();

        orderAggr.markAsCreated();
        orderAggr.publishCreatedEvent();

        log.info("订单创建成功: orderNo={}, customerId={}, totalAmount={}", orderNo, customerId, totalAmount);
        return orderAggr;
    }

    /**
     * 生成订单编号
     * @return 订单编号
     */
    public static String generateOrderNo() {
        return "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    // ==================== 业务方法 ====================

    /**
     * 支付订单
     * @param paymentMethod 支付方式
     * @param paidAmount    支付金额
     * @throws IllegalStateException 订单状态不允许支付
     * @throws IllegalArgumentException 支付金额不匹配
     */
    public void pay(PaymentMethod paymentMethod, Money paidAmount) {
        // 验证状态
        if (!status.canPay()) {
            throw new IllegalStateException(
                    String.format("订单状态不允许支付: 当前状态=%s", status)
            );
        }

        // 验证金额
        if (!totalAmount.equals(paidAmount)) {
            throw new IllegalArgumentException(
                    String.format("支付金额与订单金额不匹配: 订单金额=%s, 支付金额=%s",
                            totalAmount, paidAmount)
            );
        }

        // 修改状态
        this.status = OrderStatus.PAID;
        this.paymentMethod = paymentMethod;
        this.paymentTime = Instant.now();

        // 发布事件
        publishPaidEvent();
        markAsUpdated();

        log.info("订单支付成功: orderNo={}, paymentMethod={}, paidAmount={}",
                orderNo, paymentMethod, paidAmount);
    }

    /**
     * 发货订单
     * @throws IllegalStateException 订单状态不允许发货
     */
    public void ship() {
        // 验证状态
        if (!status.canShip()) {
            throw new IllegalStateException(
                    String.format("订单状态不允许发货: 当前状态=%s", status)
            );
        }

        // 修改状态
        this.status = OrderStatus.SHIPPED;
        this.shippedTime = Instant.now();

        // 发布事件
        publishShippedEvent();
        markAsUpdated();

        log.info("订单发货成功: orderNo={}", orderNo);
    }

    /**
     * 完成订单
     * @throws IllegalStateException 订单状态不允许完成
     */
    public void complete() {
        // 验证状态
        if (!status.canComplete()) {
            throw new IllegalStateException(
                    String.format("订单状态不允许完成: 当前状态=%s", status)
            );
        }

        // 修改状态
        this.status = OrderStatus.COMPLETED;
        this.completedTime = Instant.now();

        markAsUpdated();

        log.info("订单完成: orderNo={}", orderNo);
    }

    /**
     * 取消订单
     * @param reason 取消原因
     * @throws IllegalStateException 订单状态不允许取消
     */
    public void cancel(String reason) {
        // 验证状态
        if (!status.canCancel()) {
            throw new IllegalStateException(
                    String.format("订单状态不允许取消: 当前状态=%s", status)
            );
        }

        // 修改状态
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledTime = Instant.now();

        // 发布事件
        publishCancelledEvent();
        markAsUpdated();

        log.info("订单取消: orderNo={}, reason={}", orderNo, reason);
    }

    /**
     * 添加订单项
     * @param item 订单项
     */
    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("订单项不能为空");
        }
        this.items.add(item);
        markAsUpdated();
    }

    /**
     * 移除订单项
     * @param index 订单项索引
     */
    public void removeItem(int index) {
        if (index < 0 || index >= items.size()) {
            throw new IndexOutOfBoundsException("订单项索引越界: " + index);
        }
        this.items.remove(index);
        markAsUpdated();
    }

    /**
     * 更新收货地址
     * @param address 新地址
     * @throws IllegalStateException 订单已发货不能修改地址
     */
    public void updateShippingAddress(Address address) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("订单已发货不能修改地址");
        }
        this.shippingAddress = address;
        markAsUpdated();
    }

    /**
     * 更新联系信息
     * @param contactInfo 新联系信息
     * @throws IllegalStateException 订单已发货不能修改联系信息
     */
    public void updateContactInfo(ContactInfo contactInfo) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("订单已发货不能修改联系信息");
        }
        this.contactInfo = contactInfo;
        markAsUpdated();
    }

    // ==================== 领域事件发布 ====================

    /**
     * 发布订单创建事件
     */
    private void publishCreatedEvent() {
        // 转换 OrderItem 为 OrderItemInfo
        ArrayList<OrderCreatedEventDTO.OrderItemInfo> orderItemInfos = new ArrayList<>();
        for (OrderItem item : this.items) {
            orderItemInfos.add(new OrderCreatedEventDTO.OrderItemInfo(
                    item.getProductId(),
                    item.getSkuCode(),
                    item.getQuantity()
            ));
        }

        OrderCreatedEventDTOBuilder<?, ?> builder = OrderCreatedEventDTO.OCEBuilder();
        builder.setOrderId(this.id);
        builder.setOrderNo(this.orderNo);
        builder.setCustomerId(this.customerId);
        builder.setCustomerName(this.customerName);
        builder.setTotalAmount(this.totalAmount);
        builder.setShippingAddress(this.shippingAddress);
        builder.setContactInfo(this.contactInfo);
        builder.setOrderItems(orderItemInfos);
        OrderCreatedEventDTO createdEventDTO = builder.build();
        addEvent(createdEventDTO);
    }

    /**
     * 发布订单支付事件
     */
    private void publishPaidEvent() {
        OrderPaidEventDTO.OrderPaidEventDTOBuilder<?, ?> builder = OrderPaidEventDTO.OPEBuilder();
        builder.setOrderId(this.id);
        builder.setOrderNo(this.orderNo);
        builder.setCustomerId(this.customerId);
        builder.setPaymentAmount(this.totalAmount);
        builder.setPaymentMethod(this.paymentMethod);
        builder.setPaymentTime(this.paymentTime);
        addEvent(builder.build());
    }

    /**
     * 发布订单发货事件
     */
    private void publishShippedEvent() {
        OrderShippedEventDTO.OrderShippedEventDTOBuilder<?, ?> builder = OrderShippedEventDTO.OSEBuilder();
        builder.setOrderId(this.id);
        builder.setOrderNo(this.orderNo);
        builder.setCustomerId(this.customerId);
        builder.setShippedTime(this.shippedTime != null ? this.shippedTime.toString() : Instant.now().toString());
        addEvent(builder.build());
    }

    /**
     * 发布订单取消事件
     */
    private void publishCancelledEvent() {
        OrderCancelledEventDTO.OrderCancelledEventDTOBuilder<?, ?> builder = OrderCancelledEventDTO.OCEBuilder();
        builder.setOrderId(this.id);
        builder.setOrderNo(this.orderNo);
        builder.setCustomerId(this.customerId);
        builder.setReason(this.cancelReason);
        builder.setCancelledTime(this.cancelledTime);
        addEvent(builder.build());
    }

    // ==================== 查询方法 ====================

    /**
     * 检查订单是否已支付
     * @return 已支付返回true
     */
    public boolean isPaid() {
        return status == OrderStatus.PAID
                       || status == OrderStatus.SHIPPED
                       || status == OrderStatus.COMPLETED;
    }

    /**
     * 检查订单是否已取消
     * @return 已取消返回true
     */
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    /**
     * 检查订单是否已完成
     * @return 已完成返回true
     */
    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED;
    }

    /**
     * 获取订单项数量
     * @return 订单项数量
     */
    public int getItemCount() {
        return items.size();
    }

}
