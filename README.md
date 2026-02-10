# HQL Native Persistence 

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.x-blue.svg)](https://hibernate.org/)
[![ShardingSphere](https://img.shields.io/badge/ShardingSphere-5.4+-green.svg)](https://shardingsphere.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight, flexible persistence layer built on Hibernate/HQL that provides an alternative to Spring Data JPA and MyBatis. Features transparent database sharding, type-safe query building, and full control over database operations. 

## Features 
- **HQL-Native**: Pure Hibernate Query Language (HQL) - No MyBatis, no Spring Data JPA 
- **Type-Safe Query Building**: Fluent API for building complex queries programmatically 
- **Transparent Sharding**: Apache ShardSphere integration for database/table sharding 
- **Multi-RDBMS Support**: Works with MySQL, PostgreSQL, Oracle, SQL Server, and more 
- **Cloud-Native Ready**: Designed for containerized, scalable architectures
- **Built-in Soft Delete**: Automatic filtering of deleted entities 
- **Entity Lifecycle Hooks**: Callback methods for entity operations 
- **Query Post-Processing**: Pluggable hooks for result transformation 
- **Pagination Support**: Built-in pagination utilities 

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
- [Sharding Solution](docs/SHARDING_SOLUTION.md) - Database sharding guide 
- [Tradeoffs Analysis](docs/TRADEOFFS.md) - Comparison with MyBatis/Spring Data JPA
- [Article: Beyond Spring Data JPA](docs/ARTICLE.md) - Detaild benefits analysis


## Database Support 

### Supported Databases

This solution supports multiple relational databases through Hibernate's dialect system. **However, you must use the same database type across all shards in a single deployment.**

| Database | Status | Notes |
|----------|--------|-------|
| **MySQL** | ✅ Fully Supported | Production-ready, well-tested |
| **PostgreSQL** | ✅ Fully Supported | Excellent HQL support |
| **Oracle** | ✅ Supported | Requires Oracle dialect |
| **SQL Server** | ✅ Supported | Microsoft SQL Server support |
| **H2** | ✅ Supported | Great for testing |
| **MariaDB** | ✅ Supported | Compatible with MySQL dialect |
| **DB2** | ✅ Supported | Enterprise database support |


### Important: Single Database Type Per Deployment in Cloud Native Envs 
**CRITICAL**: This solution does **NOT** support mixing different database types in the same configuration. All data sources in a sharding configuration must use the same database type. 

#### ❌ NOT Supported - Mixed Database Types

```yaml 
# ❌ DO NOT DO THIS - Mixed database types are NOT supported
dataSources:
  ds_0:
    jdbcUrl: jdbc:mysql://localhost:3306/db_0  # MySQL
  ds_1:
    jdbcUrl: jdbc:postgresql://localhost:5432/db_1  # PostgreSQL - NOT ALLOWED!
```

**Why Mixed Types Are Not Supported:**
- SQL syntax differences cause query failures 
- Data type mapping inconsistencies 
- Different performance characteristics 
- Increased maintenance complexity 
- Transaction behavior differences 
- Sharding algorithm incompatibilities 

#### Supported - Single Database Type Per Deployment 

```yaml
# ✅ CORRECT - All shards use the same database type
dataSources:
  ds_0:
    jdbcUrl: jdbc:mysql://localhost:3306/db_0  # MySQL
  ds_1:
    jdbcUrl: jdbc:mysql://localhost:3306/db_1  # MySQL - Same type!
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

### Sharding in Cloud Environments

ShardingSphere works excentlly in cloud-native setups: 

```yaml 
# Cloud-native sharding configuration
dataSources:
  ds_0:
    jdbcUrl: jdbc:mysql://rds-instance-0.region.rds.amazonaws.com:3306/db
  ds_1:
    jdbcUrl: jdbc:mysql://rds-instance-1.region.rds.amazonaws.com:3306/db
```

**Benefits**:
- Distributed load across multiple database instances
- Scale databases independently 
- Highly available through sharding 
- Cost optimization (smaller instances)

## Architecture

<img width="489" height="851" alt="Screenshot 2026-02-10 at 15 57 40" src="https://github.com/user-attachments/assets/4f5652e1-cadf-420e-a94b-67461acca947" />


## Modules 
### persistence-common 

Core persistence layer with HQL support: 
- `QueryService` - Main query interface 
- `PersistenceService` - Hibernate implementation 
- `HqlQueryBuilder` - Type-safe query builder
- Entity hierarchy (PersistedObject, NamedArtifact, etc.)

### persistence-sharding 

Database sharding support: 
- `DBHashModShardingAlgorithm` - Database sharding 
- `TableHashModShardingAlgorithm` - Table sharding 
- `ShardingAwareQueryService` - Sharding utilities 
- ShardingSphere integration 

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

### Sharding Configuration

See [docs/README.md](persistence-sharding/README.md) for detailed sharding setup.

## Performance

### Query Performance

- **Single-shard queries**: Optimized routing to one shard
- **Cross-shard queries**: Automatic merging by ShardingSphere
- **Batch operations**: Distributed across shards automatically
- **Connection pooling**: HikariCP for optimal performance

### Scalability

- **Horizontal scaling**: Stateless design enables easy scaling
- **Database sharding**: Distribute load across multiple databases
- **Connection management**: Efficient connection pooling
- **Cloud-optimized**: Designed for cloud database services

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

## Acknowledgments

- [Hibernate](https://hibernate.org/) - The underlying ORM framework
- [Apache ShardingSphere](https://shardingsphere.apache.org/) - Database sharding solution
- Inspired by the need for a better persistence layer alternative

## Support

For questions and support:
- Open an issue on GitHub
- Check the [documentation](README.md)
- Review [examples](docs/SHARDING_SOLUTION.md)

---

**Built with ❤️ for developers who want control, type safety, and cloud-native compatibility.**
