package org.smm.archetype.infrastructure.exampleorder.adapter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.exampleorder.service.InventoryService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟库存服务适配器
 *
职责：
 * <ul>
 *   <li>实现InventoryService端口接口</li>
 *   <li>提供Mock的库存验证和锁定功能</li>
 *   <li>用于演示和测试</li>
 * </ul>
 *
说明：
 * <ul>
 *   <li>这是一个Mock实现，仅用于演示</li>
 *   <li>实际生产环境需要调用真实的库存服务</li>
 *   <li>使用内存Map存储库存数据</li>
 * </ul>


 */
@Getter
@Slf4j
// 注意：不要使用@Component，通过OrderInfraConfigure配置类注册为Bean
public class MockInventoryServiceAdapter implements InventoryService {

    /**
     * 模拟库存数据（商品ID + SKU → 库存数量）
     *
     * 使用静态初始化块，确保无论如何都会初始化
     */
    private static final Map<String, Integer> DEFAULT_INVENTORY;

    static {
        Map<String, Integer> map = new ConcurrentHashMap<>();
        // Keys are constructed as: productId + "_" + skuCode
        map.put("PROD001_IPHONE15-BLK-128G", 1000);  // iPhone 15
        map.put("PROD002_MBP14-SPACE-16G", 500);     // MacBook Pro 14
        map.put("PROD003_APPRO-WHITE-2G", 2000);     // AirPods Pro
        map.put("PRODUCT_001_SKU_001", 100);
        map.put("PRODUCT_002_SKU_001", 50);
        map.put("PRODUCT_003_SKU_001", 200);
        DEFAULT_INVENTORY = Collections.unmodifiableMap(map);
        log.info("MockInventoryServiceAdapter static init: DEFAULT_INVENTORY size = {}", DEFAULT_INVENTORY.size());
    }

    /**
     * -- GETTER --
     *  获取库存数据（用于测试和调试）
     */
    private final Map<String, Integer> inventoryData;

    /**
     * 构造函数，初始化库存数据
     */
    public MockInventoryServiceAdapter() {
        log.info("MockInventoryServiceAdapter Constructor called");
        // Initialize inventory data from static defaults
        this.inventoryData = new ConcurrentHashMap<>(DEFAULT_INVENTORY);
        log.info("MockInventoryServiceAdapter initialized with {} inventory items", inventoryData.size());
    }

    @Override
    public void lockInventory(
            Long orderId,
            String orderNo,
            List<InventoryItem> inventoryItems
    ) throws InsufficientInventoryException {
        log.info("锁定库存开始: orderId={}, orderNo={}, itemCount={}",
                orderId, orderNo, inventoryItems.size());

        for (InventoryItem item : inventoryItems) {
            String key = item.productId() + "_" + item.skuCode();
            Integer availableStock = inventoryData.getOrDefault(key, 0);

            if (availableStock < item.quantity()) {
                log.error("库存不足: productId={}, skuCode={}, available={}, required={}",
                        item.productId(), item.skuCode(), availableStock, item.quantity());
                throw new InsufficientInventoryException(
                        String.format("库存不足: 商品=%s, SKU=%s, 可用=%d, 需要=%d",
                                item.productId(), item.skuCode(), availableStock, item.quantity())
                );
            }

            // 扣减库存
            inventoryData.put(key, availableStock - item.quantity());
            log.info("库存锁定成功: productId={}, skuCode={}, lockedQuantity={}, remaining={}",
                    item.productId(), item.skuCode(), item.quantity(), availableStock - item.quantity());
        }

        log.info("库存锁定完成: orderNo={}", orderNo);
    }

    @Override
    public void releaseInventory(Long orderId, String orderNo) {
        log.info("释放库存开始: orderId={}, orderNo={}", orderId, orderNo);

        // TODO: 实际实现需要根据订单ID查询之前锁定的库存并释放
        // 这里仅打印日志
        log.info("库存释放完成: orderNo={}", orderNo);
    }

    @Override
    public boolean validateInventory(List<InventoryItem> inventoryItems) {
        log.info("验证库存开始: itemCount={}", inventoryItems.size());

        for (InventoryItem item : inventoryItems) {
            String key = item.productId() + "_" + item.skuCode();
            Integer availableStock = inventoryData.getOrDefault(key, 0);

            log.info("检查库存: key={}, productId={}, skuCode={}, available={}, required={}",
                    key, item.productId(), item.skuCode(), availableStock, item.quantity());

            if (availableStock < item.quantity()) {
                log.warn("库存验证失败: productId={}, skuCode={}, available={}, required={}",
                        item.productId(), item.skuCode(), availableStock, item.quantity());
                return false;
            }
        }

        log.info("库存验证通过: itemCount={}", inventoryItems.size());
        return true;
    }

}
