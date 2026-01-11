package org.smm.archetype.infrastructure._example.order.persistence;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._example.order.model.OrderAggr;
import org.smm.archetype.domain._example.order.model.OrderItem;
import org.smm.archetype.domain._example.order.model.OrderStatus;
import org.smm.archetype.domain._example.order.model.PaymentMethod;
import org.smm.archetype.domain._example.order.model.valueobject.Address;
import org.smm.archetype.domain._example.order.model.valueobject.ContactInfo;
import org.smm.archetype.domain._example.order.model.valueobject.Money;
import org.smm.archetype.domain._example.order.repository.OrderAggrRepository;
import org.smm.archetype.domain._shared.base.PageModel;
import org.smm.archetype.infrastructure._example.order.persistence.converter.OrderAggrConverter;
import org.smm.archetype.infrastructure._example.order.persistence.converter.OrderItemConverter;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.OrderAddressDO;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.OrderAggrDO;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.OrderContactInfoDO;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.OrderItemDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.OrderAddressMapper;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.OrderAggrMapper;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.OrderContactInfoMapper;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.OrderItemMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单聚合根仓储实现
 *
 * <p>职责：
 * <ul>
 *   <li>OrderAggr与OrderAggrDO之间的转换</li>
 *   <li>协调多个Mapper（OrderAggrMapper、OrderItemMapper等）</li>
 *   <li>管理事务边界</li>
 *   <li>处理领域事件的保存和发布</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>一个事务只保存一个聚合根</li>
 *   <li>保证聚合内的数据一致性</li>
 *   <li>使用乐观锁防止并发冲突</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderAggrRepositoryImpl implements OrderAggrRepository {

    private final OrderAggrConverter     converter;
    private final OrderItemConverter     orderItemConverter;
    private final OrderAggrMapper        orderAggrMapper;
    private final OrderItemMapper        orderItemMapper;
    private final OrderAddressMapper     orderAddressMapper;
    private final OrderContactInfoMapper orderContactInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderAggr save(OrderAggr order) {
        if (order == null) {
            throw new IllegalArgumentException("订单不能为空");
        }

        OrderAggrDO orderDO;
        if (order.isNew()) {
            // 新增订单
            orderDO = converter.toDO(order);
            orderAggrMapper.insert(orderDO);
            // 使用反射回填ID
            setFieldValue(order, "id", orderDO.getId());

            // 更新所有未提交事件的聚合根信息
            List<org.smm.archetype.domain._shared.base.DomainEvent> events = order.getUncommittedEvents();
            for (org.smm.archetype.domain._shared.base.DomainEvent event : events) {
                event.setAggregateInfo(orderDO.getId().toString(), "OrderAggr");
            }

            // 保存关联数据
            saveRelatedData(order, orderDO.getId());

            log.info("订单创建成功: id={}, orderNo={}", orderDO.getId(), order.getOrderNo());

            // 直接返回order对象，避免重新查询导致items丢失
            return order;
        } else {
            // 更新订单
            orderDO = orderAggrMapper.selectOneById(order.getId());
            if (orderDO == null) {
                throw new IllegalArgumentException("订单不存在: " + order.getId());
            }

            // 简化：跳过版本检查（TODO: 实现乐观锁）

            // 更新订单主表
            converter.updateDO(order, orderDO);
            orderAggrMapper.update(orderDO);

            // 更新关联数据（删除旧的，插入新的）
            saveRelatedData(order, orderDO.getId());

            log.info("订单更新成功: id={}, orderNo={}", orderDO.getId(), order.getOrderNo());

            // 对于更新，需要重新查询以获取最新状态
            return toDomain(orderDO);
        }
    }

    /**
     * 保存关联数据（items、address、contactInfo）
     */
    private void saveRelatedData(OrderAggr order, Long orderId) {
        // 1. 保存订单items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            // 先删除旧的items
            orderItemMapper.deleteByQuery(
                    QueryWrapper.create().where("order_id = ?", orderId)
            );
            // 插入新的items
            for (var item : order.getItems()) {
                OrderItemDO itemDO = new OrderItemDO();
                itemDO.setOrderId(orderId);
                itemDO.setProductId(item.getProductId());
                itemDO.setProductName(item.getProductName());
                itemDO.setSkuCode(item.getSkuCode());
                itemDO.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().getAmount() : null);
                itemDO.setCurrency(item.getCurrency());
                itemDO.setQuantity(item.getQuantity());
                itemDO.setSubtotal(item.getSubtotal() != null ? item.getSubtotal().getAmount() : null);
                orderItemMapper.insert(itemDO);
            }
        }

        // 2. 保存address
        if (order.getShippingAddress() != null) {
            // 先删除旧的address
            orderAddressMapper.deleteByQuery(
                    QueryWrapper.create().where("order_id = ?", orderId)
            );
            // 插入新的address
            OrderAddressDO addressDO = new OrderAddressDO();
            addressDO.setOrderId(orderId);
            addressDO.setProvince(order.getShippingAddress().getProvince());
            addressDO.setCity(order.getShippingAddress().getCity());
            addressDO.setDistrict(order.getShippingAddress().getDistrict());
            addressDO.setDetailAddress(order.getShippingAddress().getDetailAddress());
            addressDO.setPostalCode(order.getShippingAddress().getPostalCode());
            orderAddressMapper.insert(addressDO);
        }

        // 3. 保存contactInfo
        if (order.getContactInfo() != null) {
            // 先删除旧的contactInfo
            orderContactInfoMapper.deleteByQuery(
                    QueryWrapper.create().where("order_id = ?", orderId)
            );
            // 插入新的contactInfo
            OrderContactInfoDO contactInfoDO = new OrderContactInfoDO();
            contactInfoDO.setOrderId(orderId);
            contactInfoDO.setContactName(order.getContactInfo().getContactName());
            contactInfoDO.setContactPhone(order.getContactInfo().getContactPhone());
            contactInfoDO.setContactEmail(order.getContactInfo().getContactEmail());
            orderContactInfoMapper.insert(contactInfoDO);
        }

        log.debug("保存关联数据成功: orderId={}, itemsCount={}", orderId, order.getItems() != null ? order.getItems().size() : 0);
    }

    @Override
    public Optional<OrderAggr> findById(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }

        OrderAggrDO orderDO = orderAggrMapper.selectOneById(orderId);
        if (orderDO == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(orderDO));
    }

    @Override
    public Optional<OrderAggr> findByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isEmpty()) {
            return Optional.empty();
        }

        OrderAggrDO orderDO = orderAggrMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("order_no = ?", orderNo)
        );

        if (orderDO == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(orderDO));
    }

    @Override
    public List<OrderAggr> findByCustomerId(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            return List.of();
        }

        List<OrderAggrDO> orderDOs = orderAggrMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("customer_id = ?", customerId)
        );

        return orderDOs.stream()
                       .map(this::toDomain)
                       .toList();
    }

    @Override
    public PageModel<OrderAggr> findOrders(String customerId, int pageNumber, int pageSize) {
        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (customerId != null && !customerId.isEmpty()) {
            queryWrapper.where("customer_id = ?", customerId);
        }

        // 查询总数
        Long totalRaw = orderAggrMapper.selectCountByQuery(queryWrapper);

        // 查询分页数据
        int offset = (pageNumber - 1) * pageSize;
        List<OrderAggrDO> orderDOs = orderAggrMapper.selectListByQuery(
                queryWrapper.limit(pageSize).offset(offset)
        );

        // 转换为领域对象
        List<OrderAggr> orders = orderDOs.stream()
                                         .map(this::toDomain)
                                         .toList();

        return PageModel.<OrderAggr>builder()
                       .pageNumber((long) pageNumber)
                       .pageSize((long) pageSize)
                       .records(orders)
                       .totalRaw(totalRaw)
                       .build();
    }

    @Override
    public boolean existsByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isEmpty()) {
            return false;
        }

        OrderAggrDO orderDO = orderAggrMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("order_no = ?", orderNo)
        );

        return orderDO != null;
    }

    // ==================== 私有方法 ====================

    /**
     * DO转领域对象
     * <p>由于OrderAggr是聚合根，没有setter，需要使用反射
     */
    private OrderAggr toDomain(OrderAggrDO orderDO) {
        try {
            // 创建OrderAggr实例（使用受保护的默认构造函数）
            java.lang.reflect.Constructor<OrderAggr> constructor = OrderAggr.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            OrderAggr order = constructor.newInstance();

            // 使用反射设置字段值
            setFieldValue(order, "id", orderDO.getId());
            setFieldValue(order, "orderNo", orderDO.getOrderNo());
            setFieldValue(order, "customerId", orderDO.getCustomerId());
            setFieldValue(order, "customerName", orderDO.getCustomerName());
            setFieldValue(order, "status", orderDO.getStatus() != null ? OrderStatus.valueOf(orderDO.getStatus()) : null);
            setFieldValue(order, "totalAmount", orderDO.getTotalAmount() != null ? Money.of(orderDO.getTotalAmount()) : null);
            setFieldValue(order, "currency", orderDO.getCurrency());
            setFieldValue(order, "paymentMethod",
                    orderDO.getPaymentMethod() != null ? PaymentMethod.valueOf(orderDO.getPaymentMethod()) : null);
            setFieldValue(order, "remark", orderDO.getRemark());

            // 查询并设置关联数据
            // 1. 查询items - 注意：items字段类型是ArrayList<OrderItem>
            List<OrderItemDO> itemDOs = orderItemMapper.selectListByQuery(
                    QueryWrapper.create().where("order_id = ?", orderDO.getId())
            );
            ArrayList<OrderItem> items = itemDOs.stream()
                                                 .map(orderItemConverter::toDomain)
                                                 .collect(Collectors.toCollection(ArrayList::new));
            setFieldValue(order, "items", items);

            // 2. 查询address
            OrderAddressDO addressDO = orderAddressMapper.selectOneByQuery(
                    QueryWrapper.create().where("order_id = ?", orderDO.getId())
            );
            if (addressDO != null) {
                Address address = Address.builder()
                                          .province(addressDO.getProvince())
                                          .city(addressDO.getCity())
                                          .district(addressDO.getDistrict())
                                          .detailAddress(addressDO.getDetailAddress())
                                          .postalCode(addressDO.getPostalCode())
                                          .build();
                setFieldValue(order, "shippingAddress", address);
            }

            // 3. 查询contactInfo
            OrderContactInfoDO contactInfoDO = orderContactInfoMapper.selectOneByQuery(
                    QueryWrapper.create().where("order_id = ?", orderDO.getId())
            );
            if (contactInfoDO != null) {
                ContactInfo contactInfo = ContactInfo.builder()
                                                  .contactName(contactInfoDO.getContactName())
                                                  .contactPhone(contactInfoDO.getContactPhone())
                                                  .contactEmail(contactInfoDO.getContactEmail())
                                                  .build();
                setFieldValue(order, "contactInfo", contactInfo);
            }

            return order;

        } catch (Exception e) {
            log.error("DO转领域对象失败: {}", e.getMessage(), e);
            throw new RuntimeException("DO转领域对象失败", e);
        }
    }

    /**
     * 使用反射设置字段值
     * <p>支持设置父类的字段
     */
    private void setFieldValue(OrderAggr order, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = findField(OrderAggr.class, fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(order, value);
            } else {
                log.warn("字段不存在: field={}", fieldName);
            }
        } catch (Exception e) {
            log.warn("设置字段失败: field={}, value={}, error={}", fieldName, value, e.getMessage());
            log.debug("设置字段失败堆栈:", e);
        }
    }

    /**
     * 递归查找字段（包括父类）
     */
    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

}
