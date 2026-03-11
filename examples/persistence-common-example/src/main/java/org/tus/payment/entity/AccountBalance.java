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
 * AccountBalance entity - Account Balance
 * Sharding Key: user_id
 */
@Entity
@Table(name = "t_account_balance")
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountBalance extends PersistedObject {

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId; // Sharding Key

    @Column(name = "available_balance", precision = 18, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "frozen_balance", precision = 18, scale = 2)
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    @Column(name = "total_balance", precision = 18, scale = 2)
    private BigDecimal totalBalance = BigDecimal.ZERO;

    @Column(name = "currency", length = 8)
    private String currency = "CNY";

    // 0 - Active
    // 1 - Frozen
    // 2 - Closed
    @Column(name = "account_status")
    private Integer accountStatus;

    @Column(name = "credit_limit", precision = 18, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_transaction_time")
    private Date lastTransactionTime;
}
