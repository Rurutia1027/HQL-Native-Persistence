package org.tus.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tus.common.domain.dao.HqlQueryBuilder;
import org.tus.common.domain.persistence.QueryService;
import org.tus.payment.entity.Order;
import org.tus.payment.entity.OrderItem;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Order Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final QueryService queryService;

    /**
     * Query User Order List
     */
    public List<Order> getUserOrders(Long userId, Integer orderStatus) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Order.class, "o")
                .select("o")
                .eq("o.userId", userId)
                .isNull("o.deleted");

        if (orderStatus != null) {
            builder.and().eq("o.orderStatus", orderStatus);
        }

        builder.orderBy("o.createDate", false);
        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        return queryService.query(hql, params);
    }

    /**
     * Query Order via orderId and userId
     */
    public Order getOrderByOrderId(String orderId, Long userId) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Order.class, "o")
                .select("o")
                .eq("o.orderId", orderId)
                .and()
                .eq("o.userId", userId)
                .isNull("o.deleted");
        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        Object result = queryService.querySingle(hql, params, null);
        return result != null ? (Order) result : null;
    }

    /**
     * Query Order and Order items
     */
    public Order getOrderWithItems(String orderId, Long userId) {
        // Query Order
        Order order = getOrderByOrderId(orderId, userId);
        if (order == null) {
            return null;
        }

        // Query Item in same shard
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(OrderItem.class, "oi")
                .select("oi")
                .eq("oi.orderId", orderId)
                .and()
                .eq("oi.userId", userId)
                .isNull("oi.deleted")
                .orderBy("oi.createdDate", true);

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        List<OrderItem> items = queryService.query(hql, params);
        return order;
    }

    /**
     * Create order
     */
    @Transactional
    public Order createOrder(Order order) {
        if (order.getOrderId() == null) {
            order.setOrderId(generateOrderId());
        }
        return queryService.save(order);
    }

    /**
     * Update order status
     */
    @Transactional
    public Order updateOrderStatus(String orderId, Long userId, Integer newStatus) {
        Order order = getOrderByOrderId(orderId, userId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        order.setOrderStatus(newStatus);
        return queryService.save(order);
    }

    /**
     * Generate Order ID
     */
    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
