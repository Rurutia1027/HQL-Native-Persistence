package org.tus.common.sharding.service;

import org.hibernate.Session;
import org.tus.common.domain.persistence.NamedArtifact;
import org.tus.common.domain.persistence.PersistedObject;
import org.tus.common.domain.persistence.QueryPostProcessor;
import org.tus.common.domain.persistence.QueryService;
import org.tus.common.domain.persistence.SimplePersistedObject;
import org.tus.common.domain.persistence.UniqueNamedArtifact;
import org.tus.common.sharding.util.ShardingUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ShardingAwareQueryService.
 *
 * This implementation extends the base QueryService functionality with
 * sharding-aware operations. All sharded queries include the sharding value
 * in the HQL so ShardingSphere can route to the correct shard(s).
 */
public class ShardingAwareQueryServiceImpl implements ShardingAwareQueryService {

    /**
     * Parameter name used when appending sharding key IN clause in queryWithShardingKeys.
     */
    public static final String SHARDING_KEYS_PARAM = "__shardingKeys";

    private final QueryService delegate;

    public ShardingAwareQueryServiceImpl(QueryService delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> T findObjectByIdWithShardingKey(Class<T> clazz, String id, Long shardingKey) {
        return findObjectByIdWithShardingKey(clazz, id, "userId", shardingKey);
    }

    @Override
    public <T> T findObjectByIdWithShardingKey(Class<T> clazz, String id, String shardingKeyPropertyName, Object shardingKeyValue) {
        if (shardingKeyPropertyName == null || shardingKeyPropertyName.isBlank()) {
            throw new IllegalArgumentException("shardingKeyPropertyName must not be null or blank");
        }
        if (shardingKeyValue == null) {
            throw new IllegalArgumentException("shardingKeyValue must not be null for shard routing");
        }
        String hql = "from " + clazz.getName() + " where id = :id and " + shardingKeyPropertyName + " = :shardingKeyValue";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("shardingKeyValue", shardingKeyValue);
        Object result = delegate.querySingle(hql, params, null);
        return result != null ? clazz.cast(result) : null;
    }

    @Override
    public List queryWithShardingKeys(String hql, Map<String, Object> namedParameters, List<Long> shardingKeys) {
        if (shardingKeys != null && !shardingKeys.isEmpty()) {
            Map<String, Object> params = namedParameters == null ? new HashMap<>() : new HashMap<>(namedParameters);
            params.put("shardingKeys", shardingKeys);
            return delegate.query(hql, params);
        }
        return delegate.query(hql, namedParameters != null ? namedParameters : Map.of());
    }

    @Override
    public List queryWithShardingKeys(String hql, Map<String, Object> namedParameters, String shardingKeyPropertyName, List<?> shardingKeys) {
        if (shardingKeyPropertyName == null || shardingKeyPropertyName.isBlank()) {
            throw new IllegalArgumentException("shardingKeyPropertyName must not be null or blank");
        }
        Map<String, Object> params = namedParameters == null ? new HashMap<>() : new HashMap<>(namedParameters);
        if (shardingKeys != null && !shardingKeys.isEmpty()) {
            String condition = shardingKeyPropertyName + " IN (:" + SHARDING_KEYS_PARAM + ")";
            String normalizedHql = hql.trim();
            boolean hasWhere = normalizedHql.toUpperCase().contains(" WHERE ");
            String fullHql = hasWhere ? (normalizedHql + " AND " + condition) : (normalizedHql + " WHERE " + condition);
            params.put(SHARDING_KEYS_PARAM, shardingKeys);
            return delegate.query(fullHql, params);
        }
        return delegate.query(hql, params);
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
    public Session openSession() {
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
    public List query(String hql, QueryPostProcessor post, Object... params) {
        return delegate.query(hql, post, params);
    }

    @Override
    public List query(String hql, Map<String, Object> namedParams) {
        return delegate.query(hql, namedParams);
    }

    @Override
    public List query(String hql, Map<String, Object> namedParams, QueryPostProcessor post) {
        return delegate.query(hql, namedParams, post);
    }

    @Override
    public List pagedQuery(String hql, Map<String, Object> namedParameters, Integer pageStart, Integer pageSize) {
        return delegate.pagedQuery(hql, namedParameters, pageStart, pageSize);
    }

    @Override
    public List pagedQuery(String hql, Map<String, Object> namedParameters, Integer pageStart, Integer pageSize,
                           QueryPostProcessor post) {
        return delegate.pagedQuery(hql, namedParameters, pageStart, pageSize, post);
    }

    @Override
    public <T extends SimplePersistedObject> T save(T item) {
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
    public <T extends SimplePersistedObject> List<T> saveAll(List<T> itemList) {
        return delegate.saveAll(itemList);
    }

    @Override
    public <T> T save(T item) {
        return delegate.save(item);
    }

    @Override
    public <T extends SimplePersistedObject> T delete(T item) {
        return delegate.delete(item);
    }

    @Override
    public <T extends SimplePersistedObject> List<T> mergeAll(List<T> itemList) {
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
    public <T extends UniqueNamedArtifact> T findObjectByName(Class<T> clazz, String name) {
        return delegate.findObjectByName(clazz, name);
    }

    @Override
    public <T extends SimplePersistedObject> T findSimpleObjectById(Class<T> clazz, String objId, String typeName) {
        return delegate.findSimpleObjectById(clazz, objId, typeName);
    }

    @Override
    public <T extends SimplePersistedObject> T findSimpleObjectById(Class<T> clazz, String objId) {
        return delegate.findSimpleObjectById(clazz, objId);
    }

    @Override
    public <T extends UniqueNamedArtifact> T findObjectByName(Class<T> clazz, String name,
                                                              QueryPostProcessor post) {
        return delegate.findObjectByName(clazz, name, post);
    }

    @Override
    public <T extends PersistedObject> T findObjectById(Class<T> clazz, String id) {
        return delegate.findObjectById(clazz, id);
    }

    @Override
    public <T extends PersistedObject> T findObjectById(Class<T> clazz, String id, QueryPostProcessor post) {
        return delegate.findObjectById(clazz, id, post);
    }

    @Override
    public <T extends PersistedObject> T findObjectByIdOrName(Class<T> clazz, String idOrName) {
        return delegate.findObjectByIdOrName(clazz, idOrName);
    }

    @Override
    public <T extends PersistedObject> T findObjectByIdOrName(Class<T> clazz, String idName, QueryPostProcessor post) {
        return delegate.findObjectByIdOrName(clazz, idName, post);
    }

    @Override
    public <T extends PersistedObject> List<T> findObjectsByAndingParams(Class<T> tClass,
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
    public Object querySingle(String hql, Map<String, Object> namedParameters, QueryPostProcessor post) {
        return delegate.querySingle(hql, namedParameters, post);
    }

    @Override
    public <T extends NamedArtifact> T findOrSave(String hql, Map<String, Object> namedParameters, T item) {
        return delegate.findOrSave(hql, namedParameters, item);
    }
}
