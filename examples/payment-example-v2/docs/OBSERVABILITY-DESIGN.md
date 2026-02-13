# Observability Design: Three Layer SQL/DB Query Project

## 1. Overview & Goals

This documentation defines **observability** for the three-layer persistence stack used by the payment example:

| Layer             | Module                 | Responsibility                                                       |
|-------------------|------------------------|----------------------------------------------------------------------|
| **L1 – SQL/DB**   | `persistence-common`   | Session/connection, HQL execution, JDBC, transaction lifecycle       |
| **L2 – Sharding** | `persistence-sharding` | Shard routing, merge behavior, shard-aware query API                 |
| **L3 – Business** | `payment-example-v2`   | Business analytics (e.g. `getOverallMetrics`) and use-case semantics |

**Goals**

- **Custom metrics** per layer with clear ownership and naming.
- **Prometheus** as the metrics backend (scrape endpoints, consistent naming/labels).
- **Grafana** for dashboards (per-layer and cross-layer).
- **Distributed tracing (spans)** so a single `getOveralMetrics` call can be followed across L1->L2->L3 and down to
  SQL/shard.

--- 

## 2. Data Flow: `getOverallMetrics` Through the Layers

A single call to `BusinessAnalyticsService#getOverallMetrics(startDate, endDate)` triggers:

```
[L3] getOverallMetrics()
  ├─ countOrders()           → queryService.query(HQL)  [L2]
  ├─ countPayments()          → queryService.query(HQL)  [L2]
  ├─ calculateTotalRevenue()  → queryService.query(HQL)  [L2]
  ├─ calculateTotalRefundAmount() → queryService.query(HQL)  [L2]
  ├─ getPaymentMethodDistribution() → 5 × queryService.query()  [L2]
  ├─ getOrderStatusDistribution()   → 6 × queryService.query()  [L2]
  └─ getPaymentStatusDistribution() → 6 × queryService.query()  [L2]
  
[L2] ShardingAwareQueryService / ShardingSphere
  ├─ Route HQL to shard(s) – cross-shard = multiple DBs
  ├─ Execute per-shard (delegate to L1)
  └─ Merge results (if needed)
       │
       ▼  
[L1] QueryService / PersistenceService (Hibernate/JDBC)
  ├─ Session/connection from pool
  ├─ HQL → SQL, execute, map results
  └─ Transaction boundaries
```

**Observability requirements**

- One **trace** per `getOverallMetrics` with a **span** for the L3 operation and child spans for L2 (per logical query
  or per shard) and L1 (per DB execution).
- **Metrics** at L1 (SQL/DB), L2 (sharding), L3(business) so we can see latency, throughput, and errors at each level
  and correlate with shard count and query type.

--- 

## 3. Observability

| Concern            | Technology                                                      | Purpose                                                       |
|--------------------|-----------------------------------------------------------------|---------------------------------------------------------------|
| **Metrics**        | Micrometer + Prometheus                                         | Counters, timers, gauges; scrape endpoint for Prometheus      |
| **Tracing**        | Micrometer Tracing (e.g. Brave/Zipkin or OTLP) or OpenTelemetry | Spans for L1/L2/L3 and propagation                            |
| **Dashboards**     | Grafana                                                         | Dashboards per layer + one “getOverallMetrics” flow dashboard |
| **Custom metrics** | Application code + Micrometer `MeterRegistry`                   | Layer-specific indicators (see below)                         |

**Principle:** Each module registers only its own metrics and spans; the business module (L3) does not duplicate
low-level DB metrics.


--- 

## 4. Layers 1 - persistence-common (SQL/DB Query Layer)

**Focus** Single-DB view: connection usage, statement execution, transaction duration, errors. No shard awareness.

### 4.1 Suggested Metrics (Custom)

