package org.tus.common.sharding.service;

import org.tus.common.domain.dao.HqlQueryBuilder;
import org.tus.common.domain.model.Page;
import org.tus.common.domain.model.PageResponse;
import org.tus.common.domain.persistence.PersistedObject;
import org.tus.common.domain.persistence.QueryService;
import org.tus.common.sharding.common.ShardingMeta;
import org.tus.common.sharding.common.ShardingMode;
import org.tus.common.sharding.context.ShardingContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Default implementation of ShardingAwareQueryService.
 *
 * It delegates all actual work to the underlying QueryService, but:
 * - looks up sharding metadata for the root entity
 * - validates that required sharding keys are present in the ShardingContext
 *
 * In STRICT mode, violations result in an IllegalStateException.
 * In OBSERVE mode, violations are allowed but can be recorded via hooks (to be added later).
 */
public class ShardingAwareQueryServiceImpl implements ShardingAwareQueryService {

    private final QueryService delegate;
    private final ShardingMeta shardingMeta;
    private final ShardingMode mode;

    public ShardingAwareQueryServiceImpl(QueryService delegate,
                                         ShardingMeta shardingMeta,
                                         ShardingMode mode) {
        this.delegate = delegate;
        this.shardingMeta = shardingMeta;
        this.mode = mode;
    }

    @Override
    public <T extends PersistedObject> T findObjectByIdSharded(Class<T> entityClass,
                                                               String id,
                                                               ShardingContext context) {
        validateShardingContext(entityClass, context);
        return delegate.findObjectById(entityClass, id);
    }

    @Override
    public <T extends PersistedObject> List<T> querySharded(Class<T> rootEntity,
                                                            Function<HqlQueryBuilder, String> hqlBuilderFn,
                                                            ShardingContext context) {
        validateShardingContext(rootEntity, context);

        HqlQueryBuilder builder = new HqlQueryBuilder();
        String hql = hqlBuilderFn.apply(builder);

        Map<String, Object> params = new HashMap<>(builder.getInjectionParameters());
        builder.clear();

        @SuppressWarnings("unchecked")
        List<T> results = (List<T>) delegate.query(hql, params);
        return results;
    }

    @Override
    public <T extends PersistedObject> PageResponse<T> pagedQuerySharded(Class<T> rootEntity,
                                                                         Function<HqlQueryBuilder, String> hqlBuilderFn,
                                                                         Map<String, Object> extraParams,
                                                                         Page page,
                                                                         ShardingContext context) {
        validateShardingContext(rootEntity, context);

        HqlQueryBuilder builder = new HqlQueryBuilder();
        String hql = hqlBuilderFn.apply(builder);

        Map<String, Object> params = new HashMap<>(builder.getInjectionParameters());
        if (extraParams != null && !extraParams.isEmpty()) {
            params.putAll(extraParams);
        }
        builder.clear();

        @SuppressWarnings("unchecked")
        List<T> data = (List<T>) delegate.pagedQuery(hql, params, page.getStart(), page.getPageSize());

        PageResponse<T> response = new PageResponse<>();
        response.setStart(page.getStart());
        response.setPageSize(page.getPageSize());
        response.setElements(data);
        // Total can be set by callers if they run a separate count query.
        return response;
    }

    private void validateShardingContext(Class<?> entityClass, ShardingContext context) {
        if (!shardingMeta.isShardedEntity(entityClass)) {
            return;
        }

        Set<String> requiredKeys = shardingMeta.getShardingKeys(entityClass);
        if (requiredKeys.isEmpty()) {
            return;
        }

        for (String key : requiredKeys) {
            if (!context.hasKey(key)) {
                handleViolation(entityClass, key);
            }
        }
    }

    private void handleViolation(Class<?> entityClass, String missingKey) {
        String message = "Missing required sharding key '" + missingKey +
                "' for sharded entity " + entityClass.getName();

        if (mode == ShardingMode.STRICT) {
            throw new IllegalStateException(message);
        } else {
            // OBSERVE mode – for now we just use standard error logging.
            // Later this can be extended to Micrometer metrics / OTEL logs.
            System.err.println("[Sharding][OBSERVE] " + message);
        }
    }
}

