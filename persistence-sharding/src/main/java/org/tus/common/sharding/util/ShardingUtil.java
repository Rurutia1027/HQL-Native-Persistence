package org.tus.common.sharding.util;

import org.tus.common.sharding.algorithm.DBHashModShardingAlgorithm;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Utility class for sharding operations.
 * Provides helper methods to determine shard locations programmatically.
 * 
 * This is useful when you need to know which database/table a value will be
 * routed to before executing queries, or when handling cross-shard operations.
 */
public class ShardingUtil {

    /**
     * Calculates which database a sharding key should be routed to.
     * 
     * @param shardingKey The sharding key value (e.g., user_id, shop_number)
     * @param shardingCount Total number of shards (database * table combinations)
     * @param availableDatabases Number of available databases
     * @return The database index (0-based)
     */
    public static int calculateDatabaseShard(long shardingKey, int shardingCount, int availableDatabases) {
        long hash = Math.abs((long) Long.valueOf(shardingKey).hashCode());
        return (int) (hash % shardingCount / (shardingCount / availableDatabases));
    }

    /**
     * Calculates which table a sharding key should be routed to.
     * 
     * @param shardingKey The sharding key value
     * @param availableTables Number of available tables
     * @return The table index (0-based)
     */
    public static int calculateTableShard(long shardingKey, int availableTables) {
        long hash = Math.abs((long) Long.valueOf(shardingKey).hashCode());
        return (int) (hash % availableTables);
    }

    /**
     * Gets the actual table name for a sharded entity.
     * 
     * @param baseTableName Base table name (e.g., "t_user_coupon")
     * @param shardingKey The sharding key value
     * @param tableCount Number of table shards
     * @return The actual table name (e.g., "t_user_coupon_5")
     */
    public static String getShardedTableName(String baseTableName, long shardingKey, int tableCount) {
        int tableIndex = calculateTableShard(shardingKey, tableCount);
        return baseTableName + "_" + tableIndex;
    }

    /**
     * Gets the actual database name for a sharded entity.
     * 
     * @param baseDataSourceName Base data source name (e.g., "ds")
     * @param shardingKey The sharding key value
     * @param shardingCount Total sharding count
     * @param databaseCount Number of databases
     * @return The actual data source name (e.g., "ds_0")
     */
    public static String getShardedDataSourceName(String baseDataSourceName, long shardingKey, 
                                                   int shardingCount, int databaseCount) {
        int dbIndex = calculateDatabaseShard(shardingKey, shardingCount, databaseCount);
        return baseDataSourceName + "_" + dbIndex;
    }

    /**
     * Creates a DBHashModShardingAlgorithm instance with the given configuration.
     * Useful for programmatic sharding calculations.
     */
    public static DBHashModShardingAlgorithm createDBShardingAlgorithm(int shardingCount) {
        DBHashModShardingAlgorithm algorithm = new DBHashModShardingAlgorithm();
        Properties props = new Properties();
        props.setProperty("sharding-count", String.valueOf(shardingCount));
        algorithm.init(props);
        return algorithm;
    }

    /**
     * Gets available database names.
     * This should match the configuration in shardingsphere-config.yaml
     */
    public static Collection<String> getAvailableDatabases(int databaseCount) {
        List<String> databases = new java.util.ArrayList<>();
        for (int i = 0; i < databaseCount; i++) {
            databases.add("ds_" + i);
        }
        return databases;
    }
}
