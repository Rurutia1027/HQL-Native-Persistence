package org.tus.common.sharding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tus.common.domain.persistence.PersistedObject;

/**
 * Test entity for sharding integration tests. 
 * This entity will be sharded by userId. 
*/
@Entity 
@Table(name = "t_test_sharded")
@Data
@EqualsAndHashCode(callSuper = true)
public class TestShardedEntity extends PersistedObject {
    @Column(name = "user_id", nullable = false)
    private Long userId; // Sharding key
    
    @Column(name = "name", nullable = false, length = 128)
    private String name;
    
    @Column(name = "value", length = 256)
    private String value;
    
    @Column(name = "status")
    private Integer status;
}