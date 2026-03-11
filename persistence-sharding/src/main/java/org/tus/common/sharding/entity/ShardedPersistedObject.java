package org.tus.common.sharding.entity;

import org.tus.common.domain.persistence.SimplePersistedObject;

/**
 * Optional base class for entities that participate in sharding.
 * <p>
 * This lives in the sharding module and extends the generic SimplePersistedObject
 * from persistence-common, so persistence-common itself does not depend on sharding.
 * <p>
 * Usage:
 * <pre>
 * ShardedEntity
 * public class User extends SharedPersistedObject {
 *    @ShardingKey
 *    private String userId;
 * }
 * </pre>
 */
public abstract class ShardedPersistedObject extends SimplePersistedObject {
}
