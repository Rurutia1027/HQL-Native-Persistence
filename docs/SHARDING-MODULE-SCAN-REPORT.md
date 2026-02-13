# Sharding Module & HikariCP Scan Report

Scan of `/Users/emma/architecture/HQL-Native-Persistence` focused on: (1) HikariCP adoption across modules, (2) sharding-aware service definition and usage.

---

## 1. HikariCP Adoption

### Where HikariCP appears

| Location | How HikariCP is used |
|----------|----------------------|
| **persistence-sharding** `pom.xml` | Dependency declared (optional / for YAML-driven pools) |
| **persistence-sharding** `src/main/resources/shardingsphere-config.yaml` | `dataSourceClassName: com.zaxxer.hikari.HikariDataSource` per data source |
| **persistence-sharding** README / **docs/SHARDING_SOLUTION.md** | Docs show YAML with `HikariDataSource` |
| **payment-example-v1** | Spring Boot creates `HikariDataSource` in `PaymentPersistenceConfig` and sets it on `LocalSessionFactoryBean` |
| **payment-example-v2** | ShardingSphere YAML uses `HikariDataSource`; app does not create HikariCP in code |

### Where HikariCP is **not** adopted

| Module / area | Gap |
|---------------|-----|
| **persistence-common** | No HikariCP dependency and no code that creates or configures a connection pool. `PersistenceService` has a `DataSource` field used only in `queryByJdbc()`; it is never set when using the common constructor `PersistenceService(SessionFactory)`. So raw JDBC via `queryByJdbc` will NPE if the app does not inject `DataSource`. |
| **persistence-sharding** | No Java code instantiates or configures HikariCP. Pools are created only by ShardingSphere when it reads `dataSourceClassName: com.zaxxer.hikari.HikariDataSource` from YAML. The module does not expose pool metrics, health, or any HikariCP-specific API. |
| **ShardingSphereDataSourceConfig** | Builds `QueryService` with `new PersistenceService(sessionFactory)` only. It does **not** pass the ShardingSphere `DataSource` into `PersistenceService`, so `PersistenceService.dataSource` remains null. Any use of `queryByJdbc` in an app that only uses this config will fail. |

### Summary (HikariCP)

- **Adoption:** HikariCP is used only via **YAML** (ShardingSphere) or in **payment-example-v1** (Spring Boot). There is no shared, explicit “use HikariCP here” path in persistence-common or persistence-sharding.
- **Recommendations:**
  - In **persistence-common:** Document that for `queryByJdbc` to work, the application must set `PersistenceService.setDataSource(DataSource)`. Optionally add an overload `PersistenceService(SessionFactory, DataSource)` and recommend HikariCP in README.
  - In **persistence-sharding:** In `ShardingSphereDataSourceConfig`, when creating `QueryService`, pass the ShardingSphere `DataSource` into `PersistenceService` (e.g. setter or new constructor) so that raw JDBC and future pool metrics (e.g. HikariCP) can use the same data source.
  - Optionally add HikariCP as an optional dependency in persistence-common and document a recommended `DataSource` bean (e.g. HikariCP) for apps that need connection pooling and raw JDBC.

---

## 2. Sharding-Aware Services

### ShardingAwareQueryService – definition and usage

- **Interface:** `persistence-sharding/.../ShardingAwareQueryService.java`  
  Extends `QueryService` and adds:
  - `findObjectByIdWithShardingKey(Class, String id, Long shardingKey)`
  - `queryWithShardingKeys(String hql, Map, List<Long> shardingKeys)`
  - `getShardInfo(Long shardingKey, int shardingCount, int databaseCount, int tableCount)`
  - Inner class `ShardInfo` (databaseIndex, tableIndex, databaseName, tableName).

- **Implementation:** `ShardingAwareQueryServiceImpl` delegates to a `QueryService` and implements the three methods above.

### Issues

#### 2.1 `findObjectByIdWithShardingKey` does not use the sharding key

- **Current implementation:** Builds HQL `from <entity> where id = :id` and passes only `params = Map.of("id", id)`. The `shardingKey` parameter is **never** used in the query.
- **Effect:** ShardingSphere cannot route by sharding key. Without the sharding column in the WHERE clause, the query is broadcast to all shards (or routed incorrectly), which is the opposite of what a “find by ID with sharding key” API is for.
- **Comment in code:** The implementation even says “If the entity has a sharding key field, include it in the query” and “This ensures ShardingSphere routes correctly,” but the code does not add it.

**Recommendation:** Implement routing correctly by including the sharding key in the HQL and parameters. This requires knowing the sharding column name for the entity (e.g. `user_id`). Options:

