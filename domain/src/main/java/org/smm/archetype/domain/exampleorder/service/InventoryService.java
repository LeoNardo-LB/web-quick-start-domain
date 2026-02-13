package org.smm.archetype.domain.exampleorder.service;

import org.smm.archetype.domain.exampleorder.model.event.OrderCreatedEventDTO;

import java.util.List;

/**
 * 库存服务端口接口，定义库存操作的抽象。
 */
public interface InventoryService {

    /**
     * 锁定库存
     * @param orderId        订单ID
     * @param orderNo        订单编号
     * @param inventoryItems 库存项列表（商品ID、SKU、数量）
     * @throws InsufficientInventoryException 库存不足
     */
    void lockInventory(
            Long orderId,
            String orderNo,
            List<InventoryItem> inventoryItems
    ) throws InsufficientInventoryException;

    /**
     * 释放库存
     * @param orderId 订单ID
     * @param orderNo 订单编号
     */
    void releaseInventory(Long orderId, String orderNo);

    /**
     * 验证库存是否充足
     * @param inventoryItems 库存项列表
     * @return 库存充足返回true
     */
    boolean validateInventory(List<InventoryItem> inventoryItems);

    /**
     * 从订单创建事件提取库存项
     * @param event 订单创建事件
     * @return 库存项列表
     */
    default List<InventoryItem> extractInventoryItems(OrderCreatedEventDTO event) {
        return event.getOrderItems().stream()
                       .map(item -> new InventoryItem(
                               item.productId(),
                               item.skuCode(),
                               item.quantity()
                       ))
                       .toList();
    }

    /**
     * 库存不足异常
     */
    class InsufficientInventoryException extends Exception {

        public InsufficientInventoryException(String message) {
            super(message);
        }

    }

    /**
     * 库存项
     */
    record InventoryItem(
            /**
             * 商品ID
             */
            String productId,
            /**
             * SKU编码
             */
            String skuCode,
            /**
             * 数量
             */
            Integer quantity
    ) {

        public InventoryItem {
            if (quantity <= 0) {
                throw new IllegalArgumentException("数量必须大于0");
            }
        }

    }

}
