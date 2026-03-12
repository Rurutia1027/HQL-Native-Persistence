package org.tus.sharding.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.tus.common.sharding.common.ShardingKey;
import org.tus.common.sharding.entity.ShardedEntity;
import org.tus.common.sharding.entity.ShardedPersistedObject;

import java.util.Date;

/**
 * Coupon template entity — sharded by shop_number.
 * Maps to t_coupon_template (PARTITION BY HASH(shop_number) PARTITIONS 16).
 */
@Entity
@Table(name = "t_coupon_template")
@jakarta.persistence.AttributeOverride(name = "id", column = @Column(name = "id"))
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
    /** JSON string for receive_rule column (DB type: json). */
    private String receiveRule;
    /** JSON string for consume_rule column (DB type: json). */
    private String consumeRule;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Integer delFlag;

    @Column(name = "shop_number")
    public Long getShopNumber() {
        return shopNumber;
    }

    public void setShopNumber(Long shopNumber) {
        this.shopNumber = shopNumber;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "source")
    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    @Column(name = "target")
    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    @Column(name = "goods")
    public String getGoods() {
        return goods;
    }

    public void setGoods(String goods) {
        this.goods = goods;
    }

    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    @Column(name = "stock")
    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @Column(name = "receive_rule")
    public String getReceiveRule() {
        return receiveRule;
    }

    public void setReceiveRule(String receiveRule) {
        this.receiveRule = receiveRule;
    }

    @Column(name = "consume_rule")
    public String getConsumeRule() {
        return consumeRule;
    }

    public void setConsumeRule(String consumeRule) {
        this.consumeRule = consumeRule;
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
