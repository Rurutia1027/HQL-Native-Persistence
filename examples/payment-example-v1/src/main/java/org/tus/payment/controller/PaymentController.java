package org.tus.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tus.payment.dto.ApiResponse;
import org.tus.payment.dto.CreatePaymentRequest;
import org.tus.payment.dto.UpdatePaymentStatusRequest;
import org.tus.payment.entity.Payment;
import org.tus.payment.entity.PaymentRecord;
import org.tus.payment.service.PaymentService;

import java.util.List;
import java.util.UUID;

/**
 * Payment Controller - REST API for Payment operations
 */
@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Get user payments
     * GET /api/payments?userId={userId}&status={status}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Payment>>> getUserPayments(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer status) {
        try {
            List<Payment> payments = paymentService.getUserPayments(userId, status);
            return ResponseEntity.ok(ApiResponse.success(payments));
        } catch (Exception e) {
            log.error("Error getting user payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get payments: " + e.getMessage()));
        }
    }
    
    /**
     * Get payment by payment ID
     * GET /api/payments/{paymentId}?userId={userId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Payment>> getPayment(
            @PathVariable String paymentId,
            @RequestParam Long userId) {
        try {
            Payment payment = paymentService.getPaymentByPaymentId(paymentId, userId);
            if (payment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Payment not found", 404));
            }
            return ResponseEntity.ok(ApiResponse.success(payment));
        } catch (Exception e) {
            log.error("Error getting payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get payment: " + e.getMessage()));
        }
    }
    
    /**
     * Get payment by order ID
     * GET /api/payments/by-order/{orderId}?userId={userId}
     */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByOrderId(
            @PathVariable String orderId,
            @RequestParam Long userId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId, userId);
            if (payment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Payment not found for order", 404));
            }
            return ResponseEntity.ok(ApiResponse.success(payment));
        } catch (Exception e) {
            log.error("Error getting payment by order ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get payment: " + e.getMessage()));
        }
    }
    
    /**
     * Create payment
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> createPayment(@RequestBody CreatePaymentRequest request) {
        try {
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setOrderId(request.getOrderId());
            payment.setUserId(request.getUserId());
            payment.setPaymentAmount(request.getPaymentAmount());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setPaymentChannel(request.getPaymentChannel());
            payment.setNotifyUrl(request.getNotifyUrl());
            payment.setReturnUrl(request.getReturnUrl());
            payment.setClientIp(request.getClientIp());
            payment.setDeviceInfo(request.getDeviceInfo());
            payment.setRemark(request.getRemark());
            
            Payment created = paymentService.createPayment(payment);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Payment created successfully", created));
        } catch (Exception e) {
            log.error("Error creating payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create payment: " + e.getMessage()));
        }
    }
    
    /**
     * Update payment status
     * PUT /api/payments/{paymentId}/status
     */
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<ApiResponse<Payment>> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestBody UpdatePaymentStatusRequest request) {
        try {
            Payment payment = paymentService.updatePaymentStatus(
                    paymentId,
                    request.getUserId(),
                    request.getNewStatus(),
                    request.getThirdPartyTradeNo()
            );
            return ResponseEntity.ok(ApiResponse.success("Payment status updated", payment));
        } catch (RuntimeException e) {
            log.error("Error updating payment status", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error updating payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update payment status: " + e.getMessage()));
        }
    }
    
    /**
     * Get payment records
     * GET /api/payments/{paymentId}/records?userId={userId}
     */
    @GetMapping("/{paymentId}/records")
    public ResponseEntity<ApiResponse<List<PaymentRecord>>> getPaymentRecords(
            @PathVariable String paymentId,
            @RequestParam Long userId) {
        try {
            List<PaymentRecord> records = paymentService.getPaymentRecords(paymentId, userId);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            log.error("Error getting payment records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get payment records: " + e.getMessage()));
        }
    }
}
