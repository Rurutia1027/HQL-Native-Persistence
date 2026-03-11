package org.tus.common.sharding.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Registry that describes which entities are backed by sharded tables
 * and which logical fields are considered sharding keys for those entities.
 *
 * For now this is a simple in-memory registry that can be configured programmatically.
 * In the future this could be backed by annotations or external configuration.
 */
public final class ShardingMeta {

    private final Map<Class<?>, Set<String>> entityToShardingKeys;

    private ShardingMeta(Map<Class<?>, Set<String>> mapping) {
        this.entityToShardingKeys = mapping;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns true if the given entity class is considered sharded.
     */
    public boolean isShardedEntity(Class<?> entityClass) {
        return entityToShardingKeys.containsKey(entityClass);
    }

    /**
     * Returns the required sharding key field names for the given entity.
     */
    public Set<String> getShardingKeys(Class<?> entityClass) {
        return entityToShardingKeys.getOrDefault(entityClass, Collections.emptySet());
    }

    public static final class Builder {
        private final Map<Class<?>, Set<String>> mapping = new HashMap<>();

        /**
         * Register an entity as sharded with the given logical field names as sharding keys.
         *
         * Example:
         *   register(UserCoupon.class, "tenantId", "userId");
         */
        public Builder register(Class<?> entityClass, String... shardingKeys) {
            Objects.requireNonNull(entityClass, "entityClass must not be null");
            Objects.requireNonNull(shardingKeys, "shardingKeys must not be null");
            Set<String> keys = new HashSet<>();
            for (String key : shardingKeys) {
                if (key != null && !key.isBlank()) {
                    keys.add(key);
                }
            }
            mapping.put(entityClass, Collections.unmodifiableSet(keys));
            return this;
        }

        public ShardingMeta build() {
            return new ShardingMeta(Collections.unmodifiableMap(new HashMap<>(mapping)));
        }
    }
}

