package org.tus.common.sharding.common;

import org.tus.common.sharding.entity.ShardedEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper to build {@link ShardingMeta} from entity classes using annotations:
 * - {@link ShardedEntity} on the type (optional)
 * - {@link ShardingKey} on fields
 * <p>
 * This keeps sharding metadata close to the entity declarations while still
 * allowing {@code persistence-common} to be used on its own.
 */
public final class AnnotationBasedShardingMetaFactory {

    private AnnotationBasedShardingMetaFactory() {
        // utility
    }

    /**
     * Scans the provided entity classes for {@link ShardedEntity} and {@link ShardingKey}
     * and builds a {@link ShardingMeta} mapping entity classes to their declared sharding keys.
     */
    public static ShardingMeta fromEntities(Collection<Class<?>> entityClasses) {
        ShardingMeta.Builder builder = ShardingMeta.builder();
        if (entityClasses == null) {
            return builder.build();
        }

        for (Class<?> entityClass : entityClasses) {
            if (entityClass == null) {
                continue;
            }

            // Prefer explicit shardingKeys() on the type if present
            ShardedEntity shardedAnn = entityClass.getAnnotation(ShardedEntity.class);
            if (shardedAnn != null && shardedAnn.shardingKeys().length > 0) {
                builder.register(entityClass, shardedAnn.shardingKeys());
                continue;
            }

            // Otherwise, derive keys from fields annotated with @ShardingKey
            List<String> fieldKeys = new ArrayList<>();
            Class<?> current = entityClass;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    if (field.isAnnotationPresent(ShardingKey.class)) {
                        fieldKeys.add(field.getName());
                    }
                }
                current = current.getSuperclass();
            }

            if (!fieldKeys.isEmpty()) {
                builder.register(entityClass, fieldKeys.toArray(new String[0]));
            }
        }
        return builder.build();
    }
}


