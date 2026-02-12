package org.tus.payment.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.stereotype.Service;
import org.tus.common.domain.dao.HqlQueryBuilder;
import org.tus.common.domain.persistence.QueryService;
import org.tus.common.sharding.service.ShardingAwareQueryService;
import org.tus.payment.analytics.BusinessMetrics;
import org.tus.payment.entity.Payment;
import org.tus.payment.entity.Refund;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business Analytics Service - Complex cross-shard queries for business metrics.
 * <p>
 * This service demonstrates:
 * - Cross-shard aggregation queries (no sharding key in WHERE clause)
 * - Complex business metrics calculation
 * - Multi-table joins across shards
 * - Performance considerations for analytics queries
 */
@Service
@RequiredArgsConstructor
public class BusinessAnalyticsService {
    private final QueryService queryService;

    private static ShardingAwareQueryService shardingAwareQueryService;

    /**
     * Get overall business metrics across all shards.
     * This is a cross-shard query - ShardingSphere will query all shards and merge results.
     *
     * @param startDate Start date for metrics calculation.
     * @param endDate   End date for metrics calculation.
     * @return Business metrics aggregated from all shards.
     */
    public BusinessMetrics getOverallMetrics(Date startDate, Date endDate) {
        BusinessMetrics businessMetrics = new BusinessMetrics();
        businessMetrics.setStartDate(startDate);
        businessMetrics.setEndDate(endDate);

        // 1. Total order count (cross-shard aggregation)
        Long totalOrders = countOrders(startDate, endDate);
        businessMetrics.setTotalOrders(totalOrders);

        // 2. Total payments count (cross-shard aggregation)
        Long totalPayments = countPayments(startDate, endDate);
        businessMetrics.setTotalPayments(totalPayments);

        // 3. Total revenue from successful payments (cross-shard aggregation)
        BigDecimal totalRevenue = calculateTotalRevenue(startDate, endDate);
        businessMetrics.setTotalRevenue(totalRevenue);

        // 4. Total refund amount (cross-shard)
        BigDecimal totalRefundAmount = calculateTotalRefundAmount(startDate, endDate);
        businessMetrics.setTotalRefundAmount(totalRefundAmount);

        // 5. Average order value
        if (totalOrders != null && totalOrders > 0 && totalRevenue != null) {
            BigDecimal avgOrderValue = totalRevenue.divide(
                    BigDecimal.valueOf(totalOrders),
                    2,
                    RoundingMode.HALF_UP
            );
            businessMetrics.setAverageOrderValue(avgOrderValue);
        }

        // 6. Payment method distribution (cross-shard aggregation)
        Map<Integer, Long> paymentMethodCounts = getPaymentMethodDistribution(startDate,
                endDate);
        businessMetrics.setAlipayCount(paymentMethodCounts.getOrDefault(1, 0L));
        businessMetrics.setWechatCount(paymentMethodCounts.getOrDefault(2, 0L));
        businessMetrics.setBankCardCount(paymentMethodCounts.getOrDefault(3, 0L));

        // 7. Order status distribution (cross-shard aggregation)
        Map<Integer, Long> orderStatusCounts = getOrderStatusDistribution(startDate, endDate);
        businessMetrics.setPendingPaymentCount(orderStatusCounts.getOrDefault(0, 0L));
        businessMetrics.setPaidCount(orderStatusCounts.getOrDefault(1, 0L));
        businessMetrics.setShippedCount(orderStatusCounts.getOrDefault(2, 0L));
        businessMetrics.setCompletedCount(orderStatusCounts.getOrDefault(3, 0L));
        businessMetrics.setCancelledCount(orderStatusCounts.getOrDefault(4, 0L));
        businessMetrics.setRefundedCount(orderStatusCounts.getOrDefault(5, 0L));

        // 8. Payment status distribution (cross-shard aggregation)
        Map<Integer, Long> paymentStatusCounts = getPaymentStatusDistribution(startDate, endDate);
        businessMetrics.setPaymentPendingCount(paymentStatusCounts.getOrDefault(0, 0L));
        businessMetrics.setPaymentSuccessCount(paymentStatusCounts.getOrDefault(2, 0L));
        businessMetrics.setPaymentFailedCount(paymentStatusCounts.getOrDefault(3, 0L));

        return businessMetrics;
    }

