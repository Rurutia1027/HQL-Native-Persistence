package org.tus.common.sharding.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable context object carrying sharding keys for the current request.
 * <p>
 * This is intentionally simple - it does not know anything about entities or tables,
 * t just transports key/value pairs like tenantId, userId, shopNumber, etc.
 */
public final class ShardingContext {
    private final Map<String, Object> keys;

    private ShardingContext(Map<String, Object> keys) {
        this.keys = Collections.unmodifiableMap(new HashMap<>(keys));
    }

    public static ShardingContext of(Map<String, Object> keys) {
        Objects.requireNonNull(keys, "keys must not be null");
        return new ShardingContext(keys);
    }

    public static ShardingContext empty() {
        return new ShardingContext(Collections.emptyMap());
    }

    public Map<String, Object> getKeys() {
        return keys;
    }

    public Object getKey(String name) {
        return keys.get(name);
    }

    public boolean hasKey(String name) {
        return keys.containsKey(name);
    }
}
