package org.tus.payment.config;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.tus.common.domain.persistence.PersistenceService;
import org.tus.common.domain.persistence.QueryService;
import org.tus.common.sharding.service.ShardingAwareQueryService;
import org.tus.common.sharding.service.ShardingAwareQueryServiceImpl;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ShardingSphere configuration for Payment Example V2 (PostgreSQL).
 */
@Configuration
@EnableTransactionManagement
public class PaymentShardingConfig {

    @Value("${shardingsphere.config.file:classpath:shardingsphere-config.yaml}")
    private String configFile;

    // PostgreSQL Hibernate dialect
    @Value("${hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}")
    private String hibernateDialect;

    @Value("${hibernate.hbm2ddl.auto:update}")
    private String hbm2ddlAuto;

    @Value("${hibernate.show_sql:false}")
    private String showSql;

    @Value("${hibernate.format_sql:false}")
    private String formatSql;

    @Value("${hibernate.packagesToScan:org.tus.payment.entity}")
    private String packagesToScan;

    /**
     * Creates ShardingSphere data source from YAML configuration.
     */
    @Bean
    public DataSource shardingSphereDataSource(ResourceLoader resourceLoader) throws SQLException, IOException {
        String resourcePath = configFile.replace("classpath:", "");
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }

        org.springframework.core.io.Resource resource = resourceLoader.getResource(configFile);

        if (resource.exists()) {
            if (resource.isFile()) {
                File configFileObj = resource.getFile();
                return YamlShardingSphereDataSourceFactory.createDataSource(configFileObj);
            } else {
                try (java.io.InputStream inputStream = resource.getInputStream()) {
                    byte[] configBytes = inputStream.readAllBytes();
                    return YamlShardingSphereDataSourceFactory.createDataSource(configBytes);
                }
            }
        }

        throw new IllegalStateException("ShardingSphere config file not found: " + configFile);
    }

    /**
     * Creates Hibernate SessionFactory on top of ShardingSphere data source.
     */
    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource shardingSphereDataSource) {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(shardingSphereDataSource);
        factory.setPackagesToScan(packagesToScan.split(","));

        Properties props = new Properties();
        props.put("hibernate.dialect", hibernateDialect); // PostgreSQL dialect
        props.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        props.put("hibernate.show_sql", showSql);
        props.put("hibernate.format_sql", formatSql);
        props.put("hibernate.use_sql_comments", "true");
        props.put("hibernate.jdbc.batch_size", "50");
        props.put("hibernate.order_inserts", "true");
        props.put("hibernate.order_updates", "true");
        factory.setHibernateProperties(props);

        return factory;
    }

    @Bean
    public QueryService queryService(SessionFactory sessionFactory) {
        return new PersistenceService(sessionFactory);
    }

    @Bean
    public ShardingAwareQueryService shardingAwareQueryService(QueryService queryService) {
        return new ShardingAwareQueryServiceImpl(queryService);
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }
}
