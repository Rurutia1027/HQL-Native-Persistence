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
 * Payment entity - Payment Table
 * Sharding Key: user_id
 */
@Entity
@Table(name = "t_payments")
@Data
@EqualsAndHashCode(callSuper = true)
public class Payment extends PersistedObject {

    @Column(name = "payment_id", unique = true, nullable = false, length = 64)
    private String paymentId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Sharding key

    @Column(name = "payment_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal paymentAmount;

    @Column(name = "currency", length = 8)
    private String currency = "USD";

    // 1 - Credit/Debit Card, 2 - PayPal, 3 - Bank Transfer, 4 - Account Balance, 5 - Other
    @Column(name = "payment_method", nullable = false)
    private Integer paymentMethod;

    @Column(name = "payment_channel", length = 32)
    private String paymentChannel;

    // 0 - Pending Payment
    // 1 - Processing Payment
    // 2 - Payment Successful
    // 3 - Payment Failed
    // 4 - Canceled
    // 5 - Refunded
    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus;

    @Column(name = "third_party_trade_no", length = 128)
    private String thirdPartyTradeNo;

    @Column(name = "third_party_order_no", length = 128)
    private String thirdPartyOrderNo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "pay_time")
    private Date payTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expire_time")
    private Date expireTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "notify_time")
    private Date notifyTime;

    @Column(name = "notify_url", length = 512)
    private String notifyUrl;

    @Column(name = "return_url", length = 512)
    private String returnUrl;

    @Column(name = "callback_data", columnDefinition = "TEXT")
    private String callbackData; // JSON格式

    @Column(name = "error_code", length = 32)
    private String errorCode;

    @Column(name = "error_message", length = 256)
    private String errorMessage;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(name = "device_info", length = 128)
    private String deviceInfo;

    @Column(name = "remark", length = 512)
    private String remark;
}
