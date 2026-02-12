package org.tus.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.tus.common.domain.persistence.PersistedObject;

import java.math.BigDecimal;

/**
 * PaymentRecord entity  Audit Log Records
 * Sharding Key: user_id
 */
@Entity
@Table(name = "t_payment_records")
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Data
public class PaymentRecord extends PersistedObject {
    @Column(name = "payment_id", nullable = false, length = 64)
    private String paymentId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Sharding Key

    // 0 - Pending Payment, 1 - Processing Payment, 2 - Payment Successful,
    // 3 - Payment Failed, 4 - Canceled, 5 - Refunded
    @Column(name = "record_type", nullable = false)
    private Integer recordType;

    @Column(name = "old_status")
    private Integer oldStatus;

    @Column(name = "new_status")
    private Integer newStatus;

    @Column(name = "operator_type")
    private Integer operatorType; // 1-User，2-System，3-Third Party

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
