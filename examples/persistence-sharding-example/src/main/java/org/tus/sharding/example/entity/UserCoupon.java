package org.tus.sharding.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.tus.common.sharding.common.ShardingKey;
import org.tus.common.sharding.entity.ShardedEntity;
import org.tus.common.sharding.entity.ShardedPersistedObject;

import java.util.Date;

/**
 * User coupon entity — sharded by user_id.
 * Maps to t_user_coupon (PARTITION BY HASH(user_id) PARTITIONS 32).
 */
@Entity
@Table(name = "t_user_coupon")
@jakarta.persistence.AttributeOverride(name = "id", column = @Column(name = "id"))
@ShardedEntity(shardingKeys = {"userId"})
public class UserCoupon extends ShardedPersistedObject {

    @ShardingKey
    private Long userId;
    private Long couponTemplateId;
    private Date receiveTime;
    private Integer receiveCount;
    private Date validStartTime;
    private Date validEndTime;
    private Date useTime;
    private Integer source;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Integer delFlag;

    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Column(name = "coupon_template_id")
    public Long getCouponTemplateId() {
        return couponTemplateId;
    }

    public void setCouponTemplateId(Long couponTemplateId) {
        this.couponTemplateId = couponTemplateId;
    }

    @Column(name = "receive_time")
    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    @Column(name = "receive_count")
    public Integer getReceiveCount() {
        return receiveCount;
    }

    public void setReceiveCount(Integer receiveCount) {
        this.receiveCount = receiveCount;
    }

    @Column(name = "valid_start_time")
    public Date getValidStartTime() {
        return validStartTime;
    }

    public void setValidStartTime(Date validStartTime) {
        this.validStartTime = validStartTime;
    }

    @Column(name = "valid_end_time")
    public Date getValidEndTime() {
        return validEndTime;
    }

    public void setValidEndTime(Date validEndTime) {
        this.validEndTime = validEndTime;
    }

    @Column(name = "use_time")
    public Date getUseTime() {
        return useTime;
    }

    public void setUseTime(Date useTime) {
        this.useTime = useTime;
    }

    @Column(name = "source")
    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
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

    @Column(name = "del_flag")
    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }
}
