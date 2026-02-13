package org.smm.archetype.infrastructure.exampleorder.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.base.PageResult;
import org.smm.archetype.domain.exampleorder.model.OrderAggr;
import org.smm.archetype.domain.exampleorder.repository.OrderAggrRepository;

import java.util.List;
import java.util.Optional;

/**
 * 订单仓储实现，管理多个Mapper和事务边界。
 *
 */
@Slf4j
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderAggrRepository {

    @Override
    public OrderAggr save(OrderAggr order) {
        return null;
    }

    @Override
    public Optional<OrderAggr> findById(Long orderId) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderAggr> findByOrderNo(String orderNo) {
        return Optional.empty();
    }

    @Override
    public List<OrderAggr> findByCustomerId(String customerId) {
        return List.of();
    }

    @Override
    public PageResult<OrderAggr> findOrders(String customerId, Long pageNumber, Long pageSize) {
        return null;
    }

    @Override
    public boolean existsByOrderNo(String orderNo) {
        return false;
    }

}
