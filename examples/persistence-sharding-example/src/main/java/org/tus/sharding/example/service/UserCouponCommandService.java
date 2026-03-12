package org.tus.sharding.example.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tus.common.domain.persistence.QueryService;
import org.tus.sharding.example.dto.CreateUserCouponRequest;
import org.tus.sharding.example.entity.UserCoupon;

import java.util.Date;

@Service
public class UserCouponCommandService {

    private final QueryService queryService;

    public UserCouponCommandService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Transactional
    public UserCoupon create(CreateUserCouponRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }

        Date now = new Date();

        UserCoupon coupon = new UserCoupon();
        coupon.setUserId(request.getUserId());
        coupon.setCouponTemplateId(request.getCouponTemplateId());
        coupon.setReceiveCount(request.getReceiveCount() != null ? request.getReceiveCount() : 1);
        coupon.setReceiveTime(now);
        coupon.setCreateTime(now);
        coupon.setUpdateTime(now);
        coupon.setDelFlag(0);

        // PersistedObject will generate UUID id automatically via its @GeneratedValue generator.
        return queryService.save(coupon);
    }
}

