package org.tus.common.sharding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tus.common.domain.persistence.QueryService;
import org.tus.common.sharding.common.ShardingMeta;
import org.tus.common.sharding.common.ShardingMode;
import org.tus.common.sharding.service.ShardingAwareQueryService;
import org.tus.common.sharding.service.ShardingAwareQueryServiceImpl;

/**
 * Minimal Spring configuration to wire up the sharding-aware QueryService
 * on top of the core {@link org.tus.common.domain.persistence.QueryService}.
 * <p>
 * Usage in a Spring Boot application:
 * <pre>
 *     @Configuration
 *     @Import(TiDBShardingSpringConfig.class)
 *     public class PersistenceConfig {
 *          @Bean
 *          public ShardingMeta shardingMeta() {
 *              return ShardingMeta.builder()
 *                  .register(UserCoupon.class, "tenantId", "userId")
 *                  .register(CouponTemplate.class, "tenantId", "shopNumber")
 *                  .build()
 *          }
 *     }
 * </pre>
 * <p>
 * Then inject {@link ShardingAwareQueryService} into our service
 */
@Configuration
public class TiDBShardingSpringConfig {
    /**
     * Controls how strictly sharding constraints are enforced.
     * Acceptable values: STRICT, OBSERVE (case-intensive).
     */
    @Value("${persistence.sharding.mode:STRICT}")
    private String shardingModeProperty;

    @Bean
    public ShardingAwareQueryService shardingAwareQueryService(QueryService queryService,
                                                               ShardingMeta shardingMeta) {
        ShardingMode mode = parseMode(shardingModeProperty);
        return new ShardingAwareQueryServiceImpl(queryService, shardingMeta, mode);
    }


    private ShardingMode parseMode(String value) {
        if (value == null) {
            return ShardingMode.STRICT;
        }
        try {
            return ShardingMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            // Fallback to STRICT if configuration is invalid
            return ShardingMode.STRICT;
        }
    }
}
