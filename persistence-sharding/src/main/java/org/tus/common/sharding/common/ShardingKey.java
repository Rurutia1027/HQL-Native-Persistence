package org.tus.common.sharding.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field on an entity as part of the logical sharding key.
 * <p>
 * Example:
 * <pre>
 *     public class User extends ShardedPersistedObject {
 *          @ShardingKey
 *          private String userId
 *     }
 * </pre>
 * <p>
 * Field names annotated wth {@link ShardingKey} will be picked up
 * by {@link AnnotationBasedShardingMetaFactory} when building ShardingMeta.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ShardingKey {
}
