package org.tus.common.sharding.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity class as backed by a sharded table in TiDB.
 * <p>
 * The {@link #shardingKeys()} values are logical Java field names (e.g., tenantId, userId)
 * which should correspond to the partition/sharding keys used in the TiDB DDL.
 * <p>
 * This annotation lives in the sharding module so that {@code persistence-common} can be
 * used independently without any compile-time dependency on sharding concerns.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ShardedEntity {
    /**
     * Logical field names that act as sharding keys for this entity.
     * <p>
     * Example
     * {@code @ShardedEntity(shardingKeys = {"tenantId", "userId"})}
     */
    String[] shardingKeys();
}
