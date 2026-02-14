package org.tus.common.sharding.service;

import org.tus.common.domain.persistence.QueryService;

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
 * but this service provides additional utilities for case where you need
 * explicit shard awareness.
 */
public interface ShardingAwareQueryService  extends QueryService {
    /**
     * Finds an object by ID with explicit sharding key.
     * The sharding key is included in the HQL so ShardingSphere can route to the correct
     * shard.
     * Uses default sharding property name "userId" for backward compatibility.
     *
     * @param clazz Entity class
     * @param id Entity ID
     * @param shardingKey The sharding key value (e.g., user_id for user_coupon table)
     * @param <T> Entity type
     * @return Found entity or null
     */
    <T> T findObjectByIdWithShardingKey(Class<T> clazz, String id, Long shardingKey);

    /**
     * Finds an object by ID with explicit sharding key and property name.
     * The sharding key is included in the HQL so ShardingSphere can route to the correct
     * shard.
     * Use this when the sharding column is not "userId" (e.g., shop_number -> "shopNumber").
     *
     * @param clazz Entity class
     * @param id Entity ID
     * @param shardingKeyPropertyName HQL property name of the sharding column (e.g.,
     *                                "userId", "shopNumber")
     * @param shardingKeyValue The sharding key value
     * @param <T> Entity type
     * @return Found entity or null
     */
    <T> T findObjectByIdWithShardingKey(Class<T> clazz, String id,
                                        String shardingKeyPropertyName,
                                        Object shardingKeyValue);

    /**
     * Executes a query with sharding keys. The HQL must contain a condition on the sharding
     * column (e.g., {@code user IN (:shardingKeys)} and the same param name must be used.
     * ShardingSphere will route based on the values in the query.
     *
     * @param hql             HQL query (must include sharding column in WHERE, e.g. userId IN
     *                        (:shardingKeys))
     * @param namedParameters Query parameters (shardingKeys will be merged if provided)
     * @param shardingKeys    List of sharding keys (for IN queries across shards)
     * @return Query results
     */
    List queryWithShardingKeys(String hql, Map<String, Object> namedParameters,
                               List<Long> shardingKeys);


    /**
     * Executes a query ensuring the sharding key participates in the query.
     * Appends {@code AND shardingKeyPropertyName IN (:__shardingKeys)} (or WHERE if no
     * WHERE present) so ShardingSphere can route correctly. Use this when you do not want
     * to hand-write the sharding condition in HQL.
     *
     * @param hql                     Base HQL query (may or may not have WHERE)
     * @param namedParameters         Query parameters (must not use key "__shardingKeys")
     * @param shardingKeyPropertyName HQL property name of the sharding column (e.g.,
     *                                "userId", "shopNumber")
     * @param shardingKeys            List of sharding key values
     * @return Query results
     */
    List queryWithShardingKeys(String hql, Map<String, Object> namedParameters,
                               String shardingKeyPropertyName, List<?> shardingKeys);

    /**
     * Gets the shard information for a given sharding key.
     *
     * @param shardingKey   The sharding key value
     * @param shardingCount Total sharding count
     * @param databaseCount Number of databases
     * @param tableCount    Number of tables per database
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
