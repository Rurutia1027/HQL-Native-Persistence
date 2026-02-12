package org.tus.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.tus.common.domain.persistence.PersistedObject;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Order entity
 * Sharding Key: user_id
 * Note: This entity belongs to E-Commerce Module (separate DB instance)
 */
@Entity
@Table(name = "t_orders")
@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Setter
public class Order extends PersistedObject {

    @Column(name = "order_id", unique = true, nullable = false, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Sharding Key

    @Column(name = "shop_id")
    private Long shopId;

    @Column(name = "shop_name", length = 128)
    private String shopName;

    @Column(name = "total_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "shipping_fee", precision = 18, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "actual_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal actualAmount;

    @Column(name = "currency", length = 8)
    private String currency = "CNY";

    // 0 - Pending Payment, 1 - Processing Payment, 2 - Payment Successful,
    // 3 - Payment Failed, 4 - Canceled, 5 - Refunded
    @Column(name = "order_status", nullable = false)
    private Integer orderStatus;

    // 0 - Unpaid, 1 - Paid, 2 - Payment Failed, 3 - Refunded
    @Column(name = "pay_status")
    private Integer payStatus;

    // 0 - Not Shipped, 1 - Shipped, 2 - In Transit, 3 - Delivered
    @Column(name = "shipping_status")
    private Integer shippingStatus;

    @Column(name = "receiver_name", nullable = false, length = 64)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 32)
    private String receiverPhone;

    @Column(name = "receiver_address", nullable = false, columnDefinition = "TEXT")
    private String receiverAddress;

    @Column(name = "receiver_postcode", length = 16)
    private String receiverPostcode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "pay_time")
    private Date payTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ship_time")
    private Date shipTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "complete_time")
    private Date completeTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "cancel_time")
    private Date cancelTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expire_time")
    private Date expireTime;

    @Column(name = "user_remark", length = 512)
    private String userRemark;

    @Column(name = "admin_remark", length = 512)
    private String adminRemark;

    @Column(name = "source")
    private Integer source; // 0-APP，1-Web，2-Mobile App，3-H5

    @Column(name = "channel", length = 32)
    private String channel;

    @Column(name = "promotion_info", columnDefinition = "TEXT")
    private String promotionInfo; // JSON Format

}
