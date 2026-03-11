package org.tus.common.sharding.service;

import org.tus.common.domain.model.Page;
import org.tus.common.domain.model.PageResponse;
import org.tus.common.domain.persistence.PersistedObject;
import org.tus.common.sharding.context.ShardingContext;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Sharding-aware facade on top of the core QueryService.
 *
 * This interface is intentionally small and focused on the most common access patterns:
 * - find by id
 * - list query
 * - paged query
 *
 * All operations take a ShardingContext so that callers are forced to think about
 * sharding/tenant keys when accessing sharded entities.
 */
public interface ShardingAwareQueryService {

    <T extends PersistedObject> T findObjectByIdSharded(Class<T> entityClass,
                                                        String id,
                                                        ShardingContext context);

    <T extends PersistedObject> List<T> querySharded(Class<T> rootEntity,
                                                     Function<org.tus.common.domain.dao.HqlQueryBuilder, String> hqlBuilderFn,
                                                     ShardingContext context);

    <T extends PersistedObject> PageResponse<T> pagedQuerySharded(Class<T> rootEntity,
                                                                  Function<org.tus.common.domain.dao.HqlQueryBuilder, String> hqlBuilderFn,
                                                                  Map<String, Object> extraParams,
                                                                  Page page,
                                                                  ShardingContext context);
}