- Add a parameter for the sharding column name and build HQL like `from X where id = :id and <shardingColumn> = :shardingKey`, or
- Use a convention (e.g. entity annotation or a registry) to resolve sharding column per entity, then add the predicate and bind `shardingKey`.

#### 2.2 `getShardInfo` returns a generic table name

- **Current implementation:** Returns `tableName = "t_" + tableIndex` (e.g. `t_0`, `t_1`). Real sharded tables are typically named like `t_user_coupon_0`, `t_order_1`, etc.
- **Effect:** Callers cannot use `ShardInfo.getTableName()` for actual table names; the API is misleading.

**Recommendation:** Either:

- Add a parameter (e.g. logical table name) and compute the actual physical table name (e.g. `logicalTableName + "_" + tableIndex`), or
- Document that `tableName` is only a suffix or index and that the full logical table name must be applied by the caller.

#### 2.3 ShardingAwareQueryService is not provided by the sharding module

- **Current state:** `ShardingSphereDataSourceConfig` defines beans for `DataSource`, `LocalSessionFactoryBean`, `QueryService`, and `PlatformTransactionManager`. It does **not** define a bean for `ShardingAwareQueryService`.
- **Usage:** payment-example-v2 creates it manually: `new ShardingAwareQueryServiceImpl(queryService)` in `PaymentShardingConfig`. So every app that wants sharding-aware operations must duplicate this wiring.

**Recommendation:** In persistence-sharding, add a `@Bean` for `ShardingAwareQueryService` in `ShardingSphereDataSourceConfig` (or a dedicated config class), e.g.:

```java
@Bean
public ShardingAwareQueryService shardingAwareQueryService(QueryService queryService) {
    return new ShardingAwareQueryServiceImpl(queryService);
}
```

Then applications can inject `ShardingAwareQueryService` without defining it themselves.

#### 2.4 payment-example-v2 does not use ShardingAwareQueryService for queries

- **Current state:** `BusinessAnalyticsService` injects and uses `QueryService` only. It never uses `ShardingAwareQueryService` (e.g. `findObjectByIdWithShardingKey` or `queryWithShardingKeys`). So the “sharding-aware” API is not exercised in the main business flow.
- This is consistent with “transparent” sharding (ShardingSphere routes based on HQL), but it means the added value of `ShardingAwareQueryService` (explicit shard routing and shard info) is unused and could be clarified in docs (when to use QueryService vs ShardingAwareQueryService).

---

## 3. Summary Table

| Topic | Finding | Recommendation |
|-------|---------|----------------|
| **HikariCP in persistence-common** | No dependency; no pool creation; `DataSource` on `PersistenceService` often null | Document DataSource requirement for `queryByJdbc`; optionally add constructor/setter and recommend HikariCP |
| **HikariCP in persistence-sharding** | Only in YAML and ShardingSphere; no Java usage or metrics | Pass ShardingSphere `DataSource` into `PersistenceService`; optionally expose pool metrics later |
| **findObjectByIdWithShardingKey** | Sharding key not included in HQL; routing incorrect | Add sharding column to WHERE and bind shardingKey (with configurable or convention-based column name) |
| **getShardInfo** | Returns generic `t_<index>` instead of logical table name | Parameterize logical table name or document that tableName is suffix-only |
| **ShardingAwareQueryService bean** | Not defined in sharding module; each app wires it manually | Add `ShardingAwareQueryService` bean in persistence-sharding config |
| **Usage in payment-example-v2** | Uses only `QueryService`; ShardingAwareQueryService unused in business logic | Optional: use `findObjectByIdWithShardingKey` where single-shard lookup by id + userId is needed; document when to use which service |

---

## 4. Files Touched by Recommendations

- **persistence-common:** `PersistenceService.java` (constructor/setter for DataSource), README.
- **persistence-sharding:** `ShardingSphereDataSourceConfig.java` (inject DataSource into PersistenceService; add `ShardingAwareQueryService` bean), `ShardingAwareQueryServiceImpl.java` (fix `findObjectByIdWithShardingKey`; optionally improve `getShardInfo`), README or a short “Sharding-aware API” section in docs.
- **payment-example-v2 (optional):** Use `ShardingAwareQueryService.findObjectByIdWithShardingKey` for by-id+userId lookups and document the choice.

This report can be used as a checklist for implementing the recommended changes.

---

## 5. TODO List (implementation order)

Use this list to implement the recommendations step by step. Doc-only items can be done first; code changes follow.

### 5.1 Persistence-common (HikariCP & DataSource)

