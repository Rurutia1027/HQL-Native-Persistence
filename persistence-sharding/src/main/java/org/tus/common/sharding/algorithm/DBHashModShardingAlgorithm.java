package org.tus.common.sharding.algorithm;

import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Custom database sharding algorithm based on Hash Mod strategy.
 * 
 * This algorithm distributes data across multiple databases using a hash-based
 * modulo operation. It supports both precise sharding (single value) and can
 * be extended for range sharding.
 * 
 * Configuration properties:
 * - sharding-count: Total number of shards (database + table combinations)
 * - business-tag: Optional tag for algorithm identification
 * 
 * @author Extracted from onecoupon-main project, adapted for Hibernate/HQL
 */
public class DBHashModShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    private Properties props;
    private int shardingCount;
    private static final String SHARDING_COUNT_KEY = "sharding-count";

    /**
     * Performs precise sharding for a single sharding value.
     * 
     * @param availableTargetNames Available database names (e.g., ds_0, ds_1)
     * @param shardingValue The sharding value (e.g., user_id, shop_number)
     * @return The selected database name
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        long id = shardingValue.getValue();
        int dbSize = availableTargetNames.size();
        
        // Calculate which database to use based on hash modulo
        int mod = (int) hashShardingValue(id) % shardingCount / (shardingCount / dbSize);
        
        int index = 0;
        for (String targetName : availableTargetNames) {
            if (index == mod) {
                return targetName;
            }
            index++;
        }
        throw new IllegalArgumentException("No target database found for sharding value: " + id);
    }

    /**
     * Performs range sharding (not implemented in original, returns empty).
     * Can be extended for range queries if needed.
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        // Range sharding not implemented - returns empty collection
        // This means range queries will query all shards
        return List.of();
    }

    /**
     * Initializes the algorithm with configuration properties.
     */
    @Override
    public void init(Properties props) {
        this.props = props;
        shardingCount = getShardingCount(props);
    }

    /**
     * Gets the sharding count from properties.
     */
    private int getShardingCount(final Properties props) {
        if (!props.containsKey(SHARDING_COUNT_KEY)) {
            throw new ShardingAlgorithmInitializationException(
                getType(), 
                "Sharding count cannot be null. Please configure 'sharding-count' property."
            );
        }
        String countStr = props.getProperty(SHARDING_COUNT_KEY);
        try {
            return Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            throw new ShardingAlgorithmInitializationException(
                getType(),
                "Invalid sharding-count value: " + countStr
            );
        }
    }

    /**
     * Calculates the sharding mod for a given ID and available target size.
     * This method can be used programmatically to determine which database/table
     * a value should be routed to.
     * 
     * @param id The sharding key value
     * @param availableTargetSize Number of available targets (databases or tables)
     * @return The mod index (0-based)
     */
    public int getShardingMod(long id, int availableTargetSize) {
        return (int) hashShardingValue(id) % shardingCount / (shardingCount / availableTargetSize);
    }

    /**
     * Hashes the sharding value to ensure even distribution.
     * Uses the hashCode of the value and takes absolute value.
     */
    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }

    /**
     * Returns the algorithm type identifier.
     */
    @Override
    public String getType() {
        return "DB_HASH_MOD";
    }
}
