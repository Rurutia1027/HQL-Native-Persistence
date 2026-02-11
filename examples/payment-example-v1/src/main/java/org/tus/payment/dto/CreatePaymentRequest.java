package org.tus.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Create Payment Request DTO
 */
@Data
public class CreatePaymentRequest {
    private String orderId;
    private Long userId;
    private BigDecimal paymentAmount;

    // 1 - Credit/Debit Card, 2 - PayPal, 3 - Bank Transfer, 4 - Account Balance, 5 - Other
    private Integer paymentMethod;
    private String paymentChannel;
    private String notifyUrl;
    private String returnUrl;
    private String clientIp;
    private String deviceInfo;
    private String remark;
}
