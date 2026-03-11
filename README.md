# HQL Native Persistence | [![Persistence Sharding CI](https://github.com/Rurutia1027/HQL-Native-Persistence/actions/workflows/persistence-sharding-ci.yml/badge.svg)](https://github.com/Rurutia1027/HQL-Native-Persistence/actions/workflows/persistence-sharding-ci.yml)

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.x-blue.svg)](https://hibernate.org/)
[![TiDB](https://img.shields.io/badge/TiDB-Native%20Sharding-green.svg)](https://docs.pingcap.com/tidb/stable)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight, flexible persistence layer built on Hibernate/HQL that provides an alternative to Spring Data JPA and MyBatis. Features **TiDB-native sharding** (partition-aware HQL), type-safe query building, and full control over database operations.

## Features 
- **HQL-Native**: Pure Hibernate Query Language (HQL) - No MyBatis, no Spring Data JPA 
- **Type-Safe Query Building**: Fluent API for building complex queries programmatically 
- **TiDB-Native Sharding**: Single DataSource, partition keys in DDL + `ShardingContext` / `@ShardingKey` in code; no application-side routing 
- **Multi-RDBMS Support**: Works with MySQL, TiDB, PostgreSQL, Oracle, SQL Server, and more 
- **Cloud-Native Ready**: Designed for containerized, scalable architectures
- **Built-in Soft Delete**: Automatic filtering of deleted entities 
- **Entity Lifecycle Hooks**: Callback methods for entity operations 
- **Query Post-Processing**: Pluggable hooks for result transformation 
- **Pagination Support**: Built-in pagination utilities 

## Architecture
### Persistence Common 
<img width="340" height="600" alt="Screenshot 2026-02-10 at 17 20 31" src="https://github.com/user-attachments/assets/021f522e-c22c-4e3b-ba71-674435e01a9c" />

### Persistence Sharding 
// TODO 

## Quick Start 

### Maven Dependency 

```xml
<dependency>
    <groupId>org.tus.common</groupId>
    <artifactId>persistence-common</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

<!-- For sharding support -->
<dependency>
    <groupId>org.tus.common</groupId>
    <artifactId>persistence-sharding</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Basic Usage 

```java
@Service
public class UserService {
    
    @Autowired
    private QueryService queryService;
    
    // Simple HQL query
    public User findById(String id) {
        return queryService.findObjectById(User.class, id);
    }
    
    // Type-safe query building
    public List<User> findByEmail(String email) {
        HqlQueryBuilder builder = new HqlQueryBuilder();
        builder.from(User.class, "u")
               .leftJoin(Profile.class, "p", "u.id", "p.userId")
               .eq("u.email", email)
               .orderBy("u.createdDate", false);
        
        String hql = builder.build();
        Map<String, Object> params = builder.getInjectionParameters();
        return queryService.query(hql, params);
    }
}
```

## Documentation 
- [Persistence Common Module](docs/PERSISTENCE_COMMON.md) - Core persistence layer 
- [TiDB Sharding Solution](docs/TIDB_SHARDING_SOLUTION.md) - TiDB-native sharding design 
- [TiDB Sharding API Design](docs/TIDB_SHARDING_API_DESIGN.md) - ShardingContext, ShardingAwareQueryService 
- [TiDB Sharding Concepts](docs/TIDB_SHARDING_CONCEPTS.md) - Region, partition, multi-tenant 
- [TiDB Observability](docs/TIDB_OBSERVABILITY_DESIGN.md) - Metrics, tracing, logging 
- [Tradeoffs Analysis](docs/TRADEOFFS.md) - Comparison with MyBatis/Spring Data JPA
- [Article: Beyond Spring Data JPA](docs/ARTICLE.md) - Detailed benefits analysis


## Database Support 

### Supported Databases (Single Type per Deployment)

This solution supports multiple relational databases through Hibernate's dialect system. **However, you must use the same database type across all shards in a single deployment.**

| Database | Status | Notes |
|----------|--------|-------|
| **MySQL / TiDB** | ✅ Fully Supported | Production-ready, well-tested; TiDB is MySQL-compatible |
| **PostgreSQL** | ✅ Supported | Excellent HQL support (separate deployment profile) |
| **Oracle** | ✅ Supported | Requires Oracle dialect |
| **SQL Server** | ✅ Supported | Microsoft SQL Server support |
| **H2** | ✅ Supported | Great for testing |
| **MariaDB** | ✅ Supported | Compatible with MySQL dialect |
| **DB2** | ✅ Supported | Enterprise database support |


### TiDB Sharding: One Logical Database

With **persistence-sharding**, you connect to a **single TiDB cluster** (one JDBC URL). Sharding is expressed in TiDB via **partitioned tables** (`PARTITION BY HASH(sharding_key)`). The application layer uses `ShardingContext` and `@ShardingKey` so that queries always include partition keys, enabling TiDB to prune partitions efficiently.

```yaml
# Single TiDB endpoint (default port 4000)
spring:
  datasource:
    url: jdbc:mysql://tidb-host:4000/your_db
    username: root
    password: ...
```

### Multiple Database Support Across Versions

While a single deployment must use one database type, **different versions/releases of your application can support different database types**. For example:

- **Version 1.0.1-MySQL**: MySQL-only deployment
- **Version 1.0.1-PostgreSQL**: PostgreSQL-only deployment  
- **Version 1.0.1-Oralce**: Oracle-only deployment

Each version is configured for a specific database type, but the codebase supports multiple database types across different deployments.

### Configuration Example

```java
@Configuration
public class PersistenceConfig {
    
    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.example.entity");
        
        Properties props = new Properties();
        // Configure dialect for your chosen database type
        // All data sources in this deployment must use the same type
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        
        factory.setHibernateProperties(props);
        return factory;
    }
}
```

## Cloud-Native Architecture 
### Cloud-Native Compatibility 

This solution is designed with cloud-native principles in mind: 

#### Stateless Design 
- No session state stored in application 
- Hibernate sessions are request-scoped 
- Perfect for horizontal scaling 

#### Container-Friendly 
```dockerfile 
FROM openjdk:17-jdk-slim
COPY target/persistence-common-*.jar /app/
# Stateless, can scale horizontally
```

```yaml 
# Kubernetes ConfigMap / Environment Variables
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```

#### Service Discovery Integration

Works seamlessly with service discovery: 

```java
@Configuration
public class CloudNativeConfig {
    
    @Bean
    public DataSource dataSource(@Value("${database.service.name}") String serviceName) {
        // Integrate with Consul, Eureka, Kubernetes DNS, etc.
        String jdbcUrl = discoverDatabaseUrl(serviceName);
        return createDataSource(jdbcUrl);
    }
}
```

#### Connection Pooling 

Use HikariCP (cloud-optimized connection pool): 

```yaml 
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    maximumPoolSize: 20  # Adjust based on cloud instance size
    minimumIdle: 5
    connectionTimeout: 30000
    idleTimeout: 600000
    maxLifetime: 1800000
```

#### Health Checks

Provides health check endpoints:

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private QueryService queryService;
    
    @Override
    public Health health() {
        try {
            queryService.query("select 1", new HashMap<>());
            return Health.up().build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
```


### Kubernetes Deployment Example

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-with-persistence
spec:
  replicas: 3  # Horizontal scaling
  template:
    spec:
      containers:
      - name: app
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
```

### Cloud Native Services 

Works with managed database services: 

- ✅ **AWS RDS** (MySQL, PostgreSQL, Oracle, SQL Server)
- ✅ **Google Cloud SQL** (MySQL, PostgreSQL)
- ✅ **Azure Database** (MySQL, PostgreSQL, SQL Server)
- ✅ **Alibaba Cloud RDS** (MySQL, PostgreSQL)
- ✅ **Database-as-a-Service** providers

### Sharding with TiDB in Cloud Environments

TiDB fits cloud-native setups: one logical database, horizontal scaling by adding TiKV/TiDB nodes. No application-side shard routing.

```yaml 
# Point to your TiDB cluster (self-hosted or TiDB Cloud)
spring:
  datasource:
    url: jdbc:mysql://tidb-service:4000/app_db
```

**Benefits**:
- Single DataSource; TiDB handles Region distribution and rebalancing
- Scale by adding nodes; no code or config changes for resharding
- Partition pruning when queries include sharding keys (`ShardingContext`)
- Works with TiDB Cloud, Kubernetes (TiDB Operator), or on-prem

## Modules 
### persistence-common 

Core persistence layer with HQL support: 
- `QueryService` - Main query interface 
- `PersistenceService` - Hibernate implementation 
- `HqlQueryBuilder` - Type-safe query builder
- Entity hierarchy (PersistedObject, NamedArtifact, etc.)

### persistence-sharding 

TiDB-native sharding support (no ShardingSphere): 
- `ShardingContext` – carry partition/sharding keys per request 
- `ShardingAwareQueryService` – query API that enforces sharding keys 
- `ShardingMeta` / `@ShardedEntity` / `@ShardingKey` – entity-level sharding metadata 
- `ShardedPersistedObject` – optional base class for sharded entities 
- Spring config: `TiDBShardingSpringConfig`, `AnnotationBasedShardingMetaFactory`

## Use Cases 
### When to Use This Solution 

✅ **Choose this solution if:**
- You need fine-grained control over queries
- You want type-safe query building
- You require transparent database sharding
- You prefer HQL over SQL strings
- You need cloud-native compatibility
- You work with multiple database types
- You want to avoid MyBatis boilerplate
- You need better IDE support


❌ **Consider alternatives if:**
- You only need simple CRUD operations
- Your team prefers SQL over HQL
- You need maximum SQL performance control
- You have very simple data access patterns

## 🔧 Configuration

### Basic Configuration

```java
@Configuration
public class PersistenceConfig {
    
    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        ds.setUsername("user");
        ds.setPassword("password");
        return ds;
    }
    
    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.example.entity");
        
        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        factory.setHibernateProperties(props);
        return factory;
    }
    
    @Bean
    public QueryService queryService(SessionFactory sessionFactory) {
        return new PersistenceService(sessionFactory);
    }
}
```

### Sharding Configuration (TiDB)

See [persistence-sharding README](persistence-sharding/README.md) and [TiDB Sharding Solution](docs/TIDB_SHARDING_SOLUTION.md) for setup. Use `TiDBShardingSpringConfig` and a `ShardingMeta` bean (e.g. from `AnnotationBasedShardingMetaFactory`).

## Performance

### Query Performance

- **Partition pruning**: TiDB prunes partitions when HQL includes sharding keys (via `ShardingContext`)
- **Single DataSource**: No proxy or client-side routing; TiDB handles distribution
- **Batch operations**: Hibernate batching + TiDB-friendly DDL (e.g. `AUTO_RANDOM`)
- **Connection pooling**: HikariCP for optimal performance

### Scalability

- **Horizontal scaling**: Stateless app design; TiDB scales by adding TiKV/TiDB nodes
- **TiDB-native sharding**: Regions and partitions managed by TiDB; no app resharding
- **Connection management**: One connection pool to TiDB
- **Cloud-optimized**: TiDB Cloud, K8s (TiDB Operator), or on-prem

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

## Acknowledgments

- [Hibernate](https://hibernate.org/) - The underlying ORM framework
- [TiDB](https://docs.pingcap.com/tidb/stable) - Distributed SQL database used for native sharding
- Inspired by the need for a better persistence layer alternative

## Support

For questions and support:
- Open an issue on GitHub
- Check the [documentation](README.md)
- Review [TiDB sharding docs](docs/TIDB_SHARDING_SOLUTION.md) and [examples](examples/persistence-sharding-example)

---

**Built with ❤️ for developers who want control, type safety, and cloud-native compatibility.**

