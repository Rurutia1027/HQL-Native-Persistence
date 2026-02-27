package org.tus.common.domain.persistence.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.tus.common.domain.persistence.QueryService;
import org.tus.common.domain.persistence.integration.config.PersistenceTestContainerMySQLConfig;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests that run against MySQL (Testcontainers).
 * Run with: mvn test -Dgroups=mysql
 */
@Tag("mysql")
@SpringJUnitConfig(classes = PersistenceTestContainerMySQLConfig.class)
public class QueryServiceMySQLIT {

    @Autowired
    private QueryService queryService;

    @Test
    void queryOverMySQL() {
        String hql = "FROM TestPersistedEntity";
        List<?> results = queryService.query(hql, Map.of());
        assertNotNull(results);
    }
}