| Metric Name                                       | Type    | Labels                                                       | Description                                      |
|---------------------------------------------------|---------|--------------------------------------------------------------|--------------------------------------------------|
| `persistence_common_query_duration_seconds`       | Timer   | `operation=query\|save\|update\|delete`, `entity` (optional) | Duration of each persistence operation           |
| `persistence_common_query_total`                  | Counter | `operation`, `status=success\|error`, `entity` (optional)    | Number of queries by outcome                     |
| `persistence_common_connection_pool_active`       | Gauge   | `pool` (e.g. datasource name)                                | Active connections (if exposed by pool)          |
| `persistence_common_connection_pool_idle`         | Gauge   | `pool`                                                       | Idle connections                                 |
| `persistence_common_transaction_duration_seconds` | Timer   | `status=commit\|rollback`                                    | Transaction commit/rollback duration             |
| `persistence_common_sql_execution_seconds`        | Timer   | `statement_type=select\|insert\|update\|delete`              | Low-level SQL execution time (if hook available) |

### 4.2 Span Design (L1)

- **Span name**: e.g. `persistence.query` or `db.query`.
- **Attributes**: `entity`, `operation` (query/save/update/delete), optional `hql.statement` (truncated)
- **Position** Around `QueryService.query()`, `save()`, etc.; one span per logical persistence operations. If one HQL
  triggers multiple SQL rounds , either one span per round or one parent span with internal breakdown (implementation
  choice).

### 4.3 Implementation Notes (Common Module)

- Inject `MeterRegistry` and `Tracer` (Micrometer Tracing or OTel).
- In `PersistenceService` / `QueryService`: start a span and timer per operation; record success/error and duration;
  close span on exit (use try / finally or AOP).
- Optionally wrap the underlying `DataSource` or use HikariCP metrics to expose pool

--- 

## 5. Layer 2 - persistence-sharding (Sharding Layer)

**Focus**: Shard routing, fan-out, merge; cross-shard vs single-shard; errors and latency at shard level.

### 5.1 Suggested Metrics (Custom)

| Metric Name                                   | Type               | Labels                                                                      | Description                                                                    |
|-----------------------------------------------|--------------------|-----------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| `persistence_sharding_query_duration_seconds` | Timer              | `routing=single_shard\|multi_shard`, `logical_entity` (e.g. Order, Payment) | Duration of sharding-aware query (including merge)                             |
| `persistence_sharding_queries_per_request`    | Histogram or Gauge | `routing`                                                                   | Number of shard queries triggered per logical request (e.g. 4 for cross-shard) |
| `persistence_sharding_shards_queried_total`   | Counter            | `datasource`, `logical_table`                                               | Number of shards hit per logical table/datasource                              |
| `persistence_sharding_merge_duration_seconds` | Timer              | `logical_entity`                                                            | Time spent merging results from multiple shards                                |
| `persistence_sharding_route_hits_total`       | Counter            | `datasource`, `routing=single\|multi`, `status=success\|error`              | Count of routed queries by outcome                                             |
| `persistence_sharding_errors_total`           | Counter            | `reason=timeout\|merge_error\|routing_error`                                | Sharding-specific failures                                                     |

### 5.2 Span Design (L2)

- **Parent span (L3)**: e.g., `analytics.getOverallMetrics` or `business.getOverallMetrics`
- **L2 child spans**: One span per "logical query" from L3, e.g., `sharding.query` with attributes:

> `routing=single_shard|multi_shard`
> `logical_entity=Order|Payment|Refund`
> `shard_count=N`

- Optionally, one span per shard execution (e.g., `sharding.shard_quer`) with `datasource`, `shard_index` so
  Grafana/trace UI can show which DBs were hit.

### 5.3 Implementation Notes (Sharding Module)

- `ShardingAwareQueryServiceImpl` (or equivalent) should wrap calls to `QueryService` with:
    - Timer and counter for `persistence_shard_*` metrics.
    - Span creation and propagation; set attributes from routing result (single vs multi, shard count).
- If ShardingSphere exposes hooks or events for "before route" / "after merge", use them to record
  `persistence_sharding_merge_duration_seconds` and `persistence_sharding_queries_per_request` accurately.

--- 

## 6. Layer 3 - Business Module (BusinessAnalyticsService)

