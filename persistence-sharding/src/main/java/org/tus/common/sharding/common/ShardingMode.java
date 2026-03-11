package org.tus.common.sharding.common;

/**
 * Enforcement mode for sharding constraints.
 * <p>
 * STRICT  - enforce presence of sharding keys for sharded entities, fail fast on violations.
 * OBSERVE - do not block queries, only record violations via logs/metrics (to be wired later).
 */
public enum ShardingMode {
    STRICT,
    OBSERVE
}

