# Database Sharding Solution Documentation 

## Overview 
This module provides database sharding capabilities for the persistence-common module using Apache ShardingSphere. It enables horizontal partition of data across multiple databases and tables while mantaining compatibility with Hibernate and HQL queries. 

## Architecture
### Key Components 
- **ShardingSphere Integration**: Uses Apache ShardingSphere JDBC to handle sharding transparently 
- **Custom Sharding Algorithm**: Implements hash-based modulo sharding for both databases and tables 
- **Hibernate Compatibility**: Works seamlessly with Hibernate/HQL without requiring code changes 
- **QueryService Extension**: Provides sharding-aware utilities when needed 

### Sharding Strategy 
The solution use a **hash-based modulo** sharding stratey: 
- **Database Sharding**: Distributes data across multiple databases (e.g., ds_0, ds_1)
- **Table Sharding**: Distributes data across multiple tables within each database (e.g., t_user_coupon_0, t_user_coupon_1, ..., t_user_coupon_31)
- **Sharding Key**: Uses a business key (e.g., `user_id`, `shop_number`) to determine shard location.

### Example Configuration 
```
Database Layout:
├── ds_0 (Database 0)
│   ├── t_user_coupon_0
│   ├── t_user_coupon_1
│   ├── ...
│   └── t_user_coupon_15
└── ds_1 (Database 1)
    ├── t_user_coupon_16
    ├── t_user_coupon_17
    ├── ...
    └── t_user_coupon_31
```

## Sharding Algorithms 
### DBHashModShardingAlgorithm 

Distributes data across databases using hash modulo: 

```java
int dbIndex = hash(user_id) % shardingCount / (shardingCount / databaseCount)
```

**Configuration**
- `sharding-count`: Total number of shards (database x table combinations)
- Used for: Database-level routing 

### TableHashModShardingAlgorithm 
Distributes data across tables using hash modulo: 

```java
int tableIndex = hash(user_id) % tableCount 
```

**Configuration**
- Automatically uses available table count 
- Used for: Table-level routing within a database 

## Usage 
### 1. Configuration Setup 

Create `shardingsphere-config.yaml`: 

```yaml 
dataSources:
  ds_0: 
    dataSourceClassName:  com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver 
    jdbcUrl: jdbc:mysql://localhost:3306/db_0
    username: root 
    password: root 
  ds_1: 
    # ... similar configuraiton 
rules: 
  - !SHARDING 
    tables: 
      actualDataNodes: ds_${0..1}.t_user_coupon_${0..31}
      databaseStrategy: 
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

### 2. Spring Configuration 

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

### 3. Using with HQL 
ShardingSphere handles routing transparently: 

```java
@Service 
public class UserCouponService {
    @Autowired 
    private QueryService queryService; 

    // ShardingSphere automatically routes based on user_id 
    public List<UserCoupon> findByUserId(Long userId) {
        String hql = "from UserCoupon where user_id = :userId"; 
        Map<String, Object> params = Map.of("userId", userId); 
        return queryService.query(hql, params); 
    }

    // Save operation -- automatically routed to correct shard 
    public UserCoupon save(UserCoupon coupon) {
        return queryService.save(coupon); 
    }
}
```

### 4. Using HqlQueryBuilder 

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

### 5. Sharding-Aware Operations 
For cases where we need explicit shard information: 

```java
@Autowired 
private ShardingAwareQueryService shardingService; 

// Get shard information 
ShardInfo info = shardingService.getShardInfo(
    userId, 
    shardingCount: 32, 
    databaseCount: 2,
    tableCount: 16
); 

// Returns ShardInfo(db=ds_0, table=t_user_coupon_5)
```

## Key Features 
### 1. Transparent Sharding 
- Hibernate/HQL queries work without modification 
- ShardingSphere handles routing automatically 
- No need to specify shard locations in code 

### 2. Hash-Based Distribution 
- Even data distribution across shards 
- Deterministic routing (same key -> same shard)
- Supports both database and table sharding 

### 3. Query Optimization 
- Single-shard queries (with sharding key) are routed to one shard 
- Cross-shard queries are automatically handled 
- Supports IN queries across multiple shards 

### 4. Compatibility 
- Works with existing Hibernate entities 
- Compatible with HqlQueryBuilder 
- No changes needed to entity classes 

## Sharding Key Selections 

### Best Practices 

- **Choose High-Cardinality Keys**: Keys with many distinct values ensure even distribution 
- **Use Business Keys**: Use keys that align with query patterns (e.g., `user_id` for user-related queries)
- **Avoid Hotspots**: Avoid keys that cause uneven distributino 
- **Consider Query Patterns**: Choose keys that match common query filters 

### Examples 
- **Good**: `user_id` - High cardinality, aligns with user queries 
- **Good**: `shop_number` - Good for shop-based partitioning 
- **Bad**: `status` - Low cardinality (only a few values)
- **Bad**: `created_date` - May cause hotspots for recent data 

## Migration from MyBatis 
### Differences 

| Aspect | MyBatis Approach | Hibernate/HQL Approach |
|--------|------------------|------------------------|
| Query Language | SQL (XML/Annotations) | HQL (Object-oriented) |
| Sharding Awareness | Explicit in SQL | Transparent via ShardingSphere |
| Type Safety | Manual mapping | Automatic via entities |
| Query Building | String concatenation | HqlQueryBuilder (type-safe) |


### Advantages of HQL Approach 
- **Type Safety**: HQL queries are validated against entity classes 
- **Object-Oriented**: Work with entities, not raw SQL 
- **Less Boilerplate**: No need for ResultMap or manual mapping 
- **Better IDE Support**: Autocomplete and refactoring support 
- **Transparent Sharding**: ShardingSphere handles routing automatically 

## Performance Considerations
### Single-Shard Queries 
Queries with sharding key in WHERE clause are routed to one shard: 

```hql 
-- Routed to single shard 
from UserCoupon where user_id = :userId
```

### Cross-Shard Queries 
Queries without sharding key query all shards: 

```hql 
-- Queries all shards, then merges results 
from UserCoupon where status = :status 
```

**Recommendation**: Always include sharding key when possible for better performance. 

### Batch Operations 
Batch operations are automatically distributed: 
```java
List<UserCoupoin>
```
