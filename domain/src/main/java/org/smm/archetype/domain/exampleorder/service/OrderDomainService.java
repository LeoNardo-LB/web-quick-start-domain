package org.smm.archetype.domain.exampleorder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.exampleorder.OrderErrorCode;
import org.smm.archetype.domain.exampleorder.model.OrderItem;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.exampleorder.model.valueobject.OrderItemInfo;
import org.smm.archetype.domain.shared.exception.BizException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单领域服务，封装跨聚合根的业务规则。
 */
@Slf4j
@RequiredArgsConstructor
public class OrderDomainService {

    private final InventoryService inventoryService;

    /**
     * 验证订单项
     * @param items 订单项列表
     * @throws BizException 订单项无效
     */
    public void validateOrderItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BizException(OrderErrorCode.ORDER_ITEMS_EMPTY);
        }

        for (OrderItem item : items) {
            validateOrderItem(item);
        }

        log.debug("订单项验证通过，共{}项", items.size());
    }

    /**
     * 验证单个订单项
     * @param item 订单项
     * @throws BizException 订单项无效
     */
    private void validateOrderItem(OrderItem item) {
        if (item == null) {
            throw new BizException(OrderErrorCode.ORDER_ITEM_INVALID);
        }

        if (item.getProductId() == null || item.getProductId().isBlank()) {
            throw new BizException(OrderErrorCode.PRODUCT_ID_EMPTY);
        }

        if (item.getProductName() == null || item.getProductName().isBlank()) {
            throw new BizException(OrderErrorCode.PRODUCT_NAME_EMPTY);
        }

        if (item.getSkuCode() == null || item.getSkuCode().isBlank()) {
            throw new BizException(OrderErrorCode.SKU_CODE_EMPTY);
        }

        if (item.getUnitPrice() == null || item.getUnitPrice().isNegative()) {
            throw new BizException(OrderErrorCode.UNIT_PRICE_INVALID);
        }

        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new BizException(OrderErrorCode.QUANTITY_INVALID);
        }

        // 验证小计金额计算是否正确
        Money calculatedSubtotal = item.getUnitPrice().multiply(item.getQuantity());
        if (!calculatedSubtotal.equals(item.getSubtotal())) {
            throw new BizException(
                    String.format("订单项小计金额计算错误: %s != %s",
                            calculatedSubtotal, item.getSubtotal()),
                    OrderErrorCode.ORDER_ITEM_INVALID
            );
        }
    }

    /**
     * 计算订单总金额
     * @param items 订单项列表
     * @return 总金额
     */
    public Money calculateTotalAmount(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return Money.zero();
        }

        Money total = items.stream()
                              .map(OrderItem::calculateSubtotal)
                              .reduce(Money.zero(), Money::add);

        log.debug("计算订单总金额: {}", total);
        return total;
    }

    /**
     * 从订单项信息列表创建订单项列表
     * @param orderItemInfos 订单项信息列表
     * @return 订单项列表
     */
    public List<OrderItem> createOrderItems(List<OrderItemInfo> orderItemInfos) {
        if (orderItemInfos == null || orderItemInfos.isEmpty()) {
            throw new BizException(OrderErrorCode.ORDER_ITEMS_EMPTY);
        }

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemInfo info : orderItemInfos) {
            OrderItem item = OrderItem.builder()
                                     .setProductId(info.getProductId())
                                     .setProductName(info.getProductName())
                                     .setSkuCode(info.getSkuCode())
                                     .setUnitPrice(info.getUnitPrice())
                                     .setQuantity(info.getQuantity())
                                     .setCurrency(info.getUnitPrice().getCurrency())
                                     .setSubtotal(info.calculateSubtotal())
                                     .build();
            items.add(item);
        }
        return items;
    }

    /**
     * 验证库存是否充足
     * @param orderItemInfos 订单项信息列表
     * @throws InventoryService.InsufficientInventoryException 库存不足
     */
    public void validateInventory(List<OrderItemInfo> orderItemInfos)
            throws InventoryService.InsufficientInventoryException {

        List<InventoryService.InventoryItem> inventoryItems = orderItemInfos.stream()
                                                                       .map(info -> new InventoryService.InventoryItem(
                                                                               info.getProductId(),
                                                                               info.getSkuCode(),
                                                                               info.getQuantity()
                                                                       ))
                                                                       .toList();

        if (!inventoryService.validateInventory(inventoryItems)) {
            throw new InventoryService.InsufficientInventoryException("库存不足，无法创建订单");
        }

        log.info("库存验证通过，共{}种商品", inventoryItems.size());
    }

    /**
     * 锁定库存
     * @param orderId        订单ID
     * @param orderNo        订单编号
     * @param orderItemInfos 订单项信息列表
     * @throws InventoryService.InsufficientInventoryException 库存不足
     */
    public void lockInventory(
            Long orderId,
            String orderNo,
            List<OrderItemInfo> orderItemInfos)
            throws InventoryService.InsufficientInventoryException {

        List<InventoryService.InventoryItem> inventoryItems = orderItemInfos.stream()
                                                                      .map(info -> new InventoryService.InventoryItem(
                                                                              info.getProductId(),
                                                                              info.getSkuCode(),
                                                                              info.getQuantity()
                                                                      ))
                                                                      .toList();

        inventoryService.lockInventory(orderId, orderNo, inventoryItems);
        log.info("库存锁定成功: orderNo={}, 商品数量={}", orderNo, inventoryItems.size());
    }

    /**
     * 释放库存
     * @param orderId 订单ID
     * @param orderNo 订单编号
     */
    public void releaseInventory(Long orderId, String orderNo) {
        inventoryService.releaseInventory(orderId, orderNo);
        log.info("库存释放成功: orderNo={}", orderNo);
    }

    /**
     * 验证支付金额
     * @param orderAmount   订单金额
     * @param paymentAmount 支付金额
     * @throws BizException 金额不匹配
     */
    public void validatePaymentAmount(Money orderAmount, Money paymentAmount) {
        if (!orderAmount.equals(paymentAmount)) {
            throw new BizException(
                    String.format("支付金额与订单金额不匹配: 订单金额=%s, 支付金额=%s",
                            orderAmount, paymentAmount),
                    OrderErrorCode.MONEY_INVALID
            );
        }

        log.info("支付金额验证通过: {}", paymentAmount);
    }

    /**
     * 检查订单项数量限制
     * @param itemCount 订单项数量
     * @param maxLimit  最大限制
     * @return 超出限制返回true
     */
    public boolean exceedsItemLimit(int itemCount, int maxLimit) {
        return itemCount > maxLimit;
    }

    /**
     * 计算折扣金额
     * @param originalAmount 原始金额
     * @param discountRate   折扣率（0-100）
     * @return 折扣金额
     */
    public Money calculateDiscountAmount(Money originalAmount, BigDecimal discountRate) {
        if (discountRate.compareTo(BigDecimal.ZERO) < 0
                    || discountRate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BizException(OrderErrorCode.MONEY_INVALID);
        }

        BigDecimal discount = originalAmount.getAmount()
                                      .multiply(discountRate)
                                      .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

        return Money.of(discount, originalAmount.getCurrency());
    }

}
