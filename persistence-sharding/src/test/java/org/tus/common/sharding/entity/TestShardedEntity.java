package org.tus.common.sharding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import org.tus.common.domain.persistence.PersistedObject;

/**
 * Test entity for sharding integration tests. 
 * This entity will be sharded by userId. 
*/
@Entity 
@Table(name = "t_test_sharded")
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
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
}