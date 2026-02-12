package org.tus.payment.analytics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Business metrics DTO for analytics queries.
 * Represents aggregated business data across shards.
 */
@Data
@Getter
@Setter
public class BusinessMetrics {
    private Long totalOrders;
    private Long totalPayments;
    private Long totalUsers;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefundAmount;
    private BigDecimal averageOrderValue;
    private Date startDate;
    private Date endDate;

    // Payment method distribution
    private Long alipayCount;
    private Long wechatCount;
    private Long bankCardCount;
    private Long balanceCount;

    // Order status distribution
    private Long pendingPaymentCount;
    private Long paidCount;
    private Long shippedCount;
    private Long completedCount;
    private Long cancelledCount;
    private Long refundedCount;

    // Payment status distribution
    private Long paymentPendingCount;
    private Long paymentSuccessCount;
    private Long paymentFailedCount;

    public BusinessMetrics() {
    }
}
