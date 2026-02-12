# Payment Example V2 - Complex Sharding Scenarios

## Overview

Payment Example V2 is an e-commerce payment system example built on top of the `persistence-common` and
`persistence-sharding` modules. It demonstrates **complex cross-module and cross-database sharding scenarios**, include
database-level and table-level sharding.

## Core Features

### Multi-Module Database Architecture

### Complex Business Scenarios

### Sharding Strategy

- Database-level sharding: 2 database shards (`ds_ecommerce_0/1`, `ds_payment_0/1`)
- Table-level sharding: each table is sharded based on business needs (16-64 shards)
- Sharding key: `user_id` (user-dimension sharding)

## Architecture Design

### Database Architecture

```

```

## Sharding Configuration

### E-Commerce Module Tables

#### Table: `t_orders`

- Database Shards: 2 (`ds_ecommerce_0/1`)
- Table Shards: 16 per DB
- Description: Order table, sharded by `user_id`

#### Table: `t_order_items`

- Database Shards: 2 (`ds_ecommerce_0/1`)
- Table Shards: 16 per DB
- Description: Order item table, aligned with order sharding

### Payment Module Table

#### Table: `t_payments`

- Database Shard: 2 (`ds_payment_0/1`)
- Table Shards: 16 per DB
- Description: Payment table, sharded by `user_id`

#### Table: `t_payment_records`

- Database Shard: 2 (`ds_payment_0/1`)
- Table Shards: 16 per DB
- Description: Payment record table, aligned with payment sharding

#### Table: `t_refunds`

- Database Shard: 2 (`ds_payment_0/1`)
- Table Shards: 8 per DB
- Description: Refund table, sharded by `user_id`

#### Table: `t_account_balance`

- Database Shard: 2 (`ds_payment_0/1`)
- Table Shards: 1 (no table sharding)
- Description: Account balance table

#### Table: `t_account_transaction`

- Database Shard: (`ds_payment_0/1`)
- Table Shards: 32 per DB
- Description: Account transaction table, sharded by `user_id`

---

## Complex Business Scenarios

### Cross-Shard Aggregation Queries

**Scenarios**: Calculate platform-wide business metrics (total orders, total payment amount, payment method
distribution, etc.)

**Characteristics**:

- Query condition **do not include the sharding key (user_id)**
- ShardingSphere queries **all shards** and merges results
- Suitable for analytics and reporting use cases

**Example**:

```java
// Calculate platform-wide order count and total payment amount 

BusinessMetriics metrics = analyticsService.getOverallMetrics(startDate, endDate);

// ShardingSphere queries all 4 database shards and merges the aggregated results 
```

**Performance Considerations**:

- Cross-shard queries across all shards and are slower
- Prefer asynchronous processing or caching
- For low real-time requirements, use scheduled pre-computation

### Single-Shard Optimized Queries

**Scenario**: Query order and payment data for a specific user

**Characteristics**:

- Query conditions include the sharding key (`user_id`)
- ShardingSphere routes to a **single shard**
- Optimal performance, suitable for high-frequency queries

Example:

```java
// Query user-specific metrics (single shard)
BusinessMetrics userMetrics = analyticsService.getUserMetrics(userId, startDate, endDate);
// Routed to a single shard based on userId

```

### Cross Module Data Association

**Scenario**: Orders and payments reside in different database modules but must be queried together

**Design Strategies**:

- Application-level join (recommended): Query order first, then payment
- Data redundancy: Store payment status redundantly in order table (already implemented)
- Event-driven synchronization: Use message queues for data synchronization (for non-real-time requirements)

Example:

```java
// 1. Query order (E-Commerce Module)
Order order = queryService.findObjectById(Order.class, orderId, userId);

// 2. Query payment (Payment Module, same userId ensures same shard index logic)
Payment payment = queryService.findObjectById(Payment.class, paymentId, userId);

// 3. Assemble result at application layer 
OrderWithPayment result = new OrderWithPayment(order, payment); 
```

## Performance Optimization Recommendations

### Query Optimization

- Always include the sharding key (`user_id`) in high-frequency queries
- Avoid cross-shard queries in latency-sensitive scenarios
- Cache cross-shard aggregation results (e.g., Redis)

### Data Redundancy

- Store payment status in order table
- Store product information in order items
- Store order information in payment table for fast lookup

### Asynchronous Processing

- Use scheduled jobs for metrics computation
- Use message queues for cross-module synchronization 