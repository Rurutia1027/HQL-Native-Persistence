package org.tus.common.sharding.entity;

import org.tus.common.domain.persistence.PersistedObject;

/**
 * Optional base class for entities that participate in sharding.
 *
 * Extends PersistedObject so they work with QueryService and ShardingAwareQueryService.
 *
 * Usage:
 * <pre>
 * &#064;ShardedEntity
 * &#064;AttributeOverride(name = "id", column = &#064;Column(name = "id"))
 * public class UserCoupon extends ShardedPersistedObject {
 *     &#064;ShardingKey
 *     private Long userId;
 * }
 * </pre>
 */
public abstract class ShardedPersistedObject extends PersistedObject {
}

