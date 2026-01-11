package org.smm.archetype.app._example.order.query;

import org.smm.archetype.domain._shared.base.Query;

/**
 * 订单列表查询
 * @author Leonardo
 * @since 2026/1/11
 */
public class OrderListQuery implements Query {

    /**
     * 客户ID（可选）
     */
    private String customerId;

    /**
     * 页码（从1开始）
     */
    private int pageNumber = 1;

    /**
     * 每页大小
     */
    private int pageSize = 10;

    public OrderListQuery() {
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 计算偏移量
     */
    public int getOffset() {
        return (pageNumber - 1) * pageSize;
    }

}
