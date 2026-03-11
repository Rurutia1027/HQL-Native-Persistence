package org.tus.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tus.common.domain.persistence.PersistedObject;

import java.math.BigDecimal;

/**
 * PaymentRecord entity - PaymentRecord Table（Audit Log）
 * Sharding Key: user_id
 */
@Entity
@Table(name = "t_payment_records")
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentRecord extends PersistedObject {

    @Column(name = "payment_id", nullable = false, length = 64)
    private String paymentId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Sharding Key

    // 1-Create Payment, 2-Paying, 3-Payment Successful, 4-Payment Failed, 5-Payment Canceled, 6-Refunded
    @Column(name = "record_type", nullable = false)
    private Integer recordType;

    @Column(name = "old_status")
    private Integer oldStatus;

    @Column(name = "new_status")
    private Integer newStatus;

    @Column(name = "operator_type")
    private Integer operatorType; // 1 - User 2 - System 3 - Third Party

    @Column(name = "operator_id", length = 64)
    private String operatorId;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "third_party_trade_no", length = 128)
    private String thirdPartyTradeNo;

    @Column(name = "remark", length = 512)
    private String remark;

    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData; // JSON Format
}
