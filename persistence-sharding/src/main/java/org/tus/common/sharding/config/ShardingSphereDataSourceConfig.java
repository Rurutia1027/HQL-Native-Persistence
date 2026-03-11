package org.tus.common.sharding.config;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.tus.common.domain.persistence.PersistenceService;
import org.tus.common.domain.persistence.QueryService;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;

/**
 * Configuration for ShardingSphere data source integration with Hibernate.
 * 
 * This configuration creates a ShardingSphere data source that handles
 * database and table sharding transparently. Hibernate works on top of
 * this data source without knowing about the sharding.
 * 
 * The sharding rules are defined in shardingsphere-config.yaml, but can
 * also be configured programmatically here.
 */
@Configuration
public class ShardingSphereDataSourceConfig {

    @Value("${shardingsphere.config.file:classpath:shardingsphere-config.yaml}")
    private String configFile;
    
    private final ResourceLoader resourceLoader;

    public ShardingSphereDataSourceConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Creates a ShardingSphere data source from configuration file.
     * This is the primary data source that handles all sharding logic.
     * 
     * Uses Spring Resource API for elegant resource loading.
     */
    @Bean
    @Primary
    public DataSource shardingSphereDataSource() throws SQLException, IOException {
        // Use Spring Resource API to load configuration file
        // Supports classpath:, file:, http:, etc.
        Resource resource = resourceLoader.getResource(configFile);
        
        if (!resource.exists()) {
            throw new IllegalStateException("ShardingSphere config file not found: " + configFile);
        }
        
        // If resource is a file, use File directly (most efficient)
        if (resource.isFile() && resource.getFile().exists()) {
            File configFile = resource.getFile();
            return YamlShardingSphereDataSourceFactory.createDataSource(configFile);
        }
        
        // Otherwise, read as byte array (for classpath resources, etc.)
        // Use Files API for elegant byte array conversion
        try (java.io.InputStream inputStream = resource.getInputStream()) {
            // Java 9+ readAllBytes() - elegant and efficient
            byte[] yamlBytes = inputStream.readAllBytes();
            return YamlShardingSphereDataSourceFactory.createDataSource(yamlBytes);
        }
    }

    /**
     * Alternative: Programmatic configuration (if not using YAML).
     * Uncomment and modify if you prefer code-based configuration.
     */
    /*
    @Bean
    @Primary
    public DataSource shardingSphereDataSourceProgrammatic() throws SQLException {
        // Define actual data sources
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", createDataSource("jdbc:mysql://localhost:3306/db_0"));
        dataSourceMap.put("ds_1", createDataSource("jdbc:mysql://localhost:3306/db_1"));

        // Configure sharding rules
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        
        // Configure table sharding rules
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration(
            "t_user_coupon",
            "ds_${0..1}.t_user_coupon_${0..31}"
        );
        tableRuleConfig.setDatabaseShardingStrategy(
            new StandardShardingStrategyConfiguration("user_id", "db_hash_mod")
        );
        tableRuleConfig.setTableShardingStrategy(
            new StandardShardingStrategyConfiguration("user_id", "table_hash_mod")
        );
        shardingRuleConfig.getTables().add(tableRuleConfig);

        // Configure sharding algorithms
        shardingRuleConfig.getShardingAlgorithms().put(
            "db_hash_mod",
            new AlgorithmConfiguration("CLASS_BASED", createDBAlgorithmProps(32))
        );
        shardingRuleConfig.getShardingAlgorithms().put(
            "table_hash_mod",
            new AlgorithmConfiguration("CLASS_BASED", createTableAlgorithmProps())
        );

        Properties props = new Properties();
        props.setProperty("sql-show", "true");

        // Note: For programmatic configuration, use ShardingSphereDataSourceFactory
        // This requires additional imports and configuration
        // Uncomment and add necessary imports if needed
        throw new UnsupportedOperationException("Programmatic configuration not implemented. Use YAML configuration instead.");
    }
    */

    /**
     * Creates Hibernate SessionFactory on top of ShardingSphere data source.
     * Hibernate will work with the sharded data source transparently.
     * 
     * Hibernate properties can be overridden via application.yml:
     * - hibernate.dialect: Database dialect (default: MySQL)
     * - hibernate.hbm2ddl.auto: Schema update mode (default: none)
     * - hibernate.show_sql: Show SQL (default: false)
     * - hibernate.format_sql: Format SQL (default: true)
     * - hibernate.packagesToScan: Entity package scan (default: empty, must be set)
     * 
     * Example application.yml (TiDB/MySQL style):
     * ```yaml
     * hibernate:
     *   dialect: org.hibernate.dialect.MySQLDialect
     *   hbm2ddl.auto: update
     *   show_sql: true
     *   format_sql: true
     *   packagesToScan: com.example.entity
     * ```
     */
    @Bean
    public LocalSessionFactoryBean sessionFactory(
            DataSource shardingSphereDataSource,
            @Value("${hibernate.dialect:org.hibernate.dialect.MySQLDialect}") String dialect,
            @Value("${hibernate.hbm2ddl.auto:none}") String hbm2ddlAuto,
            @Value("${hibernate.show_sql:false}") boolean showSql,
            @Value("${hibernate.format_sql:true}") boolean formatSql,
            @Value("${hibernate.packagesToScan:}") String packagesToScan) {
        
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(shardingSphereDataSource);
        
        // Set packages to scan - must be configured in application.yml
        if (packagesToScan != null && !packagesToScan.trim().isEmpty()) {
            factory.setPackagesToScan(packagesToScan.split(","));
        } else {
            // Fallback: scan common persistence entity packages
            factory.setPackagesToScan("org.tus.common.domain.persistence.entity");
        }
        
        // Build Hibernate properties with defaults that can be overridden
        Properties props = new Properties();
        props.put("hibernate.dialect", dialect);
        props.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        props.put("hibernate.show_sql", String.valueOf(showSql));
        props.put("hibernate.format_sql", String.valueOf(formatSql));
        
        // Additional useful defaults
        props.put("hibernate.use_sql_comments", "false");
        props.put("hibernate.jdbc.batch_size", "20");
        props.put("hibernate.order_inserts", "true");
        props.put("hibernate.order_updates", "true");
        
        factory.setHibernateProperties(props);
        return factory;
    }

    /**
     * Creates QueryService that works with sharded data source.
     */
    @Bean
    public QueryService queryService(SessionFactory sessionFactory) {
        return new PersistenceService(sessionFactory);
    }

    /**
     * Transaction manager for sharded data source.
     */
    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    /**
     * Helper method to create properties for DB sharding algorithm.
     */
    private Properties createDBAlgorithmProps(int shardingCount) {
        Properties props = new Properties();
        props.setProperty("sharding-count", String.valueOf(shardingCount));
        props.setProperty("algorithmClassName", 
            "org.tus.common.sharding.algorithm.DBHashModShardingAlgorithm");
        return props;
    }

    /**
     * Helper method to create properties for table sharding algorithm.
     */
    private Properties createTableAlgorithmProps() {
        Properties props = new Properties();
        props.setProperty("algorithmClassName", 
            "org.tus.common.sharding.algorithm.TableHashModShardingAlgorithm");
        return props;
    }
}
