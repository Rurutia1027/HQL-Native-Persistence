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
 * OrderItem entity
 * Sharding Key: user_id (the same as Order table)
 */
@Entity
@Table(name = "t_order_items")
@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Setter
public class OrderItem extends PersistedObject {
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Sharding Key (duplicated)

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sku_id")
    private Long skuId;

    @Column(name = "product_name", nullable = false, length = 256)
    private String productName;

    @Column(name = "product_image", length = 512)
    private String productImage;

    @Column(name = "sku_spec", length = 256)
    private String skuSpec;

    @Column(name = "unit_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "total_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "product_category", length = 64)
    private String productCategory;

    @Column(name = "brand_name", length = 64)
    private String brandName;
}
