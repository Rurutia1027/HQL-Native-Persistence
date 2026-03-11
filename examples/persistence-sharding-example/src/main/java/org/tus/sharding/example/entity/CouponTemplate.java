package org.tus.sharding.example.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.tus.common.sharding.common.ShardingKey;
import org.tus.common.sharding.entity.ShardedEntity;
import org.tus.common.sharding.entity.ShardedPersistedObject;

import java.util.Date;
import java.util.Map;

/**
 * Coupon template entity — sharded by shop_number.
 * Maps to t_coupon_template (PARTITION BY HASH(shop_number) PARTITIONS 16).
 */
@Getter
@Setter
@Data
@Entity
@Table(name = "t_coupon_template")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@ShardedEntity(shardingKeys = {"shopNumber"})
public class CouponTemplate extends ShardedPersistedObject {

    @ShardingKey
    private Long shopNumber;
    private String name;
    private Integer source;
    private Integer target;
    private String goods;
    private Integer type;
    private Date validStartTime;
    private Date validEndTime;
    private Integer stock;
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> receiveRule;
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> consumeRule;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Integer delFlag;
}
