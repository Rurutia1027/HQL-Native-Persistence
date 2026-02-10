# Example-1: Payment Example via Persistence-Common

## Overview

This is a sample e-commerce payment system module based on the `persistence-common` module. It demonstrates how to
perform database operations using `HQLQueryBuilder`.

**Note**: This module only uses `persistence-common` and does not include database sharding. Sharding support will be
implemented in another folder.

## Module Structure

```
payment-examples-v1/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/tus/payment/
│   │   │       ├── entity/          # Entity classes
│   │   │       ├── service/         # Service layer (using HQLQueryBuilder)
│   │   │       └── config/          # Spring configuration
│   │   └── resources/
│   └── test/
│       └── java/
│           └── org/tus/payment/
│               ├── integration/     # Integration tests
│               └── config/          # Test configuration
└── pom.xml
```

## Entities

Based on the design in [SCHEMA_ECOMMERCE_PAYMENT.md](todo), the following entities are include:

- **User** - User table
- **Order** - Order table
- **OrderItem** - Order item table
- **Payment** - Payment table
- **PaymentRecord** - Payment record table (audit log)
- **Refund** - Refund table
- **AccountBalance** - Account balance table
- **AccountTransaction** - Account transaction ledger 

All entities inherit from `PersistedObject`, which provides: 
- Soft delete support 
- Version control (optimistic locking)
- Timestamps (creation time and last modified time)

---

## Service Layer Example 
### OrderService 

Query order using `HQLQueryBuilder`:

```java
public List<Order> getUserOrders(Long userId, Integer orderStatus) {
    HqlQueryBuilder builder = new HqlQueryBuilder();
    builder.fromAs(Order.class, "o")
           .select("o")
           .eq("o.userId", userId)
           .isNull("o.deleted"); // Soft delete filter
    
    if (orderStatus != null) {
        builder.and().eq("o.orderStatus", orderStatus);
    }
    
    builder.orderBy("o.createdDate", false);
    
    String hql = builder.build();
    Map<String, Object> params = builder.getInjectionParameters();
    builder.clear();
    
    return queryService.query(hql, params);
}
```

### PaymentService

Query payments using `HQLQueryBuilder`: 

```java
public Payment getPaymentByOrderId(String orderId, Long userId) {
    HqlQueryBuilder builder = new HqlQueryBuilder();
    builder.fromAs(Payment.class, "p")
           .select("p")
           .eq("p.orderId", orderId)
           .eq("p.userId", userId)
           .isNull("p.deleted")
           .orderBy("p.createdDate", false);
    
    String hql = builder.build();
    Map<String, Object> params = builder.getInjectionParameters();
    builder.clear();
    
    List<Payment> payments = queryService.query(hql, params);
    return payments.isEmpty() ? null : payments.get(0);
}
```

## Running Tests 
### Prerequisites 
- Docker must be running (used by Testcontainers)
- Ensure the `persistence-common` module has been installed into the local Maven repository 

### Install Dependencies
```bash 
# First install the persistence-common module
cd ../persistence-common
mvn clean install -DskipTests

# Then return to payment-example
cd ../payment-example
mvn clean compile
```

### Run Integration Tests 

```bash
# Run all tests
mvn verify

# Run integration tests only
mvn package -DskipTests
mvn failsafe:integration-test

# Run a specific test
mvn failsafe:integration-test -Dit.test=OrderServiceIT 
```

## Design Features

- Type Safety: Uses HQLQueryBuilder to construct type-safe queries
- Soft Delete Support: All queries automatically filter deleted records
- Optimistic Locking: Version fields prevent concurrent update conflicts
- Audit Logging: PaymentRecord tracks all payment status changes
- Account Ledger: AccountTransaction records all account changes