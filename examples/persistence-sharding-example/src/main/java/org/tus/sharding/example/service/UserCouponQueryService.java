package org.tus.sharding.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tus.common.sharding.context.ShardingContext;
import org.tus.common.sharding.service.ShardingAwareQueryService;
import org.tus.sharding.example.entity.UserCoupon;

import java.util.Collections;
import java.util.List;

/**
 * Example service that uses ShardingAwareQueryService to query user coupons
 * with a required sharding context (userId).
 */
@Service
@RequiredArgsConstructor
public class UserCouponQueryService {
    private final ShardingAwareQueryService shardingAwareQueryService;

    /**
     * Find user coupons by user id. The sharding context must include userId
     * so that the query is routed to the correct partition(s).
     */
    public List<UserCoupon> findByUserId(Long userId) {
        ShardingContext context = ShardingContext.of(Collections.singletonMap("userId", userId));
        return shardingAwareQueryService.querySharded(
                UserCoupon.class,
                builder -> {
                    builder.from(UserCoupon.class).eq("userId", userId);
                    return builder.build();
                },
                context);
    }

    /**
     * Find a single user coupon by id. Sharding context must include userId.
     */
    public UserCoupon findById(String id, Long userId) {
        ShardingContext context = ShardingContext.of(Collections.singletonMap("userId", userId));
        return shardingAwareQueryService.findObjectByIdSharded(UserCoupon.class, id, context);
    }
}
