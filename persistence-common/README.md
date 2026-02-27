# Persistence Common Module 
A lightweight, flexible persistence layer built on top of Hibernate that provides an alternative to Spring Data JPA. This module offers fine-grained control over database operations while maintaining type safety and developer productivity. 

## Overview 

The Persistence Common module provides: 
- **Direct HQL/SQL Control**: Write queries using Hibernate Query Language (HQL) or native SQL with full contorl 
- **Type-Safe Query Builder**: Fluent API for building complex queries programmatically
- **Built-in Soft Delete**: Automatic filtering of deleted entities 
- **Entity Lifecycle Hooks**: Callback methods for entity operations 
- **Query Post-Processing**: Pluggable hooks for result transformation 
- **Pagination Support**: Built-in pagination utilities 
- **Entity Tagging**: Support for ETags and caching strategies 

## Key Components 
### Core Services 

- **`QueryService`**: Interface defining all query and persistence operations 
- **`PersistenceService`**: Main implementation providing Hiberante-based persistence 

### Entity Hierachy 
- **`SimplePersistedObject`**: Base class with UUID-based ID generation 
- **`PersistedObject`**: Extends SimplePersistedObject with versioning, timestamps, and soft delete support 
- **`NamedArtifact`**: Adds name and displayName fields 
- **`UniqueNameArtifact`**: Named artifact with uniqueness contrains 

### Query Building 
- **`HqlQueryBuilder`**: Fluent API for building HQL queries programmatically 
- Supports joins, conditions, subqueries, pagination, and more 

### Utilities 
- **`Page`**: Request pagination parameters 
- **`PageResponse`**: Paginated response wrapper 
- **`QueryPostProcessor`**: Interface for post-processing query results 


## Quick Start 

### 1. Add Dependency 
```xml
<dependency>
    <groupId>org.tus.common</groupId>
    <artifactId>persistence-common</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```


### 2. Configure Spring Beans

```java 
@Configuration
public class PersistenceConfig {
    
    @Bean
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl("jdbc:postgresql://localhost:5432/mydb");
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
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "none");
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
}
```

#### Using MySQL

To use MySQL as the data source, add the MySQL JDBC driver and configure the dialect:

**Maven dependency (in your application):**
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Configuration:**
```java
@Bean
@Primary
public DataSource dataSource() {
    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
    ds.setUrl("jdbc:mysql://localhost:3306/mydb?useUnicode=true&characterEncoding=utf8");
    ds.setUsername("user");
    ds.setPassword("password");
    return ds;
}

// In sessionFactory hibernate properties:
props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");  // or MySQL8Dialect for MySQL 8+
```

To run integration tests against MySQL (Testcontainers), use:  
`mvn test -DexcludedGroups=` (runs all tests including MySQL). By default, MySQL container tests are excluded for faster CI.

### 3. Define Entities 

```java
@Entity 
@Table(name = "users")
public class User extends PersistedObject {
    private String email;
    private String firstName;
    private String lastName;
    
    // Getters and setters
}
```

### 4. Use QueryService

```java
@Service
public class UserService {
    
    @Autowired
    private QueryService queryService;
    
    public User findById(String id) {
        return queryService.findObjectById(User.class, id);
    }
    
    public List<User> findByEmail(String email) {
        String hql = "from User where email = :email";
        Map<String, Object> params = Map.of("email", email);
        return queryService.query(hql, params);
    }
    
    public User save(User user) {
        return queryService.save(user);
    }
    
    public void delete(User user) {
        queryService.delete(user);
    }
}
```

## Usage Examples 
### Using HQL Query Builder 

```java
HqlQueryBuilder builder = new HqlQueryBuilder();
builder.from(User.class, "u")
       .leftJoin(Profile.class, "p", "u.id", "p.userId")
       .eq("u.email", email)
       .isNotNull("p.bio")
       .orderBy("u.createdDate", false);

String hql = builder.build();
Map<String, Object> params = builder.getInjectionParameters();
List<User> users = queryService.query(hql, params);
```

### Pagination 

```java
Page page = new Page(0, 20, "createdDate", Page.SORT_DESC);
String hql = "from User where deleted is null";
List<User> users = queryService.pagedQuery(
    hql, 
    new HashMap<>(), 
    page.getStart(), 
    page.getPageSize()
);
```

### Native SQL Queries 
```java
String sql = "SELECT u.*, COUNT(o.id) as order_count " +
             "FROM users u LEFT JOIN orders o ON u.id = o.user_id " +
             "WHERE u.created_date > ? " +
             "GROUP BY u.id";
List<Map<String, Object>> results = queryService.sqlQuery(sql, startDate);
```

### Query Post-Processing 
```java
QueryPostProcessor processor = new QueryPostProcessor() {
    @Override
    public <T> T processFindResult(T entity) {
        // Initialize lazy collections
        if (entity instanceof User) {
            ((User) entity).initialize();
        }
        return entity;
    }
    
    @Override
    public <T> List<T> processListResult(Collection<T> collection) {
        return new ArrayList<>(collection);
    }
};

User user = queryService.findObjectById(User.class, id, processor);
```

## Benefits Over Spring Data JPA 
### 1. **Full Query Control** 
- Write complex HQL queries without method name limitations 
- Direct SQL access when needed 
- No magic method name conventions 

### 2. **Type-Safe Query Building**
- Programmatic query construction with compile-time safety
- Reusable query builders
- Dynamic query generation

### 3. **Built-in Soft Delete**
- Automatic filtering of deleted entities
- No need for custom filters or annotations
- Consistent behavior across all queries

### 4. **Entity Lifecycle Hooks**
- `onSave()` callback for entity-specific logic
- Automatic timestamp management
- Version control support

### 5. **Query Post-Processing**
- Pluggable result transformation
- Lazy loading initialization
- Result caching hooks

### 6. **Explicit Session Management**
- Full control over Hibernate sessions
- Better understanding of persistence context
- Easier debugging

### 7. **No Repository Boilerplate**
- No need to create repository interfaces
- Direct service-to-persistence layer
- Less abstraction overhead

### 8. **Performance Optimization**
- Direct control over query execution
- Ability to optimize queries per use case
- No hidden N+1 query problems

## Architecture 

## Entity Hierarchy

```
SimplePersistedObject
    ├── id (UUID)
    └── onSave() hook

PersistedObject extends SimplePersistedObject
    ├── versionNumber (optimistic locking)
    ├── createdDate
    ├── modifiedDate
    ├── deleted (soft delete)
    ├── locked
    ├── disabled
    └── entityTag (for caching)
    
NamedArtifact extends PersistedObject
    ├── name
    └── displayName
    
UniqueNamedArtifact extends NamedArtifact
    └── uniqueness constraints    
```

## Testing 
The module includes test configurations for both H2 (in-memory) and PostgreSQL (Testcontainers): 
```java 
@SpringBootTest 
@ContextConfiguration(calsses = PersistenceH2DBConfig.class)
class UserServiceTest {
    // Tests run against H2 database 
}
```

## Requirements 
- Java 17+
- Hibernate 6.x 
- Spring Framework 6.x (for Spring integration)
- Any JPA-compatible database (PostgreSQL, MySQL, H2, etc.)

## LICENSE 
[LICENSE](../LICENSE)


## Contributing

Contributions are welcome! Please ensure all tests pass and follow the existing code style.
