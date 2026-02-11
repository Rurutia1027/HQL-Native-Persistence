package org.tus.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tus.common.domain.persistence.QueryService;
import org.tus.payment.dto.ApiResponse;
import org.tus.payment.dto.CreateOrderRequest;
import org.tus.payment.entity.Order;
import org.tus.payment.entity.OrderItem;
import org.tus.payment.service.OrderService;

import java.util.List;
import java.util.UUID;

/**
 * Order Controller - REST API for Order operations
 */
@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private QueryService queryService;
    
    /**
     * Get user orders
     * GET /api/orders?userId={userId}&status={status}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer status) {
        try {
            List<Order> orders = orderService.getUserOrders(userId, status);
            return ResponseEntity.ok(ApiResponse.success(orders));
        } catch (Exception e) {
            log.error("Error getting user orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get orders: " + e.getMessage()));
        }
    }
    
    /**
     * Get order by order ID
     * GET /api/orders/{orderId}?userId={userId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrder(
            @PathVariable String orderId,
            @RequestParam Long userId) {
        try {
            Order order = orderService.getOrderByOrderId(orderId, userId);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Order not found", 404));
            }
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (Exception e) {
            log.error("Error getting order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get order: " + e.getMessage()));
        }
    }
    
    /**
     * Get order with items
     * GET /api/orders/{orderId}/items?userId={userId}
     */
    @GetMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<Order>> getOrderWithItems(
            @PathVariable String orderId,
            @RequestParam Long userId) {
        try {
            Order order = orderService.getOrderWithItems(orderId, userId);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Order not found", 404));
            }
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (Exception e) {
            log.error("Error getting order with items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get order: " + e.getMessage()));
        }
    }
    
    /**
     * Create order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setUserId(request.getUserId());
            order.setShopId(request.getShopId());
            order.setShopName(request.getShopName());
            order.setTotalAmount(request.getTotalAmount());
            order.setDiscountAmount(request.getDiscountAmount());
            order.setShippingFee(request.getShippingFee());
            order.setActualAmount(request.getActualAmount() != null 
                    ? request.getActualAmount() 
                    : request.getTotalAmount().subtract(
                            request.getDiscountAmount() != null ? request.getDiscountAmount() : java.math.BigDecimal.ZERO
                    ).add(request.getShippingFee() != null ? request.getShippingFee() : java.math.BigDecimal.ZERO));
            order.setOrderStatus(0); // Pending Payment
            order.setPayStatus(0); // No Payment Yet
            order.setShippingStatus(0); // No Deliver yet
            order.setReceiverName(request.getReceiverName());
            order.setReceiverPhone(request.getReceiverPhone());
            order.setReceiverAddress(request.getReceiverAddress());
            order.setReceiverPostcode(request.getReceiverPostcode());
            order.setUserRemark(request.getUserRemark());
            order.setSource(request.getSource());
            order.setChannel(request.getChannel());
            
            Order created = orderService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Order created successfully", created));
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create order: " + e.getMessage()));
        }
    }
    
    /**
     * Update order status
     * PUT /api/orders/{orderId}/status?userId={userId}
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam Long userId,
            @RequestParam Integer newStatus) {
        try {
            Order order = orderService.updateOrderStatus(orderId, userId, newStatus);
            return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
        } catch (RuntimeException e) {
            log.error("Error updating order status", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error updating order status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update order status: " + e.getMessage()));
        }
    }
}