**Focus**: Business semantics: "overall metrics" request rate, end-to-end latency, breakdown by metric type (orders,
payments, refunds, distributions), and error rate.

### 6.1 Suggested Metrics (Custom)

| Metric Name                                           | Type      | Labels                                                                                                                            | Description                                                                                              |
|-------------------------------------------------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| `business_analytics_overall_metrics_duration_seconds` | Timer     | —                                                                                                                                 | End-to-end duration of `getOverallMetrics()`                                                             |
| `business_analytics_overall_metrics_total`            | Counter   | `status=success\|error`                                                                                                           | Invocation count                                                                                         |
| `business_analytics_subquery_duration_seconds`        | Timer     | `subquery=count_orders\|count_payments\|total_revenue\|total_refund\|payment_method_dist\|order_status_dist\|payment_status_dist` | Per-subquery duration (e.g. countOrders, countPayments, …)                                               |
| `business_analytics_subquery_total`                   | Counter   | `subquery`, `status=success\|error`                                                                                               | Per-subquery call count                                                                                  |
| `business_analytics_cross_shard_queries_per_request`  | Histogram | —                                                                                                                                 | Number of cross-shard (or total) DB queries per one `getOverallMetrics` (optional, if available from L2) |

### 6.2 Span Design (L3)

- **Root span for the request**: e.g., `getOverallMetrics` or `business.analytics.getOverallMetrics`.
    - Attributes: `date_range_start`, `date_range_end` (if safe and small)
- **Child span**: One per logical step (e.g., count orders, count payments, revenue, refunds, each distribution). Naming
  e.g., `analytics.count_orders`, `analytics.calculate_total_revenue`.
- These child spans will become parents of L2/L1 spans when the call goes to `queryService.query()`

### 6.3 Implementation Notes (Business Module)

- In `BusinessAnalyticsService#getOverallMetircs()`
    - Start a root span and a timer `business_analytics_overall_metrics_duraiton_seconds`
    - For each logical step (countOrders, countPayment, calculateTotalRevenue, etc.):
        - Start a child span and record `business_analytics_subquery_duration_seconds` with label `subquery=...`,
    - On success/exception: record `business_analytics_overall_metrics_total` with `status=success|error` and end span
      with appropriate status.
- Do not duplicate L1/L2 metrics; only business-level indicators and subsequent breakdown.

--- 

## 7. Prometheus Integration

### 7.1 Scrape Endpoint

- **Where**: Expose Micrometer's Prometheus endpoint in the **application that runs the payment-example-v2** (the same
  process that has L1 + L2 + L3)
