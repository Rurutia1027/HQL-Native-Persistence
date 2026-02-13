# Cloud Thread Pool and HikariCP: How They Work Together

You have a **cloud thread pool** with dynamic tuning and Prometheus/Grafana metrics. This doc explains how it relates to **HikariCP** and how both can be integrated in the same stack.

---

## 1. HikariCP is a connection pool, not a thread pool

- **HikariCP** manages **database connections** (JDBC). It keeps a fixed (or bounded) set of connections to the DB and hands them out to callers. When a caller is done, the connection is returned to the pool. So the “heavy” resource here is **connections** (limited by DB and network), not **threads**.
- **Your cloud thread pool** manages **worker threads** that run application code (e.g. HTTP request handling, async tasks). Those threads execute the logic that eventually calls `dataSource.getConnection()`, uses the connection, and returns it.

So:

- HikariCP = **connection pool** (DB connections).
- Cloud thread pool = **thread pool** (CPU/workers that run your code).

They solve different problems and are **complementary**, not replacements for each other.

---

## 2. How they work together (no “integration” in the sense of one inside the other)

Typical flow:

```
Request/task
    → Cloud thread pool: a worker thread is assigned
        → Application code runs on that thread
            → dataSource.getConnection()  (HikariCP: borrow a connection)
            → Execute SQL / Hibernate
            → connection.close() or return to pool  (HikariCP: give back connection)
        → Thread returns to your thread pool
    → Response
```

- **Threads** (from your cloud thread pool) are the ones that **use** connections from HikariCP.
- HikariCP does use a small number of **internal** threads (e.g. for keepalive, timeout), but the main “heavy” usage from the application’s point of view is **connection count**, not thread count.

So you don’t “plug HikariCP into” your thread pool. They **cooperate**: your pool provides threads; HikariCP provides connections; threads borrow connections when they need to hit the DB.

---

## 3. Can you “integrate” the cloud thread pool with HikariCP?

Yes, in the sense of **using both in the same app and observing both**:

1. **Runtime**  
   - Application work (including DB access) runs on threads from your **cloud thread pool**.  
   - DB access uses connections from **HikariCP**.  
   - No special “integration” API is required; the only link is: your code (running on your threads) calls the same `DataSource` that HikariCP implements.

2. **Observability (Prometheus / Grafana)**  
   - **Cloud thread pool:** you already expose custom metrics (e.g. active threads, queue size, rejections, dynamic core/max size). These can be scraped by Prometheus and visualized in Grafana.  
   - **HikariCP:** exposes metrics (active/idle connections, wait time, usage) via Micrometer or its own MBeans. Those can be scraped by Prometheus and shown in Grafana alongside your thread-pool metrics.  
   So “integration” here means: **both** pools expose metrics to the **same** Prometheus/Grafana stack. You get one dashboard with:
   - Thread pool: concurrency, queue, dynamic config.
   - Connection pool: connections in use, idle, wait time.

3. **Dynamic behavior**  
   - **Cloud thread pool:** you support dynamic modification (e.g. core/max size). That’s independent of HikariCP.  
   - **HikariCP:** pool size is usually set at startup (e.g. `maximumPoolSize`). Some runtimes allow changing it at runtime (e.g. Spring Boot Actuator or custom admin); that’s separate from your thread pool.  
   So “integration” does **not** mean one pool controlling the other; it means both can be tuned (and observed) in the same system.

---

## 4. Is HikariCP “thread heavy”?

- HikariCP is **connection-heavy** (it can hold many connections) and **light on threads** (few internal housekeeping threads).
- The **thread-heavy** part is usually **your application**: many threads (e.g. from your cloud thread pool or the HTTP container) all calling the same HikariCP `DataSource`. So the “thread heavy” item is the layer that **uses** HikariCP (your thread pool), not HikariCP itself.

If you want to avoid too many threads contending for too few connections, you tune:

- **Cloud thread pool:** max threads (so you don’t have 1000 threads fighting for 20 connections).
- **HikariCP:** `maximumPoolSize` (and optionally min idle) so you have enough connections for the number of threads that actually do DB work at once.

So integrating the two in practice means: **sizing both** (threads and connections) and **observing both** (Prometheus/Grafana).

---

## 5. Practical integration checklist (doc-level)

- [ ] **Runtime:** Ensure application code that does DB access runs on threads from your cloud thread pool (or a dedicated executor that you control), and that it uses a single shared `DataSource` backed by HikariCP.
- [ ] **Metrics – thread pool:** Expose your cloud thread pool metrics (e.g. via Micrometer) with a consistent prefix (e.g. `cloud_thread_pool_*`) so Prometheus can scrape them and Grafana can show them.
- [ ] **Metrics – HikariCP:** Enable HikariCP metrics (Micrometer or HikariCP’s registry) for the same `DataSource` and expose them (e.g. under `hikaricp_*` or `persistence_common_connection_pool_*`) so Prometheus can scrape and Grafana can show them.
- [ ] **Grafana:** One dashboard (or one row per “pool”) showing thread-pool and connection-pool metrics side by side (e.g. active threads vs active connections, queue depth vs connection wait time).
- [ ] **Sizing:** Document or automate a simple guideline (e.g. “max threads that do DB work” vs “HikariCP maximumPoolSize”) so the two pools are aligned and observable together.

This way, your cloud thread pool and HikariCP are **integrated** in the same architecture and observability stack, without one replacing or embedding the other.