    /**
     * Count order across all shards (cross-shard query).
     * No sharding key in WHERE clause - queries all shards.
     */
    private Long countOrders(Date startDate, Date endDate) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Order.class, "o")
                .select("COUNT(o)")
                .isNull("o.deleted");

        if (startDate != null) {
            builder.and().ge("o.createdDate", startDate);
        }

        if (endDate != null) {
            builder.and().le("o.createdDate", endDate);
        }

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        List<Object> results = queryService.query(hql, params);
        if (results != null && !results.isEmpty()) {
            Object count = results.get(0);
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
        }

        return 0L;
    }

    /**
     * Count payments across all shards (cross-shard query).
     */
    private Long countPayments(Date startDate, Date endDate) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Payment.class, "p")
                .select("COUNT(p)")
                .isNull("p.deleted");

        if (startDate != null) {
            builder.and().ge("p.createdDate", startDate);
        }

        if (endDate != null) {
            builder.and().le("p.createdDate", endDate);
        }

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        List<Object> results = queryService.query(hql, params);
        if (results != null && !results.isEmpty()) {
            Object count = results.get(0);
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
        }
        return 0L;
    }

    /**
     * Calculate total revenue from successful payments (cross-shard aggregation).
     */
    private BigDecimal calculateTotalRevenue(Date startDate, Date endDate) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Payment.class, "p")
                .select("SUM(p.paymentAmount)")
                .eq("p.paymentStatus", 2) // 2 = PaymentStatus.SUCCESS
                .isNull("p.deleted");

        if (startDate != null) {
            builder.and().ge("p.payTime", startDate);
        }

        if (endDate != null) {
            builder.and().le("p.payTime", endDate);
        }

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        List<Object> results = queryService.query(hql, params);
        if (results != null && !results.isEmpty()) {
            Object sum = results.get(0);
            if (sum instanceof BigDecimal) {
                return (BigDecimal) sum;
            } else if (sum instanceof Number) {
                return BigDecimal.valueOf(((Number) sum).doubleValue());
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculate total refund amount (cross-shard aggregation).
     */
    private BigDecimal calculateTotalRefundAmount(Date startDate, Date endDate) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Refund.class, "r")
                .select("SUM(r.refundAmount)")
                .eq("r.refundStatus", 5) // 5 = RefundStatus.SUCCESS
                .isNull("r.deleted");

        if (startDate != null) {
            builder.and().ge("r.refundTime", startDate);
        }

        if (endDate != null) {
            builder.and().le("r.refundTime", endDate);
        }

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        List<Object> results = queryService.query(hql, params);
        if (results != null && !results.isEmpty()) {
            Object sum = results.get(0);
            if (sum instanceof BigDecimal) {
                return (BigDecimal) sum;
            } else if (sum instanceof Number) {
                return BigDecimal.valueOf(((Number) sum).doubleValue());
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Get payment method distribution (cross-shard aggregation).
     * Returns map of payment_method -> count
     */
    private Map<Integer, Long> getPaymentMethodDistribution(Date startDate, Date endDate) {
        // Note: This is a simplified version. In production, you might use GROUP BY
        // For now, we query each payment method separately
        Map<Integer, Long> distribution = new HashMap<>();
        for (int method = 1; method <= 5; method++) {
            HqlQueryBuilder builder = new HqlQueryBuilder();
            builder.fromAs(Payment.class, "p")
                    .select("COUNT(p)")
                    .eq("p.paymentMethod", method)
                    .isNull("p.deleted");

            if (startDate != null) {
                builder.and().ge("p.createdDate", startDate);
            }

            if (endDate != null) {
                builder.and().le("p.createdDate", endDate);
            }

            String hql = builder.build();
            Map<String, Object> params = builder.getInjectionParameters();
            builder.clear();

            List<Object> results = queryService.query(hql, params);
            if (results != null && !results.isEmpty()) {
                Object count = results.get(0);
                if (count instanceof Number) {
                    distribution.put(method, ((Number) count).longValue());
                }
            }


        }
        return distribution;
    }

    /**
     * Get order status distribution (cross-shard aggregation).
     */
    private Map<Integer, Long> getOrderStatusDistribution(Date startDate, Date endDate) {
        java.util.Map<Integer, Long> distribution = new java.util.HashMap<>();

        for (int status = 0; status <= 5; status++) {
            HqlQueryBuilder builder = new HqlQueryBuilder();
            builder.fromAs(Order.class, "o")
                    .select("COUNT(o)")
                    .eq("o.orderStatus", status)
                    .isNull("o.deleted");

            if (startDate != null) {
                builder.and().ge("o.createdDate", startDate);
            }
            if (endDate != null) {
                builder.and().le("o.createdDate", endDate);
            }

            String hql = builder.build();
            Map<String, Object> params = builder.getInjectionParameters();
            builder.clear();

            List<Object> results = queryService.query(hql, params);
            if (results != null && !results.isEmpty()) {
                Object count = results.get(0);
                if (count instanceof Number) {
                    distribution.put(status, ((Number) count).longValue());
                }
            }
        }

        return distribution;
    }

    /**
     * Get payment status distribution (cross-shard aggregation).
     */
    private Map<Integer, Long> getPaymentStatusDistribution(Date startDate, Date endDate) {
        java.util.Map<Integer, Long> distribution = new java.util.HashMap<>();

        for (int status = 0; status <= 5; status++) {
            HqlQueryBuilder builder = new HqlQueryBuilder();
            builder.fromAs(Payment.class, "p")
                    .select("COUNT(p)")
                    .eq("p.paymentStatus", status)
                    .isNull("p.deleted");

            if (startDate != null) {
                builder.and().ge("p.createdDate", startDate);
            }
            if (endDate != null) {
                builder.and().le("p.createdDate", endDate);
            }

            String hql = builder.build();
            Map<String, Object> params = builder.getInjectionParameters();
            builder.clear();

            List<Object> results = queryService.query(hql, params);
            if (results != null && !results.isEmpty()) {
                Object count = results.get(0);
                if (count instanceof Number) {
                    distribution.put(status, ((Number) count).longValue());
                }
            }
        }

        return distribution;
    }

    /**
     * Get user-specific metrics (single-shard query - optimized).
     * This query uses sharding key, so it only queries one shard.
     */
    public BusinessMetrics getUserMetrics(Long userId, Date startDate, Date endDate) {
        BusinessMetrics metrics = new BusinessMetrics();
        metrics.setStartDate(startDate);
        metrics.setEndDate(endDate);

        // These queries use userId (sharding key), so they only query one shard
        Long userOrders = countUserOrders(userId, startDate, endDate);
        metrics.setTotalOrders(userOrders);

        BigDecimal userRevenue = calculateUserRevenue(userId, startDate, endDate);
        metrics.setTotalRevenue(userRevenue);

        if (userOrders != null && userOrders > 0 && userRevenue != null) {
            BigDecimal avgOrderValue = userRevenue.divide(
                    BigDecimal.valueOf(userOrders),
                    2,
                    RoundingMode.HALF_UP
            );
            metrics.setAverageOrderValue(avgOrderValue);
        }

        return metrics;
    }

    private Long countUserOrders(Long userId, Date startDate, Date endDate) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Order.class, "o")
                .select("COUNT(o)")
                .eq("o.userId", userId)
                .and()
                .isNull("o.deleted");

        if (startDate != null) {
            builder.and().ge("o.createdDate", startDate);
        }
        if (endDate != null) {
            builder.and().le("o.createdDate", endDate);
        }

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        List<Object> results = queryService.query(hql, params);
        if (results != null && !results.isEmpty()) {
            Object count = results.get(0);
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
        }
        return 0L;
    }

    private BigDecimal calculateUserRevenue(Long userId, Date startDate, Date endDate) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(Payment.class, "p")
                .select("SUM(p.paymentAmount)")
                .eq("p.userId", userId)
                .and()
                .eq("p.paymentStatus", 2)
                .and()
                .isNull("p.deleted");

        if (startDate != null) {
            builder.and().ge("p.payTime", startDate);
        }
        if (endDate != null) {
            builder.and().le("p.payTime", endDate);
        }

        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();

        List<Object> results = queryService.query(hql, params);
        if (results != null && !results.isEmpty()) {
            Object sum = results.get(0);
            if (sum instanceof BigDecimal) {
                return (BigDecimal) sum;
            } else if (sum instanceof Number) {
                return BigDecimal.valueOf(((Number) sum).doubleValue());
            }
        }
        return BigDecimal.ZERO;
    }
}
