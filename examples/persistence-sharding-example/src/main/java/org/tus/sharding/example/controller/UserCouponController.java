package org.tus.sharding.example.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tus.sharding.example.entity.UserCoupon;
import org.tus.sharding.example.service.UserCouponQueryService;

import java.util.List;

/**
 * Example REST endpoints that query user coupons with a sharding context (userId)
 * Always pass userId so that STRICT mode can validate the sharding key.
 */
@RestController
@RequestMapping("/api/user-coupons")
@RequiredArgsConstructor
public class UserCouponController {
    private final UserCouponQueryService userCouponQueryService;

    /**
     * List coupons for a user. Requires userId (sharding key).
     * Example: GET /api/user-coupons?userId=1001
     */
    @GetMapping
    public ResponseEntity<List<UserCoupon>> listByUser(@RequestParam Long userId) {
        List<UserCoupon> list = userCouponQueryService.findByUserId(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get one coupon by id. Requires userId (sharding key).
     * Example: GET /api/user-coupons/abc-123?userId=1001
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserCoupon> getById(@PathVariable String id,
                                              @RequestParam Long userId) {
        UserCoupon coupon = userCouponQueryService.findById(id, userId);
        return coupon != null ? ResponseEntity.ok(coupon) :
                ResponseEntity.notFound().build();
    }

}
