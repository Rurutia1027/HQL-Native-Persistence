package org.tus.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tus.common.domain.persistence.PersistedObject;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Refund entity - Refund Table
 * Sharding key: user_id
 */
@Entity
@Table(name = "t_refunds")
@Data
@EqualsAndHashCode(callSuper = true)
public class Refund extends PersistedObject {

    @Column(name = "refund_id", unique = true, nullable = false, length = 64)
    private String refundId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "payment_id", nullable = false, length = 64)
    private String paymentId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Sharding Key

    @Column(name = "refund_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal refundAmount;

    @Column(name = "currency", length = 8)
    private String currency = "CNY";

    // 1 - Full Refund
    // 2 - Partial Refund
    // 3 - Refund Only (No Return Required)
    @Column(name = "refund_type", nullable = false)
    private Integer refundType;

    @Column(name = "refund_reason", length = 256)
    private String refundReason;

    // 0 - Pending Application
    // 1 - Under Review
    // 2 - Approved
    // 3 - Rejected
    // 4 - Refund In Progress
    // 5 - Refund Successful
    // 6 - Refund Failed
    @Column(name = "refund_status", nullable = false)
    private Integer refundStatus;

    @Column(name = "third_party_refund_no", length = 128)
    private String thirdPartyRefundNo;

    @Column(name = "third_party_trade_no", length = 128)
    private String thirdPartyTradeNo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "apply_time", nullable = false)
    private Date applyTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "approve_time")
    private Date approveTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "refund_time")
    private Date refundTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "complete_time")
    private Date completeTime;

    @Column(name = "approver_id", length = 64)
    private String approverId;

    @Column(name = "approver_name", length = 64)
    private String approverName;

    @Column(name = "approve_remark", length = 512)
    private String approveRemark;

    // 1 - Refund to Original Payment Method
    // 2 - Refund to Account Balance
    // 3 - Refund to Bank Card
    @Column(name = "refund_method")
    private Integer refundMethod;

    @Column(name = "bank_account", length = 128)
    private String bankAccount;

    @Column(name = "bank_name", length = 128)
    private String bankName;
}
