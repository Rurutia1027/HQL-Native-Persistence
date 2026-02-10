package org.tus.common.sharding.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.tus.common.domain.dao.HqlQueryBuilder;
import org.tus.common.domain.persistence.QueryService;
import org.tus.common.sharding.config.ShardingTestContainerDBConfig;
import org.tus.common.sharding.entity.TestShardedEntity;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ShardingSphere DataSource configuration.
 * Tests that ShardingSphere data source is properly configured and can route queries.
 */
@Slf4j
@SpringJUnitConfig(classes = ShardingTestContainerDBConfig.class)
public class ShardingSphereDataSourceConfigIT {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private QueryService queryService;
    
    private Long testUserId1 = 1001L;
    private Long testUserId2 = 2001L;
    
    @BeforeEach
    void setUp() {
        cleanupTestData();
    }
    
    @Test
    void testDataSourceIsConfigured() {
        assertNotNull(dataSource, "DataSource should be configured");
        assertNotNull(queryService, "QueryService should be configured");
    }
    
    @Test
    void testSaveAndQueryEntity() {
        // Create and save entity
        TestShardedEntity entity = new TestShardedEntity();
        entity.setUserId(testUserId1);
        entity.setName("Test Entity 1");
        entity.setValue("Test Value 1");
        entity.setStatus(1);
        
        TestShardedEntity saved = queryService.save(entity);
        
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(testUserId1, saved.getUserId());
        assertEquals("Test Entity 1", saved.getName());
    }
    
    @Test
    void testQueryByUserId() {
        // Create multiple entities with different user IDs
        createTestEntities();
        
        // Query entities for user 1
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(TestShardedEntity.class, "e")
               .select("e")
               .eq("e.userId", testUserId1)
               .and()
               .isNull("e.deleted");
        
        String hql = builder.build();
        System.err.println("DEBUG Generated HQL: [" + hql + "]");
        System.err.println("DEBUG HQL length: " + hql.length());
        System.err.println("DEBUG HQL contains 'and': " + hql.contains("and"));
        System.err.println("DEBUG HQL contains 'AND': " + hql.contains("AND"));
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();
        
        List<TestShardedEntity> results = queryService.query(hql, params);
        
        assertNotNull(results);
        assertEquals(2, results.size(), "Should find 2 entities for user 1");
        results.forEach(entity -> assertEquals(testUserId1, entity.getUserId()));
    }
    
    @Test
    void testQueryByStatus() {
        // Create test entities
        createTestEntities();
        
        // Query by status (cross-shard query)
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(TestShardedEntity.class, "e")
               .select("e")
               .eq("e.status", 1)
               .and()
               .isNull("e.deleted");
        
        String hql = builder.build();
        System.err.println("DEBUG Generated HQL: [" + hql + "]");
        System.err.println("DEBUG HQL contains 'and': " + hql.contains("and"));
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();
        
        List<TestShardedEntity> results = queryService.query(hql, params);
        
        assertNotNull(results);
        assertTrue(results.size() >= 2, "Should find entities across shards");
    }
    
    @Test
    void testUpdateEntity() {
        // Create entity
        TestShardedEntity entity = createTestEntity(testUserId1, "Original Name");
        
        // Update entity
        entity.setName("Updated Name");
        entity.setValue("Updated Value");
        TestShardedEntity updated = queryService.save(entity);
        
        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated Value", updated.getValue());
    }
    
    @Test
    void testDeleteEntity() {
        // Create entity
        TestShardedEntity entity = createTestEntity(testUserId1, "To Delete");
        
        // Delete entity
        queryService.delete(entity);
        
        // Verify deleted
        TestShardedEntity found = queryService.findObjectById(TestShardedEntity.class, entity.getId());
        assertNull(found, "Entity should be deleted");
    }
    
    @Test
    void testSoftDelete() {
        // Create entity
        TestShardedEntity entity = createTestEntity(testUserId1, "To Soft Delete");
        
        // Soft delete
        entity.markDeleted();
        queryService.save(entity);
        
        // Query should not return soft-deleted entity
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.fromAs(TestShardedEntity.class, "e")
               .select("e")
               .eq("e.userId", testUserId1)
               .and()
               .isNull("e.deleted");
        
        String hql = builder.build();
        System.err.println("DEBUG Generated HQL: [" + hql + "]");
        System.err.println("DEBUG HQL contains 'and': " + hql.contains("and"));
        Map<String, Object> params = builder.getInjectionParameters();
        builder.clear();
        
        List<TestShardedEntity> results = queryService.query(hql, params);
        
        assertTrue(results.stream().noneMatch(e -> e.getId().equals(entity.getId())),
                   "Soft-deleted entity should not be returned");
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
        createTestEntity(testUserId2, "User2 Entity 2");
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
