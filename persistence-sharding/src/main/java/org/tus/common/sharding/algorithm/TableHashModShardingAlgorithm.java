package org.tus.common.sharding.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.List;

/**
 * Custom table sharding algorithm based on Hash Mod strategy.
 * 
 * This algorithm distributes data across multiple tables within a database
 * using a hash-based modulo operation. It's simpler than database sharding
 * as it only needs to select from available table names.
 * 
 * @author Extracted from onecoupon-main project, adapted for Hibernate/HQL
 */
public class TableHashModShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    /**
     * Performs precise sharding for a single sharding value.
     * 
     * @param availableTargetNames Available table names (e.g., t_user_coupon_0, t_user_coupon_1)
     * @param shardingValue The sharding value (e.g., user_id)
     * @return The selected table name
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        long id = shardingValue.getValue();
        int shardingCount = availableTargetNames.size();
        
        // Calculate which table to use based on hash modulo
        int mod = (int) hashShardingValue(id) % shardingCount;
        
        int index = 0;
        for (String targetName : availableTargetNames) {
            if (index == mod) {
                return targetName;
            }
            index++;
        }
        throw new IllegalArgumentException("No target table found for sharding value: " + id);
    }

    /**
     * Performs range sharding (not implemented, returns empty).
     * Range queries will query all tables.
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        // Range sharding not implemented - returns empty collection
        // This means range queries will query all tables
        return List.of();
    }

    /**
     * Hashes the sharding value to ensure even distribution.
     */
    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }

    /**
     * Returns the algorithm type identifier.
     */
    @Override
    public String getType() {
        return "TABLE_HASH_MOD";
    }
}
