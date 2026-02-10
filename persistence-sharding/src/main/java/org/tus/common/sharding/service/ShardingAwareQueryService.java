package org.tus.common.sharding.service;

import org.tus.common.domain.persistence.QueryService;
import org.tus.common.sharding.util.ShardingUtil;

import java.util.List;
import java.util.Map;

/**
 * Extension of QueryService that provides sharding-aware operations.
 * 
 * This service adds methods to work with sharded entities while maintaining
 * compatibility with the base QueryService interface. It provides utilities
 * for determining shard locations and handling cross-shard queries.
 * 
 * Note: Most sharding logic is handled transparently by ShardingSphere,
 * but this service provides additional utilities for cases where you need
 * explicit shard awareness.
 */
public interface ShardingAwareQueryService extends QueryService {

    /**
     * Finds an object by ID with explicit sharding key.
     * Useful when the sharding key is different from the ID.
     * 
     * @param clazz Entity class
     * @param id Entity ID
     * @param shardingKey The sharding key (e.g., user_id for user_coupon table)
     * @param <T> Entity type
     * @return Found entity or null
     */
    <T> T findObjectByIdWithShardingKey(Class<T> clazz, String id, Long shardingKey);

    /**
     * Executes a query that may span multiple shards.
     * ShardingSphere will automatically route to correct shards.
     * 
     * @param hql HQL query
     * @param namedParameters Query parameters
     * @param shardingKeys List of sharding keys (for IN queries across shards)
     * @return Query results
     */
    List queryWithShardingKeys(String hql, Map<String, Object> namedParameters, List<Long> shardingKeys);

    /**
     * Gets the shard information for a given sharding key.
     * 
     * @param shardingKey The sharding key value
     * @param shardingCount Total sharding count
     * @param databaseCount Number of databases
     * @param tableCount Number of tables per database
     * @return Shard information
     */
    ShardInfo getShardInfo(Long shardingKey, int shardingCount, int databaseCount, int tableCount);

    /**
     * Represents shard location information.
     */
    class ShardInfo {
        private final int databaseIndex;
        private final int tableIndex;
        private final String databaseName;
        private final String tableName;

        public ShardInfo(int databaseIndex, int tableIndex, String databaseName, String tableName) {
            this.databaseIndex = databaseIndex;
            this.tableIndex = tableIndex;
            this.databaseName = databaseName;
            this.tableName = tableName;
        }

        public int getDatabaseIndex() {
            return databaseIndex;
        }

        public int getTableIndex() {
            return tableIndex;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public String getTableName() {
            return tableName;
        }

        @Override
        public String toString() {
            return String.format("ShardInfo{db=%s, table=%s}", databaseName, tableName);
        }
    }
}
