# HikariCP with DB Sharding, Spring, and K8s: A Short Routine

A concise routine for introducing HikariCP and understanding how it fits with database sharding, Spring, and Kubernetes before refining persistence-sharding.

---

## 1. HikariCP in one paragraph

**HikariCP** is a JDBC **connection pool**: it keeps a bounded set of open connections to the database and hands them out to application threads. When a thread finishes using a connection, it returns it to the pool instead of closing it, which avoids the cost of opening a new connection per request. It is lightweight (few internal threads), fast, and the default pool in Spring Boot. Key knobs: `maximumPoolSize`, `minimumIdle`, `connectionTimeout`, `idleTimeout`, `maxLifetime`.

---

## 2. How HikariCP works with DB sharding (ShardingSphere)

- **One pool per physical data source.** In ShardingSphere you define multiple data sources in YAML (e.g. `ds_0`, `ds_1`). For each entry you set `dataSourceClassName: com.zaxxer.hikari.HikariDataSource`. ShardingSphere then **instantiates one HikariCP pool per data source** using that class and the given `jdbcUrl`, `username`, `password`, and optional HikariCP props.

- **Application sees a single logical DataSource.** Your app (and Hibernate) use the **ShardingSphere DataSource**. It is a facade that routes each statement to the right shard. Under the hood it gets connections from the appropriate underlying HikariCP pool (e.g. from `ds_0` or `ds_1`) per request. So: **many HikariCP pools (one per shard), one logical DataSource in the app.**

- **Sizing.** Total connections ≈ (number of shard data sources) × (per-pool `maximumPoolSize`). Keep per-pool size reasonable so that (pods × pools × pool size) does not exceed what the DB(s) can handle. Each shard DB has its own connection limit.

- **Summary:** HikariCP is the **implementation** of each shard’s connections; ShardingSphere is the **router** that uses those connections. No code change is needed in the app beyond pointing to the ShardingSphere DataSource; HikariCP is configured in the sharding YAML per data source.

---

## 3. How HikariCP works with Spring

- **Sharding setup (this project):** The ShardingSphere DataSource is created from YAML (e.g. `YamlShardingSphereDataSourceFactory.createDataSource(...)`). That DataSource is the one registered as a bean and injected into `LocalSessionFactoryBean` (Hibernate). So Spring and Hibernate never see “HikariCP” directly; they see the ShardingSphere DataSource, which internally uses HikariCP per shard.

- **Non-sharding (single DB):** Spring Boot can create the DataSource itself. With `spring-boot-starter-jdbc` or `-jpa`, it typically creates a `HikariDataSource` and configures it from `spring.datasource.*` and `spring.datasource.hikari.*` (e.g. `maximum-pool-size`, `minimum-idle`). You inject that DataSource and pass it to Hibernate or JdbcTemplate.

- **Common point:** Whether the bean is ShardingSphere (multi-pool) or a single HikariDataSource, the app and Spring always work with a `DataSource` bean. HikariCP is either the type of that bean (single DB) or the type used internally by ShardingSphere for each shard (sharding).

- **Optional:** Expose HikariCP metrics via Micrometer (Spring Boot Actuator + `micrometer-registry-prometheus`) so pool health and usage are visible in Prometheus/Grafana. For ShardingSphere, each underlying pool can be registered if the runtime exposes them.

---

## 4. How this fits in Kubernetes (K8s)

- **Connection URLs.** In K8s, DB hosts are usually Services (e.g. `postgres-payment-0:5432`). In ShardingSphere YAML you set each shard’s `jdbcUrl` to the corresponding K8s Service (or to a single DB host for non-sharded). So HikariCP connects to whatever host:port you put in `jdbcUrl` (often internal K8s DNS).

- **Pool size and pod count.** Total DB connections ≈ **pods × (number of shard pools per pod) × maximumPoolSize per pool**. Size each pool so that (replicas × total connections per pod) stays under the DB’s `max_connections` (and under any connection limits imposed by your DB operator or cloud).

- **Readiness / liveness.** The app is ready when it can serve traffic; that implies the DataSource (and thus the underlying HikariCP pools) can obtain connections. A readiness probe that hits an endpoint that runs a simple DB check (e.g. `SELECT 1` or actuator health) is enough. No need to probe HikariCP directly if health is expressed via DB health.

- **Multi-replica.** Each pod has its own ShardingSphere DataSource and thus its own set of HikariCP pools (one per shard). So with 3 app replicas and 2 shards and 10 connections per pool, you have 3 × 2 × 10 = 60 connections to the DB layer. Plan DB capacity and pool sizes accordingly.

- **Secrets.** Store DB credentials in K8s Secrets (or an external secret store). Inject them into the app via env or mounted files and substitute into `jdbcUrl` / `username` / `password` (e.g. with placeholders and a small bootstrap step, or with a template in Helm/Kustomize). HikariCP only sees the final URL and credentials it is given when the pool is created.

---

## 5. Routine checklist (before refining persistence-sharding)

- [ ] **Concept:** HikariCP = connection pool per (logical) DB; ShardingSphere = one logical DataSource, many underlying HikariCP pools (one per shard data source in YAML).
- [ ] **Config:** In sharding YAML, each `dataSources.<name>` uses `dataSourceClassName: com.zaxxer.hikari.HikariDataSource` and sets `jdbcUrl`, credentials, and optional HikariCP properties (e.g. `maximumPoolSize`, `connectionTimeout`).
- [ ] **Spring:** App and Hibernate use the ShardingSphere DataSource bean; no direct HikariCP bean in the app when sharding. For single-DB apps, Spring Boot can create a single HikariDataSource bean.
- [ ] **K8s:** Point `jdbcUrl` at K8s Service names; size pools and replicas so total connections are within DB limits; use readiness (e.g. DB health) and Secrets for credentials.

With this routine clear, refining persistence-sharding (wiring DataSource into PersistenceService, optional metrics, and ShardingAwareQueryService) stays consistent with how HikariCP and sharding already work: one logical DataSource, HikariCP per shard under the hood, Spring and K8s unchanged at the API level.
