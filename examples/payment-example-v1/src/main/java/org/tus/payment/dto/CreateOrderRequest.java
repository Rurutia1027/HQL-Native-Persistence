package org.tus.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Create Order Request DTO
 */
@Data
public class CreateOrderRequest {
    private Long userId;
    private Long shopId;
    private String shopName;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal actualAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String receiverPostcode;
    private String userRemark;

    // 0 - Mobile App
    // 1 - Web
    // 2 - Mini Program (or Instant App)
    // 3 - Mobile Web (H5)
    private Integer source;
    private String channel;
}
