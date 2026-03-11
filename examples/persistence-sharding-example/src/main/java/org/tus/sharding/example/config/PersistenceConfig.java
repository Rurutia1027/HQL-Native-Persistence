package org.tus.sharding.example.config;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.tus.common.domain.persistence.PersistenceService;
import org.tus.common.domain.persistence.QueryService;
import org.tus.common.sharding.common.AnnotationBasedShardingMetaFactory;
import org.tus.common.sharding.common.ShardingMeta;
import org.tus.common.sharding.config.TiDBShardingSpringConfig;
import org.tus.sharding.example.entity.CouponSettlement;
import org.tus.sharding.example.entity.CouponTemplate;
import org.tus.sharding.example.entity.UserCoupon;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@Import(TiDBShardingSpringConfig.class)
public class PersistenceConfig {
    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("org.tus.sharding.example.entity");
        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.format_sql", "true");
        factory.setHibernateProperties(props);
        return factory;
    }

    @Bean
    public QueryService queryService(SessionFactory sessionFactory) {
        return new PersistenceService(sessionFactory);
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public ShardingMeta shardingMeta() {
        return AnnotationBasedShardingMetaFactory.fromEntities(
                List.of(UserCoupon.class, CouponSettlement.class, CouponTemplate.class));
    }
}
