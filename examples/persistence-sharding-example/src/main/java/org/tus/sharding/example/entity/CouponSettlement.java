package org.tus.sharding.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.tus.common.sharding.common.ShardingKey;
import org.tus.common.sharding.entity.ShardedEntity;
import org.tus.common.sharding.entity.ShardedPersistedObject;

import java.util.Date;

/**
 * Coupon settlement entity — sharded by user_id.
 * Maps to t_coupon_settlement (PARTITION BY HASH(user_id) PARTITIONS 16).
 */
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

    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Column(name = "order_id")
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Column(name = "coupon_id")
    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Column(name = "create_time")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Column(name = "update_time")
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
