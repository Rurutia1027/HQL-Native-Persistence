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
     * ShardingSphere routes to a single shard when the sharding column is in the WHERE clause.
     *
     * @param clazz Entity class
     * @param id Entity ID
     * @param shardingKey The sharding key (e.g., user_id value for user_coupon table)
     * @param <T> Entity type
     * @return Found entity or null
     * @deprecated Use {@link #findObjectByIdWithShardingKey(Class, String, String, Long)} with the entity's sharding column name (Java property, e.g. "userId") so routing is correct for all entities.
     */
    @Deprecated
    <T> T findObjectByIdWithShardingKey(Class<T> clazz, String id, Long shardingKey);

    /**
     * Finds an object by ID and sharding key with correct single-shard routing.
     * Includes the sharding column in the WHERE clause so ShardingSphere routes to one shard.
     *
     * @param clazz Entity class
     * @param id Entity primary key (e.g. order ID string, coupon ID string)
     * @param shardingColumnName Java property name of the sharding column (e.g. "userId", "shopId")
     * @param shardingKey The <b>value</b> of the sharding column for this row (e.g. 1001L for userId, 42L for shopId)
     * @param <T> Entity type
     * @return Found entity or null
     *
     * <p>Example (Order sharded by user_id):
     * <pre>{@code
     * Order order = service.findObjectByIdWithShardingKey(
     *     Order.class,
     *     "ord-abc-123",   // id: entity primary key
     *     "userId",       // shardingColumnName: Java property (Order.getUserId())
     *     1001L           // shardingKey: the actual user_id value for this order
     * );
     * }</pre>
     */
    <T> T findObjectByIdWithShardingKey(Class<T> clazz, String id, String shardingColumnName, Long shardingKey);

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
     * Returns generic table suffix only ("t_" + tableIndex). For full logical table name use the overload with logicalTableName.
     *
     * @param shardingKey The sharding key value
     * @param shardingCount Total sharding count
     * @param databaseCount Number of databases
     * @param tableCount Number of tables per database
     * @return Shard information (tableName is suffix only, e.g. "t_0")
     */
    ShardInfo getShardInfo(Long shardingKey, int shardingCount, int databaseCount, int tableCount);

    /**
     * Gets the shard information for a given sharding key with full logical table name.
     *
     * @param shardingKey The sharding key value
     * @param shardingCount Total sharding count
     * @param databaseCount Number of databases
     * @param tableCount Number of tables per database
     * @param logicalTableName Logical table name (e.g. "t_order", "t_user_coupon"); result tableName will be logicalTableName + "_" + tableIndex
     * @return Shard information with tableName as logicalTableName + "_" + tableIndex
     */
    ShardInfo getShardInfo(Long shardingKey, int shardingCount, int databaseCount, int tableCount, String logicalTableName);

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