- **Path**: Standard is `/actuator/prometheus` (Spring Boot Actuator + `micrometer-registry-prometheus')
- **Config**: Ensures all three layers use the same `MeterRegistry` (and, if applicable, the same `Tracer`) so one
  scrape collects L1, L2, L3 metrics.

### 7.2 Naming Conventions

- **Prefix**:
    - L1: `persistence_common_`
    - L2: `persistence_sharding_`
    - L3: `business_analytics_`
- **Units**: Prefer base units (seconds, total count). Use `_seconds` for durations, `_total` for counters where it
  clarifies.
- **Labels**: Use snake_case; keep cardinality low (e.g., entity name, operation, status, routing type).

### 7.3 Example Prometheus Queries

- L1: `rate(persistence_common_query_total{operation="query"}[5m])`
- L2: `histogram_quantile(0.99, rate(persistence_sharding_query_duration_seconds_bucket{routing="multi_shard"}[5m]))`
- L3: `rate(business_analytics_overall_metrics_total[5m])`, `business_analytics_overall_metrics_duration_seconds_max`

--- 

## 8. Grafana Dashboards

### 8,1 Dashboard per layer

| Dashboard         | Data Source | Panels (examples)                                                                                                                                        |
|-------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| **L1 – SQL/DB**   | Prometheus  | Query rate by operation/entity; p50/p95/p99 `persistence_common_query_duration_seconds`; pool active/idle; transaction duration; error rate by operation |
| **L2 – Sharding** | Prometheus  | Query rate by single vs multi shard; merge duration; shards queried per logical table; L2 error rate by reason                                           |
| **L3 – Business** | Prometheus  | `getOverallMetrics` request rate and latency (p50/p95/p99); subquery duration breakdown (by `subquery` label); success vs error count                    |

### 8.2 Cross-Layer Dashboard: “getOverallMetrics” Flow

- **One row per layer:** L3 → L2 → L1.
- **Panels:** L3 overall timer; L3 subquery timers; L2 sharding timer and shard count; L1 query count and duration from
  the same time window.
- **Goal:** Correlate slow `getOverallMetrics` with either “many subqueries”, “slow merge”, or “slow DB” using the same
  time range and, if possible, trace ID (if Grafana is wired to the same trace backend).

### 8.3 Tracing in Grafana

- If using Grafana Tempo (or Jaeger/Zipkin) with the same trace backend:
    - Use **trace ID** from logs or from a custom metric/label to jump from a slow request (e.g. high
      `business_analytics_overall_metrics_duration_seconds`) to the full trace: L3 span → L2 spans → L1 spans → SQL.

---

## 9. Span and Trace Summary

- **One trace** per `getOverallMetrics` request.
- **Span hierarchy (conceptual):**
    - `getOverallMetrics` (L3)
        - `analytics.count_orders` (L3) → `sharding.query` (L2) → `persistence.query` (L1) × N shards
        - `analytics.count_payments` (L3) → …
        - `analytics.calculate_total_revenue` (L3) → …
        - … (same pattern for each subquery)
- **Context propagation:** Ensure trace context is propagated from the HTTP/entry point through L3 → L2 → L1 and into
  any async or thread-pool usage so all spans share the same trace ID.

---

## 10. Implementation Checklist

### persistence-common

- [ ] Add dependency: Micrometer Core, Micrometer Prometheus, Micrometer Tracing (or OpenTelemetry).
- [ ] Inject `MeterRegistry` and `Tracer`; instrument `QueryService` (and related APIs) with timers, counters, and
  spans.
- [ ] Optionally expose connection-pool metrics under `persistence_common_*`.
- [ ] Document required env/config for registry and tracer (e.g. no-op if not provided).

### persistence-sharding

- [ ] Add same tracing/metrics dependencies; ensure compatibility with common’s `Tracer`.
- [ ] In sharding-aware facade: create L2 spans and record `persistence_sharding_*` metrics (routing, merge, shard
  count).
- [ ] Optionally integrate with ShardingSphere events/hooks for accurate merge and shard counts.

### payment-example-v2 (Business)

- [ ] Add Spring Boot Actuator + `micrometer-registry-prometheus`; expose `/actuator/prometheus`.
- [ ] In `BusinessAnalyticsService.getOverallMetrics()`: root span, root timer, per-subquery spans and timers, overall
  and subquery counters.
- [ ] Ensure one `MeterRegistry` and one `Tracer` are used across the app (default in Spring Boot when dependencies are
  present).

### Operations

- [ ] Configure Prometheus to scrape `/actuator/prometheus`.
- [ ] Create Grafana data source for Prometheus (and trace backend if used).
- [ ] Create the three layer-specific dashboards and the “getOverallMetrics” flow dashboard.
- [ ] (Optional) Configure sampling for tracing in production (e.g. 10% or by error).

---

## 11. Different Focused Indicators – Summary

| Layer             | Primary focus                           | Example indicators                                                                         |
|-------------------|-----------------------------------------|--------------------------------------------------------------------------------------------|
| **L1 – Common**   | DB health, query cost, connection usage | Query latency, query count by operation/entity, pool usage, transaction duration           |
| **L2 – Sharding** | Shard routing and merge cost            | Single vs multi-shard latency, merge duration, shards queried per request, sharding errors |
| **L3 – Business** | Use-case success and cost               | `getOverallMetrics` latency and rate, subquery breakdown, success/error rate               |

This keeps each layer’s metrics focused and avoids duplication while allowing Grafana and tracing to correlate behavior
across the full stack.







