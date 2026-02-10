package org.tus.common.sharding.integration;

import org.junit.jupiter.api.Test;
import org.tus.common.sharding.algorithm.DBHashModShardingAlgorithm;
import org.tus.common.sharding.algorithm.TableHashModShardingAlgorithm;
import org.tus.common.sharding.util.ShardingUtil;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for sharding algorithms.
 * Tests that sharding algorithms correctly calculate shard locations.
 */
public class ShardingAlgorithmIT {
    
    @Test
    void testDBHashModShardingAlgorithm() {
        DBHashModShardingAlgorithm algorithm = new DBHashModShardingAlgorithm();
        
        Properties props = new Properties();
        props.setProperty("sharding-count", "32");
        algorithm.init(props);
        
        // Test sharding mod calculation
        long userId1 = 1001L;
        long userId2 = 2001L;
        
        int mod1 = algorithm.getShardingMod(userId1, 2); // 2 databases
        int mod2 = algorithm.getShardingMod(userId2, 2);
        
        assertTrue(mod1 >= 0 && mod1 < 2, "Database mod should be between 0 and 1");
        assertTrue(mod2 >= 0 && mod2 < 2, "Database mod should be between 0 and 1");
        
        // Same user ID should route to same database
        int mod1Again = algorithm.getShardingMod(userId1, 2);
        assertEquals(mod1, mod1Again, "Same user ID should route to same database");
    }
    
    @Test
    void testTableHashModShardingAlgorithm() {
        TableHashModShardingAlgorithm algorithm = new TableHashModShardingAlgorithm();
        
        Properties props = new Properties();
        algorithm.init(props);
        
        // Test that algorithm is initialized
        assertNotNull(algorithm);
        assertEquals("TABLE_HASH_MOD", algorithm.getType());
    }
    
    @Test
    void testShardingUtil() {
        long userId = 1001L;
        int shardingCount = 32;
        int databaseCount = 2;
        int tableCount = 16;
        
        int dbIndex = ShardingUtil.calculateDatabaseShard(userId, shardingCount, databaseCount);
        int tableIndex = ShardingUtil.calculateTableShard(userId, tableCount);
        
        assertTrue(dbIndex >= 0 && dbIndex < databaseCount, 
                  "Database index should be between 0 and " + (databaseCount - 1));
        assertTrue(tableIndex >= 0 && tableIndex < tableCount,
                  "Table index should be between 0 and " + (tableCount - 1));
        
        // Same user ID should route to same shard
        int dbIndexAgain = ShardingUtil.calculateDatabaseShard(userId, shardingCount, databaseCount);
        int tableIndexAgain = ShardingUtil.calculateTableShard(userId, tableCount);
        
        assertEquals(dbIndex, dbIndexAgain, "Same user ID should route to same database");
        assertEquals(tableIndex, tableIndexAgain, "Same user ID should route to same table");
    }
    
    @Test
    void testShardingDistribution() {
        // Test that sharding distributes users across shards
        int shardingCount = 32;
        int databaseCount = 2;
        
        int[] dbDistribution = new int[databaseCount];
        
        // Test 100 different user IDs
        for (long userId = 1000; userId < 1100; userId++) {
            int dbIndex = ShardingUtil.calculateDatabaseShard(userId, shardingCount, databaseCount);
            dbDistribution[dbIndex]++;
        }
        
        // Both databases should have some users (distribution should be roughly even)
        assertTrue(dbDistribution[0] > 0, "Database 0 should have some users");
        assertTrue(dbDistribution[1] > 0, "Database 1 should have some users");
        
        // Distribution should be roughly balanced (within 20% difference)
        int diff = Math.abs(dbDistribution[0] - dbDistribution[1]);
        int total = dbDistribution[0] + dbDistribution[1];
        double ratio = (double) diff / total;
        
        assertTrue(ratio < 0.2, 
                  "Sharding should distribute users roughly evenly. Ratio: " + ratio);
    }
}
