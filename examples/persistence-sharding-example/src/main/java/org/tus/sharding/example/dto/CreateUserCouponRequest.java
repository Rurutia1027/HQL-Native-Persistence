package org.tus.sharding.example.dto;

/**
 * Minimal request DTO for creating a UserCoupon record.
 *
 * Keep this intentionally small; it's only used to seed data to validate
 * TiDB partitioning + sharding-aware query discipline end-to-end.
 */
public class CreateUserCouponRequest {
    private Long userId;
    private Long couponTemplateId;
    private Integer receiveCount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCouponTemplateId() {
        return couponTemplateId;
    }

    public void setCouponTemplateId(Long couponTemplateId) {
        this.couponTemplateId = couponTemplateId;
    }

    public Integer getReceiveCount() {
        return receiveCount;
    }

    public void setReceiveCount(Integer receiveCount) {
        this.receiveCount = receiveCount;
    }
}

