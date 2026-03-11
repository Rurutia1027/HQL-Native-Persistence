package org.tus.sharding.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.tus.common.sharding.common.ShardingKey;
import org.tus.common.sharding.entity.ShardedEntity;
import org.tus.common.sharding.entity.ShardedPersistedObject;

import java.util.Date;

/**
 * Coupon settlement entity — sharded by user_id.
 * Maps to t_coupon_settlement (PARTITION BY HASH(user_id) PARTITIONS 16).
 */
@Data
@Setter
@Getter
@Entity
@Table(name = "t_coupon_settlement")
@jakarta.persistence.AttributeOverride(name = "id", column = @Column(name = "id"))
@ShardedEntity(shardingKeys = {"userId"})
public class CouponSettlement extends ShardedPersistedObject {

    @ShardingKey
    private Long userId;
    private Long orderId;
    private Long couponId;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
