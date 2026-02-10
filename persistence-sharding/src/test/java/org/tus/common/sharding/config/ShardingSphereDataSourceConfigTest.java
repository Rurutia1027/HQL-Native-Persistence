package org.tus.common.sharding.config;

import org.junit.jupiter.api.Test;
import org.tus.common.sharding.algorithm.DBHashModShardingAlgorithm;
import org.tus.common.sharding.algorithm.TableHashModShardingAlgorithm;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for ShardingSphereDataSourceConfig helper methods. 
*/
public class ShardingSphereDataSourceConfigTest {
    @Test 
    void testDBHashModShardingAlgorithmInitializaiton() {
        DBHashModShardingAlgorithm algorithm = new DBHashModShardingAlgorithm(); 

        Properties props = new Properties();
        props.setProperty("sharding-count", "32");
        
        assertDoesNotThrow(() -> algorithm.init(props));
        assertEquals("DB_HASH_MOD", algorithm.getType());
    }

    @Test
    void testTableHashModShardingAlgorithmInitialization() {
        TableHashModShardingAlgorithm algorithm = new TableHashModShardingAlgorithm();
        
        Properties props = new Properties();
        
        assertDoesNotThrow(() -> algorithm.init(props));
        assertEquals("TABLE_HASH_MOD", algorithm.getType());
    }

    @Test
    void testDBHashModShardingAlgorithmWithInvalidConfig() {
        DBHashModShardingAlgorithm algorithm = new DBHashModShardingAlgorithm();
        
        Properties props = new Properties();
        // Missing sharding-count
        
        assertThrows(Exception.class, () -> algorithm.init(props));
    }
}