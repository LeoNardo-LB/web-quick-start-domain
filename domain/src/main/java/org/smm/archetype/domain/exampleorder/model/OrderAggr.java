package org.smm.archetype.domain.exampleorder.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.exampleorder.OrderErrorCode;
import org.smm.archetype.domain.exampleorder.model.event.OrderCancelledEventDTO;
import org.smm.archetype.domain.exampleorder.model.event.OrderCreatedEventDTO;
import org.smm.archetype.domain.exampleorder.model.event.OrderCreatedEventDTO.OrderCreatedEventDTOBuilder;
import org.smm.archetype.domain.exampleorder.model.event.OrderPaidEventDTO;
import org.smm.archetype.domain.exampleorder.model.event.OrderRefundEventDTO;
import org.smm.archetype.domain.exampleorder.model.event.OrderShippedEventDTO;
import org.smm.archetype.domain.exampleorder.model.valueobject.Address;
import org.smm.archetype.domain.exampleorder.model.valueobject.ContactInfo;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.shared.base.AggregateRoot;
import org.smm.archetype.domain.shared.exception.BizException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * 订单聚合根，管理订单生命周期和状态转换。
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

    // ==================== 退款相关字段 ====================
    private Money                refundedAmount;
    private Money                totalRefundedAmount;
    private String               refundReason;
    private Instant              refundedTime;
    private RefundType           refundType;

    // ==================== 排序相关字段 ====================
    private Integer sortOrder;

    // ==================== 工厂方法 ====================

    /**
     * 创建订单（使用默认排序值 0）
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
        return create(orderNo, customerId, customerName, items, totalAmount,
                shippingAddress, contactInfo, remark, 0);
    }

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
     * @param sortOrder       排序顺序（默认为 0）
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
            String remark,
            Integer sortOrder
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
        builder.sortOrder = sortOrder != null ? sortOrder : 0;
        builder.status = OrderStatus.CREATED;
        OrderAggr orderAggr = builder.build();

        orderAggr.markAsCreated();
        orderAggr.publishCreatedEvent();

        log.info("订单创建成功: orderNo={}, customerId={}, totalAmount={}, sortOrder={}",
                orderNo, customerId, totalAmount, builder.sortOrder);
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
     * @throws BizException 订单状态不允许支付或支付金额不匹配
     */
    public void pay(PaymentMethod paymentMethod, Money paidAmount) {
        // 验证状态
        if (!status.canPay()) {
            throw new BizException(
                    String.format("订单状态不允许支付: 当前状态=%s", status),
                    OrderErrorCode.ORDER_STATUS_INVALID
            );
        }

        // 验证金额
        if (!totalAmount.equals(paidAmount)) {
            throw new BizException(
                    String.format("支付金额与订单金额不匹配: 订单金额=%s, 支付金额=%s",
                            totalAmount, paidAmount),
                    OrderErrorCode.MONEY_INVALID
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
     * @throws BizException 订单状态不允许发货
     */
    public void ship() {
        // 验证状态
        if (!status.canShip()) {
            throw new BizException(
                    String.format("订单状态不允许发货: 当前状态=%s", status),
                    OrderErrorCode.ORDER_STATUS_INVALID
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
     * @throws BizException 订单状态不允许完成
     */
    public void complete() {
        // 验证状态
        if (!status.canComplete()) {
            throw new BizException(
                    String.format("订单状态不允许完成: 当前状态=%s", status),
                    OrderErrorCode.ORDER_STATUS_INVALID
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
     * @throws BizException 订单状态不允许取消
     */
    public void cancel(String reason) {
        // 验证状态
        if (!status.canCancel()) {
            throw new BizException(
                    String.format("订单状态不允许取消: 当前状态=%s", status),
                    OrderErrorCode.ORDER_STATUS_INVALID
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
     * 退款订单
     * @param refundAmount 退款金额
     * @param refundType   退款类型（全额/部分）
     * @param refundReason 退款原因
     * @throws BizException 订单状态不允许退款或退款金额超限
     */
    public void refund(Money refundAmount, RefundType refundType, String refundReason) {
        // 验证状态
        if (!status.canRefund()) {
            throw new BizException(
                    String.format("订单状态不允许退款: 当前状态=%s", status),
                    OrderErrorCode.ORDER_STATUS_INVALID
            );
        }

        // 计算剩余可退金额
        Money alreadyRefunded = this.totalRefundedAmount != null
                                        ? this.totalRefundedAmount
                                        : Money.of(java.math.BigDecimal.ZERO, currency);
        Money remainingRefundable = totalAmount.subtract(alreadyRefunded);

        // 验证退款金额
        if (refundAmount == null || refundAmount.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BizException("退款金额无效", OrderErrorCode.REFUND_AMOUNT_INVALID);
        }

        if (refundAmount.getAmount().compareTo(remainingRefundable.getAmount()) > 0) {
            throw new BizException(
                    String.format("退款金额超过剩余可退金额: 申请退款=%s, 剩余可退=%s", refundAmount, remainingRefundable),
                    OrderErrorCode.REFUND_AMOUNT_EXCEEDED
            );
        }

        // 更新退款金额
        Money newTotalRefunded = alreadyRefunded.add(refundAmount);
        this.totalRefundedAmount = newTotalRefunded;
        this.refundedAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundedTime = Instant.now();
        this.refundType = refundType;

        // 判断是全额退款还是部分退款
        if (newTotalRefunded.equals(totalAmount)) {
            this.status = OrderStatus.REFUNDED;
        } else {
            this.status = OrderStatus.PARTIALLY_REFUNDED;
        }

        // 发布事件
        publishRefundEvent();
        markAsUpdated();

        log.info("订单退款: orderNo={}, refundAmount={}, refundType={}, totalRefunded={}",
                orderNo, refundAmount, refundType, newTotalRefunded);
    }

    /**
     * 添加订单项
     * @param item 订单项
     */
    public void addItem(OrderItem item) {
        if (item == null) {
            throw new BizException(OrderErrorCode.ORDER_ITEM_INVALID);
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
     * @throws BizException 订单已发货不能修改地址
     */
    public void updateShippingAddress(Address address) {
        if (status != OrderStatus.CREATED) {
            throw new BizException(OrderErrorCode.ORDER_STATUS_INVALID);
        }
        this.shippingAddress = address;
        markAsUpdated();
    }

    /**
     * 更新联系信息
     * @param contactInfo 新联系信息
     * @throws BizException 订单已发货不能修改联系信息
     */
    public void updateContactInfo(ContactInfo contactInfo) {
        if (status != OrderStatus.CREATED) {
            throw new BizException(OrderErrorCode.ORDER_STATUS_INVALID);
        }
        this.contactInfo = contactInfo;
        markAsUpdated();
    }

    /**
     * 更新排序顺序
     * @param sortOrder 新的排序顺序
     * @throws BizException 订单状态不允许修改排序顺序
     */
    public void updateSortOrder(Integer sortOrder) {
        if (status != OrderStatus.CREATED) {
            throw new BizException(
                    String.format("订单状态不允许修改排序顺序: 当前状态=%s", status),
                    OrderErrorCode.ORDER_STATUS_INVALID
            );
        }
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        markAsUpdated();
        log.info("订单排序顺序更新: orderNo={}, sortOrder={}", orderNo, this.sortOrder);
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
        recordEvent(createdEventDTO);
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
        recordEvent(builder.build());
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
        recordEvent(builder.build());
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
        recordEvent(builder.build());
    }

    /**
     * 发布订单退款事件
     */
    private void publishRefundEvent() {
        OrderRefundEventDTO.OrderRefundEventDTOBuilder<?, ?> builder = OrderRefundEventDTO.OREBuilder();
        builder.setOrderId(this.id);
        builder.setOrderNo(this.orderNo);
        builder.setCustomerId(this.customerId);
        builder.setRefundAmount(this.refundedAmount);
        builder.setRefundType(this.refundType);
        builder.setRefundReason(this.refundReason);
        builder.setRefundedTime(this.refundedTime);
        builder.setTotalRefundedAmount(this.totalRefundedAmount);
        recordEvent(builder.build());
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
     * 检查订单是否已退款
     * @return 已退款返回true
     */
    public boolean isRefunded() {
        return status == OrderStatus.REFUNDED;
    }

    /**
     * 检查订单是否部分退款
     * @return 部分退款返回true
     */
    public boolean isPartiallyRefunded() {
        return status == OrderStatus.PARTIALLY_REFUNDED;
    }

    /**
     * 检查订单是否可以支付
     * @return 可以支付返回true
     */
    public boolean canPay() {
        return status.canPay();
    }

    /**
     * 获取订单项数量
     * @return 订单项数量
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * 检查订单是否可以流转到目标状态
     * <p>
     * 状态流转规则：
     * <ul>
     *   <li>CREATED → PAID, CANCELLED</li>
     *   <li>PAID → SHIPPED, CANCELLED, REFUNDED, PARTIALLY_REFUNDED</li>
     *   <li>SHIPPED → COMPLETED</li>
     *   <li>PARTIALLY_REFUNDED → REFUNDED, PARTIALLY_REFUNDED</li>
     *   <li>COMPLETED, CANCELLED, REFUNDED → 终态，不可流转</li>
     * </ul>
     * </p>
     *
     * @param targetStatus 目标状态
     * @return 可以流转返回 true，否则返回 false
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }

        // 终态检查
        if (this.status.isTerminalState()) {
            return false;
        }

        // 根据当前状态判断可以流转的目标状态
        return switch (this.status) {
            case CREATED -> targetStatus == OrderStatus.PAID || targetStatus == OrderStatus.CANCELLED;
            case PAID -> targetStatus == OrderStatus.SHIPPED 
                    || targetStatus == OrderStatus.CANCELLED 
                    || targetStatus == OrderStatus.REFUNDED 
                    || targetStatus == OrderStatus.PARTIALLY_REFUNDED;
            case SHIPPED -> targetStatus == OrderStatus.COMPLETED;
            // PARTIALLY_REFUNDED 允许继续部分退款（状态保持）或全额退款
            case PARTIALLY_REFUNDED -> targetStatus == OrderStatus.REFUNDED 
                    || targetStatus == OrderStatus.PARTIALLY_REFUNDED;
            default -> false;
        };
    }

}
