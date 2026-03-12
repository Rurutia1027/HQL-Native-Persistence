package org.tus.sharding.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tus.sharding.example.dto.CreateUserCouponRequest;
import org.tus.sharding.example.entity.UserCoupon;
import org.tus.sharding.example.service.UserCouponCommandService;
import org.tus.sharding.example.service.UserCouponQueryService;

import java.util.List;

/**
 * Example REST endpoints that query user coupons with a sharding context (userId).
 * Always pass userId so that STRICT mode can validate the sharding key.
 */
@RestController
@RequestMapping("/api/user-coupons")
public class UserCouponController {

    private final UserCouponQueryService userCouponQueryService;
    private final UserCouponCommandService userCouponCommandService;

    public UserCouponController(UserCouponQueryService userCouponQueryService,
                                UserCouponCommandService userCouponCommandService) {
        this.userCouponQueryService = userCouponQueryService;
        this.userCouponCommandService = userCouponCommandService;
    }

    /**
     * List coupons for a user. Requires userId (sharding key).
     * Example: GET /api/user-coupons?userId=1001
     */
    @GetMapping
    public ResponseEntity<List<UserCoupon>> listByUser(@RequestParam("userId") Long userId) {
        List<UserCoupon> list = userCouponQueryService.findByUserId(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get one coupon by id. Requires userId (sharding key).
     * Example: GET /api/user-coupons/abc-123?userId=1001
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserCoupon> getById(@PathVariable("id") String id,
                                              @RequestParam("userId") Long userId) {
        UserCoupon coupon = userCouponQueryService.findById(id, userId);
        return coupon != null ? ResponseEntity.ok(coupon) : ResponseEntity.notFound().build();
    }

    /**
     * Create a user coupon record (for testing sharding end-to-end).
     *
     * Example:
     * curl -X POST "http://localhost:8081/api/user-coupons" \
     *   -H "Content-Type: application/json" \
     *   -d '{"userId":1001,"couponTemplateId":2001,"receiveCount":1}'
     */
    @PostMapping
    public ResponseEntity<UserCoupon> create(@RequestBody CreateUserCouponRequest request) {
        UserCoupon created = userCouponCommandService.create(request);
        return ResponseEntity.ok(created);
    }
}
