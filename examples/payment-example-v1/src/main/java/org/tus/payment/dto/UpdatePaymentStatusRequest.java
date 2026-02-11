package org.tus.payment.dto;

import lombok.Data;

/**
 * Update Payment Status Request DTO
 */
@Data
public class UpdatePaymentStatusRequest {
    private String paymentId;
    private Long userId;

    // 0 - Pending Payment
    // 1 - Paid
    // 2 - Shipped
    // 3 - Completed
    // 4 - Canceled
    // 5 - Refunded
    private Integer newStatus;
    private String thirdPartyTradeNo;
}
