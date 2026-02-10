package org.tus.common.sharding.service;

import org.tus.common.domain.persistence.QueryService;
import org.tus.common.sharding.util.ShardingUtil;

import java.util.List;
import java.util.Map;

/**
 * Implementation of ShardingAwareQueryService.
 * 
 * This implementation extends the base QueryService functionality with
 * sharding-aware operations. Most operations delegate to the base QueryService,
 * as ShardingSphere handles the routing transparently.
 */
public class ShardingAwareQueryServiceImpl implements ShardingAwareQueryService {

    private final QueryService delegate;

    public ShardingAwareQueryServiceImpl(QueryService delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> T findObjectByIdWithShardingKey(Class<T> clazz, String id, Long shardingKey) {
        // ShardingSphere will automatically route based on sharding key in WHERE clause
        // We need to include the sharding key in the query
        String hql = "from " + clazz.getName() + " where id = :id";
        Map<String, Object> params = Map.of("id", id);
        
        // If the entity has a sharding key field, include it in the query
        // This ensures ShardingSphere routes correctly
        // Note: The actual routing is handled by ShardingSphere based on table configuration
        Object result = delegate.querySingle(hql, params, null);
        return result != null ? clazz.cast(result) : null;
    }

    @Override
    public List queryWithShardingKeys(String hql, Map<String, Object> namedParameters, List<Long> shardingKeys) {
        // ShardingSphere automatically handles IN queries across shards
        // Just execute the query normally - ShardingSphere will route correctly
        if (shardingKeys != null && !shardingKeys.isEmpty()) {
            // Add sharding keys to parameters if needed
            Map<String, Object> params = new java.util.HashMap<>(namedParameters);
            params.put("shardingKeys", shardingKeys);
            return delegate.query(hql, params);
        }
        return delegate.query(hql, namedParameters);
    }

    @Override
    public ShardInfo getShardInfo(Long shardingKey, int shardingCount, int databaseCount, int tableCount) {
        int dbIndex = ShardingUtil.calculateDatabaseShard(shardingKey, shardingCount, databaseCount);
        int tableIndex = ShardingUtil.calculateTableShard(shardingKey, tableCount);
        
        String dbName = "ds_" + dbIndex;
        String tableName = "t_" + tableIndex; // Base name should be provided
        
        return new ShardInfo(dbIndex, tableIndex, dbName, tableName);
    }

    // Delegate all QueryService methods to the underlying service
    @Override
    public org.hibernate.Session openSession() {
        return delegate.openSession();
    }

    @Override
    public List query(String hql) {
        return delegate.query(hql);
    }

    @Override
    public List query(String hql, Object... params) {
        return delegate.query(hql, params);
    }

    @Override
    public List query(String hql, org.tus.common.domain.persistence.QueryPostProcessor post, Object... params) {
        return delegate.query(hql, post, params);
    }

    @Override
    public List query(String hql, Map<String, Object> namedParams) {
        return delegate.query(hql, namedParams);
    }

    @Override
    public List query(String hql, Map<String, Object> namedParams, org.tus.common.domain.persistence.QueryPostProcessor post) {
        return delegate.query(hql, namedParams, post);
    }

    @Override
    public List pagedQuery(String hql, Map<String, Object> namedParameters, Integer pageStart, Integer pageSize) {
        return delegate.pagedQuery(hql, namedParameters, pageStart, pageSize);
    }

    @Override
    public List pagedQuery(String hql, Map<String, Object> namedParameters, Integer pageStart, Integer pageSize, 
                           org.tus.common.domain.persistence.QueryPostProcessor post) {
        return delegate.pagedQuery(hql, namedParameters, pageStart, pageSize, post);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.SimplePersistedObject> T save(T item) {
        return delegate.save(item);
    }

    @Override
    public <T> T save(T item, boolean saveOrUpdate) {
        return delegate.save(item, saveOrUpdate);
    }

    @Override
    public <T> T delete(T item) {
        return delegate.delete(item);
    }

    @Override
    public int delete(String hql, Object... params) {
        return delegate.delete(hql, params);
    }

    @Override
    public <T> void deleteAll(List<T> objects) {
        delegate.deleteAll(objects);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.SimplePersistedObject> List<T> saveAll(List<T> itemList) {
        return delegate.saveAll(itemList);
    }

    @Override
    public <T> T save(T item) {
        return delegate.save(item);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.SimplePersistedObject> T delete(T item) {
        return delegate.delete(item);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.SimplePersistedObject> List<T> mergeAll(List<T> itemList) {
        return delegate.mergeAll(itemList);
    }

    @Override
    public List sqlQuery(String sql, Object... params) {
        return delegate.sqlQuery(sql, params);
    }

    @Override
    public List sqlQueryLimit(String sql, int limit, Object... params) {
        return delegate.sqlQueryLimit(sql, limit, params);
    }

    @Override
    public List<Object[]> sqlQueryArray(String sql, Object... params) {
        return delegate.sqlQueryArray(sql, params);
    }

    @Override
    public int sqlUpdate(String sql, Object... params) {
        return delegate.sqlUpdate(sql, params);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.UniqueNamedArtifact> T findObjectByName(Class<T> clazz, String name) {
        return delegate.findObjectByName(clazz, name);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.SimplePersistedObject> T findSimpleObjectById(Class<T> clazz, String objId, String typeName) {
        return delegate.findSimpleObjectById(clazz, objId, typeName);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.SimplePersistedObject> T findSimpleObjectById(Class<T> clazz, String objId) {
        return delegate.findSimpleObjectById(clazz, objId);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.UniqueNamedArtifact> T findObjectByName(Class<T> clazz, String name, 
                                                                                                org.tus.common.domain.persistence.QueryPostProcessor post) {
        return delegate.findObjectByName(clazz, name, post);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.PersistedObject> T findObjectById(Class<T> clazz, String id) {
        return delegate.findObjectById(clazz, id);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.PersistedObject> T findObjectById(Class<T> clazz, String id, 
                                                                                          org.tus.common.domain.persistence.QueryPostProcessor post) {
        return delegate.findObjectById(clazz, id, post);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.PersistedObject> T findObjectByIdOrName(Class<T> clazz, String idOrName) {
        return delegate.findObjectByIdOrName(clazz, idOrName);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.PersistedObject> T findObjectByIdOrName(Class<T> clazz, String idName, 
                                                                                               org.tus.common.domain.persistence.QueryPostProcessor post) {
        return delegate.findObjectByIdOrName(clazz, idName, post);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.PersistedObject> List<T> findObjectsByAndingParams(Class<T> tClass, 
                                                                                                            Map<String, String> params) {
        return delegate.findObjectsByAndingParams(tClass, params);
    }

    @Override
    public Object querySingle(String hql) {
        return delegate.querySingle(hql);
    }

    @Override
    public Object querySingle(String hql, Map<String, Object> namedParameters) {
        return delegate.querySingle(hql, namedParameters);
    }

    @Override
    public String queryByJdbc(String sql, Map<Integer, String> namedParameters, int i) {
        return delegate.queryByJdbc(sql, namedParameters, i);
    }

    @Override
    public int executeQuery(String hql, Map<String, Object> namedParameters) {
        return delegate.executeQuery(hql, namedParameters);
    }

    @Override
    public Object querySingle(String hql, Map<String, Object> namedParameters, 
                             org.tus.common.domain.persistence.QueryPostProcessor post) {
        return delegate.querySingle(hql, namedParameters, post);
    }

    @Override
    public <T extends org.tus.common.domain.persistence.NamedArtifact> T findOrSave(String hql, Map<String, Object> namedParameters, T item) {
        return delegate.findOrSave(hql, namedParameters, item);
    }
}