- [ ] **DOC** – In persistence-common README, document that `queryByJdbc()` requires `PersistenceService` to have a non-null `DataSource` (e.g. via setter or constructor), and that callers must set it when using the single-arg `PersistenceService(SessionFactory)` constructor.
- [ ] **DOC** – In persistence-common README, add a short “Recommended DataSource” section: recommend HikariCP for production and link to Spring Boot / HikariCP setup.
- [ ] **CODE** – Add constructor overload `PersistenceService(SessionFactory sessionFactory, DataSource dataSource)` and/or ensure setter `setDataSource(DataSource)` is invoked where a DataSource is available (so `queryByJdbc` does not NPE).
- [ ] **OPT** – Add HikariCP as an optional dependency in persistence-common `pom.xml` and document when to use it (e.g. apps that need raw JDBC and connection pooling).

### 5.2 Persistence-sharding (DataSource wiring & beans)

- [ ] **CODE** – In `ShardingSphereDataSourceConfig`, when creating `QueryService`, pass the ShardingSphere `DataSource` into `PersistenceService` (e.g. `new PersistenceService(sessionFactory, shardingSphereDataSource)` or `setDataSource` after construction) so that `queryByJdbc` and future HikariCP metrics use the same data source.
- [ ] **CODE** – In `ShardingSphereDataSourceConfig` (or a dedicated config), add a `@Bean` for `ShardingAwareQueryService` that returns `new ShardingAwareQueryServiceImpl(queryService)` so applications can inject it without manual wiring.
- [ ] **DOC** – In persistence-sharding README, document that `ShardingAwareQueryService` is now provided as a bean when using the module’s config, and when to use it vs plain `QueryService`.

### 5.3 Sharding-aware API fixes

- [ ] **CODE** – Fix `findObjectByIdWithShardingKey`: include the sharding key in the HQL WHERE clause and bind the `shardingKey` parameter (e.g. add a method parameter or config for the sharding column name per entity, then build `and <column> = :shardingKey`).
- [ ] **CODE** – Improve or document `getShardInfo`: either add a logical table name parameter and return full physical table name (e.g. `logicalTableName + "_" + tableIndex`), or clearly document that `tableName` is a suffix/index only and the caller must combine with logical table name.
- [ ] **DOC** – Add a short “Sharding-aware API” section in persistence-sharding docs: when to use `QueryService` (transparent routing) vs `ShardingAwareQueryService` (explicit shard key, shard info, single-shard lookups).

### 5.4 Optional: Application usage

- [ ] **OPT** – In payment-example-v2 (or other apps), where a single-shard lookup by ID + sharding key is needed, use `ShardingAwareQueryService.findObjectByIdWithShardingKey` instead of `QueryService.findObjectById` and document the choice (e.g. in service Javadoc or README).
- [ ] **OPT** – Expose HikariCP pool metrics (e.g. via Micrometer) when using the ShardingSphere/HikariCP data source, and add a note in observability docs. See [Cloud thread pool and HikariCP](CLOUD-THREAD-POOL-AND-HIKARICP.md) for how the connection pool and your cloud thread pool work together and how both feed Prometheus/Grafana.

### 5.5 Summary checklist

| # | Task | Type | Module |
|---|------|------|--------|
| 1 | Document DataSource requirement and HikariCP recommendation in persistence-common README | DOC | persistence-common |
| 2 | Add PersistenceService(SessionFactory, DataSource) and/or ensure setDataSource used | CODE | persistence-common |
| 3 | (Optional) HikariCP optional dependency in persistence-common | CODE | persistence-common |
| 4 | Pass ShardingSphere DataSource into PersistenceService in ShardingSphereDataSourceConfig | CODE | persistence-sharding |
| 5 | Add ShardingAwareQueryService @Bean in persistence-sharding config | CODE | persistence-sharding |
| 6 | Document ShardingAwareQueryService bean and when to use it in persistence-sharding README | DOC | persistence-sharding |
| 7 | Fix findObjectByIdWithShardingKey to include sharding key in HQL | CODE | persistence-sharding |
| 8 | Improve getShardInfo (logical table name) or document tableName semantics | CODE/DOC | persistence-sharding |
| 9 | Document QueryService vs ShardingAwareQueryService usage | DOC | persistence-sharding |
| 10 | (Optional) Use findObjectByIdWithShardingKey in payment-example-v2 where appropriate | CODE | payment-example-v2 |
| 11 | (Optional) HikariCP metrics + observability note | DOC/CODE | observability |

Work through the list in order (doc first, then common, then sharding, then optional app/observability). Mark items done as you go.
