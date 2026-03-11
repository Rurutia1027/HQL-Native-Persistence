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
 * User coupon entity — sharded by user_id.
 * Maps to t_user_coupon (PARTITION BY HASH(user_id) PARTITIONS 32).
 */
@Entity
@Getter
@Setter
@Data
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

}
