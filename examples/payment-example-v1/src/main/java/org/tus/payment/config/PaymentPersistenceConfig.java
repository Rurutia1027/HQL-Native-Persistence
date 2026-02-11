package org.tus.payment.config;

import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.tus.common.domain.persistence.PersistenceService;
import org.tus.common.domain.persistence.QueryService;

import javax.sql.DataSource;
import java.util.Properties;

/***
 * Payment Persistence Configuration
 * Configures Hibernate SessionFactory and PersistenceService config options load from
 * application.yml
 */
@Configuration
@EnableTransactionManagement
public class PaymentPersistenceConfig {
    @Value("${app.hibernate.packages-to-scan:org.tus.payment.entity}")
    private String packagesToScan;

    @Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}")
    private String hibernateDialect;

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String hbm2ddlAuto;

    @Value("${app.hibernate.show-sql:false}")
    private String showSql;

    @Value("${app.hibernate.format-sql:true}")
    private String formatSql;

    /**
     * DataSource configuration from application.yml
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        return dataSource;
    }

    /**
     * Hibernate SessionFactory configuration
     */
    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan(packagesToScan.split(","));

        Properties props = new Properties();
        props.put("hibernate.dialect", hibernateDialect);
        props.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        props.put("hibernate.show_sql", showSql);
        props.put("hibernate.format_sql", formatSql);
        props.put("hibernate.use_sql_comments", "true");
        props.put("hibernate.jdbc.batch_size", "50");
        props.put("hibernate.order_inserts", "true");
        props.put("hibernate.order_updates", "true");
        sessionFactory.setHibernateProperties(props);

        return sessionFactory;
    }

    /**
     * QueryService bean (alias for PersistenceService)
     */
    @Bean
    public QueryService queryService(SessionFactory sessionFactory) {
        PersistenceService persistenceService = new PersistenceService(sessionFactory);
        return persistenceService;
    }

    /**
     * Transaction manager
     */
    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }
}
