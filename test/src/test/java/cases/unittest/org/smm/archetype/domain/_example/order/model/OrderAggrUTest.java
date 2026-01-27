package cases.unittest.org.smm.archetype.domain._example.order.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.example.model.OrderAggr;
import org.smm.archetype.domain.example.model.OrderItem;
import org.smm.archetype.domain.example.model.OrderStatus;
import org.smm.archetype.domain.example.model.PaymentMethod;
import org.smm.archetype.domain.example.model.valueobject.Address;
import org.smm.archetype.domain.example.model.valueobject.ContactInfo;
import org.smm.archetype.domain.example.model.valueobject.Money;
import support.UnitTestBase;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OrderAggr聚合根单元测试 - 简化版本
 */
@DisplayName("OrderAggr聚合根单元测试")
class OrderAggrUTest extends UnitTestBase {

    // ==================== 工厂方法测试 ====================

    @Test
    @DisplayName("创建订单 - 正常参数 - 返回订单聚合根")
    void create_ValidParams_ReturnsOrderAggr() {
        // Arrange
        String orderNo = OrderAggr.generateOrderNo();
        String customerId = "CUST001";
        String customerName = "张三";
        ArrayList<OrderItem> items = createTestOrderItems();
        Money totalAmount = Money.of(new BigDecimal("100.00"));
        Address address = createTestAddress();
        ContactInfo contactInfo = createTestContactInfo();
        String remark = "测试订单";

        // Act
        OrderAggr order = OrderAggr.create(
                orderNo, customerId, customerName,
                items, totalAmount, address, contactInfo, remark
        );

        // Assert
        assertThat(order).isNotNull();
        assertThat(order.getOrderNo()).isEqualTo(orderNo);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getCustomerName()).isEqualTo(customerName);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(order.getRemark()).isEqualTo(remark);
        assertThat(order.getItemCount()).isEqualTo(1);
        // ID会在持久化后生成，单元测试中不验证ID
    }

    @Test
    @DisplayName("生成订单编号 - 返回唯一编号")
    void generateOrderNo_ReturnsUniqueOrderNo() {
        // Act
        String orderNo1 = OrderAggr.generateOrderNo();
        String orderNo2 = OrderAggr.generateOrderNo();

        // Assert
        assertThat(orderNo1).isNotNull();
        assertThat(orderNo2).isNotNull();
        assertThat(orderNo1).isNotEqualTo(orderNo2);
        assertThat(orderNo1).startsWith("ORD");
    }

    // ==================== 支付订单测试 ====================

    @Test
    @DisplayName("支付订单 - CREATED状态 - 转换为PAID")
    void pay_CreatedStatus_TransitionsToPaid() {
        // Arrange
        OrderAggr order = createTestOrder();

        // Act
        order.pay(PaymentMethod.ALIPAY, order.getTotalAmount());

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.ALIPAY);
        assertThat(order.getPaymentTime()).isNotNull();
        assertThat(order.isPaid()).isTrue();
    }

    @Test
    @DisplayName("支付订单 - PAID状态 - 抛出异常")
    void pay_PaidStatus_ThrowsException() {
        // Arrange
        OrderAggr order = createTestOrder();
        order.pay(PaymentMethod.ALIPAY, order.getTotalAmount());

        // Act & Assert
        assertThatThrownBy(() -> order.pay(PaymentMethod.WECHAT, order.getTotalAmount()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("订单状态不允许支付");
    }

    @Test
    @DisplayName("支付订单 - 金额不匹配 - 抛出异常")
    void pay_AmountMismatch_ThrowsException() {
        // Arrange
        OrderAggr order = createTestOrder();
        Money wrongAmount = Money.of(new BigDecimal("1.00"));

        // Act & Assert
        assertThatThrownBy(() -> order.pay(PaymentMethod.ALIPAY, wrongAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("支付金额与订单金额不匹配");
    }

    // ==================== 发货订单测试 ====================

    @Test
    @DisplayName("发货订单 - PAID状态 - 转换为SHIPPED")
    void ship_PaidStatus_TransitionsToShipped() {
        // Arrange
        OrderAggr order = createTestOrder();
        order.pay(PaymentMethod.ALIPAY, order.getTotalAmount());

        // Act
        order.ship();

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.getShippedTime()).isNotNull();
    }

    @Test
    @DisplayName("发货订单 - CREATED状态 - 抛出异常")
    void ship_CreatedStatus_ThrowsException() {
        // Arrange
        OrderAggr order = createTestOrder();

        // Act & Assert
        assertThatThrownBy(() -> order.ship())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("订单状态不允许发货");
    }

    // ==================== 完成订单测试 ====================

    @Test
    @DisplayName("完成订单 - SHIPPED状态 - 转换为COMPLETED")
    void complete_ShippedStatus_TransitionsToCompleted() {
        // Arrange
        OrderAggr order = createTestOrder();
        order.pay(PaymentMethod.ALIPAY, order.getTotalAmount());
        order.ship();

        // Act
        order.complete();

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getCompletedTime()).isNotNull();
        assertThat(order.isCompleted()).isTrue();
    }

    // ==================== 取消订单测试 ====================

    @Test
    @DisplayName("取消订单 - CREATED状态 - 转换为CANCELLED")
    void cancel_CreatedStatus_TransitionsToCancelled() {
        // Arrange
        OrderAggr order = createTestOrder();
        String reason = "用户主动取消";

        // Act
        order.cancel(reason);

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelReason()).isEqualTo(reason);
        assertThat(order.getCancelledTime()).isNotNull();
        assertThat(order.isCancelled()).isTrue();
    }

    @Test
    @DisplayName("取消订单 - SHIPPED状态 - 抛出异常")
    void cancel_ShippedStatus_ThrowsException() {
        // Arrange
        OrderAggr order = createTestOrder();
        order.pay(PaymentMethod.ALIPAY, order.getTotalAmount());
        order.ship();

        // Act & Assert
        assertThatThrownBy(() -> order.cancel("不想买了"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("订单状态不允许取消");
    }

    // ==================== 订单项操作测试 ====================

    @Test
    @DisplayName("添加订单项 - 正常参数 - 订单项增加")
    void addItem_ValidItem_IncreasesItemCount() {
        // Arrange
        OrderAggr order = createTestOrder();
        OrderItem newItem = OrderItem.builder()
                                    .setProductId("PROD002")
                                    .setProductName("MacBook Pro")
                                    .setSkuCode("MBP-14-GRAY")
                                    .setUnitPrice(Money.of(new BigDecimal("12999.00")))
                                    .setQuantity(1)
                                    .build();

        // Act
        order.addItem(newItem);

        // Assert
        assertThat(order.getItemCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("添加订单项 - null - 抛出异常")
    void addItem_Null_ThrowsException() {
        // Arrange
        OrderAggr order = createTestOrder();

        // Act & Assert
        assertThatThrownBy(() -> order.addItem(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("订单项不能为空");
    }

    @Test
    @DisplayName("移除订单项 - 正常索引 - 订单项减少")
    void removeItem_ValidIndex_DecreasesItemCount() {
        // Arrange
        OrderAggr order = createTestOrder();
        int originalCount = order.getItemCount();

        // Act
        order.removeItem(0);

        // Assert
        assertThat(order.getItemCount()).isEqualTo(originalCount - 1);
    }

    @Test
    @DisplayName("移除订单项 - 负索引 - 抛出异常")
    void removeItem_NegativeIndex_ThrowsException() {
        // Arrange
        OrderAggr order = createTestOrder();

        // Act & Assert
        assertThatThrownBy(() -> order.removeItem(-1))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageContaining("订单项索引越界");
    }

    // ==================== 属性修改测试 ====================

    @Test
    @DisplayName("更新收货地址 - CREATED状态 - 地址更新成功")
    void updateShippingAddress_CreatedStatus_UpdatesAddress() {
        // Arrange
        OrderAggr order = createTestOrder();
        Address newAddress = Address.builder()
                                     .province("上海市")
                                     .city("上海市")
                                     .district("浦东新区")
                                     .detailAddress("陆家嘴金融中心")
                                     .postalCode("200120")
                                     .build();

        // Act
        order.updateShippingAddress(newAddress);

        // Assert
        assertThat(order.getShippingAddress()).isEqualTo(newAddress);
    }

    @Test
    @DisplayName("更新收货地址 - PAID状态 - 抛出异常")
    void updateShippingAddress_PaidStatus_ThrowsException() {
        // Arrange
        OrderAggr order = createTestOrder();
        order.pay(PaymentMethod.ALIPAY, order.getTotalAmount());
        Address newAddress = createTestAddress();

        // Act & Assert
        assertThatThrownBy(() -> order.updateShippingAddress(newAddress))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("订单已发货不能修改地址");
    }

    // ==================== 辅助方法 ====================

    private OrderAggr createTestOrder() {
        String orderNo = OrderAggr.generateOrderNo();
        return OrderAggr.create(
                orderNo,
                "CUST001",
                "张三",
                createTestOrderItems(),
                Money.of(new BigDecimal("100.00")),
                createTestAddress(),
                createTestContactInfo(),
                "测试订单"
        );
    }

    private ArrayList<OrderItem> createTestOrderItems() {
        ArrayList<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                          .setProductId("PROD001")
                          .setProductName("iPhone 15")
                          .setSkuCode("IPHONE15-BLK-128G")
                          .setUnitPrice(Money.of(new BigDecimal("100.00")))
                          .setQuantity(1)
                          .build());
        return items;
    }

    private Address createTestAddress() {
        return Address.builder()
                       .province("北京市")
                       .city("北京市")
                       .district("朝阳区")
                       .detailAddress("望京SOHO T1 1001室")
                       .postalCode("100102")
                       .build();
    }

    private ContactInfo createTestContactInfo() {
        return ContactInfo.builder()
                       .contactName("张三")
                       .contactPhone("13800138000")
                       .contactEmail("zhangsan@example.com")
                       .build();
    }

}
