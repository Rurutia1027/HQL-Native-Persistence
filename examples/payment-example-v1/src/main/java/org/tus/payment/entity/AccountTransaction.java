package org.tus.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tus.common.domain.persistence.PersistedObject;

import java.math.BigDecimal;

/**
 * AccountTransaction entity - Account Transaction Table
 * Sharding key: user_id
 */
@Entity
@Table(name = "t_account_transactions")
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountTransaction extends PersistedObject {

    @Column(name = "transaction_id", unique = true, nullable = false, length = 64)
    private String transactionId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Sharding key

    // 1 - Top-up
    // 2 - Spending
    // 3 - Refund
    // 4 - Withdrawal
    // 5 - Freeze
    // 6 - Unfreeze
    @Column(name = "transaction_type", nullable = false)
    private Integer transactionType;

    // Positive values indicate income, negative values indicate expenses
    @Column(name = "transaction_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal transactionAmount;

    @Column(name = "balance_before", precision = 18, scale = 2, nullable = false)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 18, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "currency", length = 8)
    private String currency = "CNY";

    @Column(name = "order_id", length = 64)
    private String orderId;

    @Column(name = "payment_id", length = 64)
    private String paymentId;

    @Column(name = "refund_id", length = 64)
    private String refundId;

    // 1 - Success
    // 2 - Failure
    // 3 - Processing
    @Column(name = "transaction_status", nullable = false)
    private Integer transactionStatus = 1;

    @Column(name = "remark", length = 512)
    private String remark;

    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData; // JSON Format
}
