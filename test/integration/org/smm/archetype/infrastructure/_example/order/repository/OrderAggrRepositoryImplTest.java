package org.smm.archetype.infrastructure._example.order.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain._example.order.model.aggregateroot.OrderAggr;
import org.smm.archetype.domain._example.order.model.enumeration.OrderStatus;
import org.smm.archetype.domain._example.order.model.valueobject.Money;
import org.smm.archetype.domain._example.order.model.valueobject.OrderId;
import org.smm.archetype.infrastructure._example.order.repository.mapper.OrderAggrMapper;
import org.smm.archetype.test.base.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 订单仓储集成测试示例
 *
 * <p>演示如何测试Repository层：
 * <ul>
 *   <li>继承IntegrationTestBase启动Spring + H2数据库</li>
 *   <li>测试真实Mapper和SQL执行</li>
 *   <li>测试DO转换逻辑</li>
 *   <li>事务自动回滚，不污染数据库</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026-01-16
 */
@DisplayName("订单仓储集成测试示例")
class OrderAggrRepositoryImplTest extends IntegrationTestBase {

    @Autowired
    private OrderAggrRepository orderRepository;

    @Autowired
    private OrderAggrMapper orderAggrMapper;

    @Test
    @DisplayName("保存订单 - 成功")
    void testSave_success() {
        // Given: 准备领域对象
        OrderId orderId = OrderId.generate();
        OrderAggr order = OrderAggr.create("CUST001", new Money(100.00, "CNY"));

        // When: 保存订单
        orderRepository.save(order);

        // Then: 验证数据库中有记录
        assertThat(orderAggrMapper.selectOneById(orderId.getValue())).isNotNull();
    }

    @Test
    @DisplayName("根据ID查询订单 - 成功")
    void testFindById_success() {
        // Given: 保存订单
        OrderAggr order = OrderAggr.create("CUST001", new Money(100.00, "CNY"));
        orderRepository.save(order);

        // When: 查询订单
        OrderAggr found = orderRepository.findById(order.getId());

        // Then: 验证查询结果
        assertThat(found).isNotNull();
        assertThat(found.getCustomerId()).isEqualTo("CUST001");
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("根据ID查询订单 - 不存在")
    void testFindById_notFound() {
        // When: 查询不存在的订单
        OrderAggr found = orderRepository.findById(OrderId.generate());

        // Then: 返回null
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("根据客户ID查询订单 - 成功")
    void testFindByCustomerId_success() {
        // Given: 保存多个订单
        OrderAggr order1 = OrderAggr.create("CUST001", new Money(100.00, "CNY"));
        OrderAggr order2 = OrderAggr.create("CUST001", new Money(200.00, "CNY"));
        orderRepository.save(order1);
        orderRepository.save(order2);

        // When: 查询客户订单
        List<OrderAggr> orders = orderRepository.findByCustomerId("CUST001");

        // Then: 验证查询结果
        assertThat(orders).isNotNull();
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getCustomerId()).isEqualTo("CUST001");
        assertThat(orders.get(1).getCustomerId()).isEqualTo("CUST001");
    }

    @Test
    @DisplayName("更新订单 - 成功")
    void testUpdate_success() {
        // Given: 保存订单
        OrderAggr order = OrderAggr.create("CUST001", new Money(100.00, "CNY"));
        orderRepository.save(order);

        // When: 更新订单状态
        order.pay();
        orderRepository.save(order);

        // Then: 验证数据库中的状态
        OrderAggr updated = orderRepository.findById(order.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("删除订单 - 成功")
    void testDelete_success() {
        // Given: 保存订单
        OrderAggr order = OrderAggr.create("CUST001", new Money(100.00, "CNY"));
        orderRepository.save(order);

        // When: 删除订单
        orderRepository.delete(order.getId());

        // Then: 验证数据库中没有记录
        OrderAggr deleted = orderRepository.findById(order.getId());
        assertThat(deleted).isNull();
    }
}
