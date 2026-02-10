# Persistence Sharding Model 
Database sharding support for the persistence-common module using Apache ShardingSphere. This module horizontal partitioning of data across multiple databases and tables while maintaining full compatibility with Hibernate and HQL queries. 

## Features 
- **Transparent Sharding**: ShardingSphere handles routing automatically 
- **Hibernate/HQL Compatible**: Works seamlessly with existing HQL queries 
- **Custom Sharding Algorithms**: Hash-based modulo sharding for databases and tables 
- **Type-Safe Queries**: Use HqlQueryBuilder for type-safe query construction 
- **Cross-Shard Support**: Automatic handling of queries spanning multiple shards 
- **No MyBatis Required**: Pure Hibernate/HQL solution 

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>org.tus.common</groupId>
    <artifactId>persistence-sharding</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. Configure ShardingSphere

Create `src/main/resources/shardingsphere-config.yaml`:

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/db_0
    username: root
    password: root
  ds_1:
    # ... similar configuration

rules:
  - !SHARDING
    tables:
      t_user_coupon:
        actualDataNodes: ds_${0..1}.t_user_coupon_${0..31}
        databaseStrategy:
          standard:
            shardingColumn: user_id
            shardingAlgorithmName: user_coupon_database_mod
        tableStrategy:
          standard:
            shardingColumn: user_id
            shardingAlgorithmName: user_coupon_table_mod
    shardingAlgorithms:
      user_coupon_database_mod:
        type: CLASS_BASED
        props:
          algorithmClassName: org.tus.common.sharding.algorithm.DBHashModShardingAlgorithm
          sharding-count: 32
      user_coupon_table_mod:
        type: CLASS_BASED
        props:
          algorithmClassName: org.tus.common.sharding.algorithm.TableHashModShardingAlgorithm
```

### 3. Configure Spring Beans


```java
@Configuration
public class ShardingConfig {
    
    @Bean
    @Primary
    public DataSource shardingSphereDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(
            getClass().getClassLoader()
                .getResourceAsStream("shardingsphere-config.yaml")
        );
    }
    
    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(dataSource);
        // entity path in the project that use persistence-sharding 
        factory.setPackagesToScan("com.example.entity");
        // ... Hibernate configuration
        return factory;
    }
    
    @Bean
    public QueryService queryService(SessionFactory sessionFactory) {
        return new PersistenceService(sessionFactory);
    }
}
```


### 4. Use with HQL

```java
@Service
public class UserCouponService {
    
    @Autowired
    private QueryService queryService;
    
    // ShardingSphere automatically routes based on user_id
    public List<UserCoupon> findByUserId(Long userId) {
        String hql = "from UserCoupon where userId = :userId";
        return queryService.query(hql, Map.of("userId", userId));
    }
}
```

## Architecture

```
Application Layer
    ↓
QueryService (Hibernate/HQL)
    ↓
ShardingSphere DataSource (Transparent Routing)
    ↓
Physical Databases (ds_0, ds_1, ...)
    ↓
Sharded Tables (t_user_coupon_0, t_user_coupon_1, ...)
```

## Key Components

### Sharding Algorithms

- **DBHashModShardingAlgorithm**: Distributes data across databases
- **TableHashModShardingAlgorithm**: Distributes data across tables


### Services

- **ShardingAwareQueryService**: Extended QueryService with sharding utilities
- **ShardingUtil**: Helper methods for shard calculations


## Example: Using HqlQueryBuilder with Sharding 

```java
HqlQueryBuilder builder = new HqlQueryBuilder(); 
builder.from(UserCoupon.class, "uc")
       .leftJoin(CouponTemplate.class, "ct", "uc.couponTemplateId", "ct.id")
       .eq("uc.userId", userId)
       .eq("uc.status", "ACTIVE")
       .orderBy("uc.createdDate", false);

String hql = builder.build();
Map<String, Object> params = builder.getInjectionParameters();
List<UserCoupon> coupons = queryService.query(hql, params);
// ShardingSphere automatically routes to correct shard(s)       
```

## Benefits Over MyBatis Approach 
1. **Type Safety**: Compile-time validation of queries 
2. **Transparent Sharding**: No manual shard routing needed 
3. **Less Boilerplate**: Automatic entity mapping 
4. **Better IDE Support**: Autocomplete and refactoring 
5. **Automatic Cross-Shard Handling**: ShardingSphere merges results 


## Requirements

- Java 17+
- Apache ShardingSphere 5.4.1+
- Hibernate 6.x
- MySQL 8.0+ (or other supported databases)

## License
[LICENSE](../LICENSE)