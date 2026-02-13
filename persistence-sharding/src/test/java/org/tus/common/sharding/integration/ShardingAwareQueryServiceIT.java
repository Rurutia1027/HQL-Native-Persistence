package org.tus.common.sharding.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.tus.common.domain.persistence.QueryService;
import org.tus.common.sharding.config.ShardingTestContainerDBConfig;
import org.tus.common.sharding.entity.TestShardedEntity;
import org.tus.common.sharding.service.ShardingAwareQueryService;
import org.tus.common.sharding.service.ShardingAwareQueryServiceImpl;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ShardingAwareQueryService.
 * Tests sharding-aware query operations.
 */
@Slf4j
@SpringJUnitConfig(classes = ShardingTestContainerDBConfig.class)
public class ShardingAwareQueryServiceIT {
    
    @Autowired
    private QueryService queryService;
    
    private ShardingAwareQueryService shardingAwareQueryService;
    
    private Long testUserId1 = 1001L;
    private Long testUserId2 = 2001L;
    
    @BeforeEach
    void setUp() {
        shardingAwareQueryService = new ShardingAwareQueryServiceImpl(queryService);
        cleanupTestData();
    }
    
    @Test
    void testFindObjectByIdWithShardingKey() {
        // Create entity
        TestShardedEntity entity = createTestEntity(testUserId1, "Test Entity");
        
        // Find using sharding-aware method
        TestShardedEntity found = shardingAwareQueryService.findObjectByIdWithShardingKey(
            TestShardedEntity.class, 
            entity.getId(), 
            testUserId1
        );
        
        assertNotNull(found);
        assertEquals(entity.getId(), found.getId());
        assertEquals(testUserId1, found.getUserId());
    }
    
    @Test
    void testQueryWithShardingKeys() {
        // Create entities
        createTestEntities();
        
        // Query with sharding keys
        String hql = "FROM TestShardedEntity WHERE userId IN (:shardingKeys)";
        Map<String, Object> params = Map.of("shardingKeys", List.of(testUserId1, testUserId2));
        
        List<TestShardedEntity> results = shardingAwareQueryService.queryWithShardingKeys(
            hql, 
            params, 
            List.of(testUserId1, testUserId2)
        );
        
        assertNotNull(results);
        assertTrue(results.size() >= 2, "Should find entities across shards");
    }
    
    @Test
    void testGetShardInfo() {
        long userId = 1001L;
        int shardingCount = 32;
        int databaseCount = 2;
        int tableCount = 16;

        ShardingAwareQueryService.ShardInfo shardInfo = shardingAwareQueryService.getShardInfo(
            userId,
            shardingCount,
            databaseCount,
            tableCount
        );

        assertNotNull(shardInfo);
        assertTrue(shardInfo.getDatabaseIndex() >= 0 && shardInfo.getDatabaseIndex() < databaseCount);
        assertTrue(shardInfo.getTableIndex() >= 0 && shardInfo.getTableIndex() < tableCount);
        assertEquals("ds_" + shardInfo.getDatabaseIndex(), shardInfo.getDatabaseName());
    }

    @Test
    void testGetShardInfoWithLogicalTableName() {
        long userId = 1001L;
        int shardingCount = 32;
        int databaseCount = 2;
        int tableCount = 16;
        String logicalTableName = "t_order";

        ShardingAwareQueryService.ShardInfo shardInfo = shardingAwareQueryService.getShardInfo(
            userId, shardingCount, databaseCount, tableCount, logicalTableName
        );

        assertNotNull(shardInfo);
        assertTrue(shardInfo.getTableName().startsWith(logicalTableName + "_"));
        assertEquals(shardInfo.getTableIndex(), Integer.parseInt(shardInfo.getTableName().substring(logicalTableName.length() + 1)));
    }

    @Test
    void testFindObjectByIdWithShardingKeyWithColumnName() {
        TestShardedEntity entity = createTestEntity(testUserId1, "Test Entity Column");

        TestShardedEntity found = shardingAwareQueryService.findObjectByIdWithShardingKey(
            TestShardedEntity.class,
            entity.getId(),
            "userId",
            testUserId1
        );

        assertNotNull(found);
        assertEquals(entity.getId(), found.getId());
        assertEquals(testUserId1, found.getUserId());
    }
    
    @Test
    void testShardingAwareQueryServiceDelegatesToQueryService() {
        // Create entity
        TestShardedEntity entity = createTestEntity(testUserId1, "Test Entity");
        
        // Test that ShardingAwareQueryService delegates correctly
        TestShardedEntity found = shardingAwareQueryService.findObjectById(
            TestShardedEntity.class, 
            entity.getId()
        );
        
        assertNotNull(found);
        assertEquals(entity.getId(), found.getId());
    }
    
    // Helper methods
    
    private void cleanupTestData() {
        String hql = "FROM TestShardedEntity";
        List<TestShardedEntity> entities = queryService.query(hql, Map.of());
        for (TestShardedEntity entity : entities) {
            queryService.delete(entity);
        }
    }
    
    private void createTestEntities() {
        createTestEntity(testUserId1, "User1 Entity 1");
        createTestEntity(testUserId1, "User1 Entity 2");
        createTestEntity(testUserId2, "User2 Entity 1");
    }
    
    private TestShardedEntity createTestEntity(Long userId, String name) {
        TestShardedEntity entity = new TestShardedEntity();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setValue("Value for " + name);
        entity.setStatus(1);
        return queryService.save(entity);
    }
}
