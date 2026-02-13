# findObjectByIdWithShardingKey — Parameter Examples

The 4-arg method is:

```java
<T> T findObjectByIdWithShardingKey(Class<T> clazz, String id, String shardingColumnName, Long shardingKey);
```

Here’s what each parameter is and how to pass it.

---

## 1. What is the “sharding key value”?

- **Sharding key** = the column your table is sharded by (e.g. `user_id`, `shop_id`).
- **Sharding key value** = the **actual value** of that column for the row you want.

So:
- **`shardingColumnName`** = Java property name of that column (e.g. `"userId"`, `"shopId"`).
- **`shardingKey`** = the value (e.g. `1001L`, `42L`).

---

## 2. Example 1: Order (sharded by user)

Table: `t_orders`, sharded by **user_id**.  
You want to load one order by its **order ID** (string) and **user ID** (so ShardingSphere hits a single shard).

- **Entity:** `Order` (has `orderId`, `userId`, …).
- **Row identity:** `id` = primary key (e.g. UUID), and you know `userId = 1001L`.

**Call:**

```java
String orderId = "ord-abc-123";   // the entity's primary key (id)
Long userId = 1001L;              // the sharding key value for this order

Order order = shardingAwareQueryService.findObjectByIdWithShardingKey(
    Order.class,
    orderId,           // id: primary key of the entity
    "userId",          // shardingColumnName: Java property name (Order has getUserId())
    1001L              // shardingKey: the actual user_id value for this order
);
```

- **`id`** = primary key of the row (e.g. `orderId` or whatever your `Order` uses as `id`).
- **`shardingColumnName`** = `"userId"` (the **Java** field name on `Order`, not the DB column name `user_id`).
- **`shardingKey`** = `1001L` = the **value** of `user_id` for that order.

So: “Find the Order whose id is `ord-abc-123` and whose userId is `1001L`.” ShardingSphere uses `userId = 1001L` to route to one shard.

---

## 3. Example 2: Shop coupon (sharded by shop)

Table: `t_shop_coupon`, sharded by **shop_id**.  
Load one coupon by coupon ID and shop ID.

- **Entity:** `ShopCoupon` (has `couponId`, `shopId`, …).
- You know: coupon id = `"cp-xyz"`, shop id = `42L`.

**Call:**

```java
ShopCoupon coupon = shardingAwareQueryService.findObjectByIdWithShardingKey(
    ShopCoupon.class,
    "cp-xyz",     // id: primary key
    "shopId",     // shardingColumnName: Java property (ShopCoupon has getShopId())
    42L           // shardingKey: the actual shop_id value
);
```

- **`shardingColumnName`** = `"shopId"` (Java property).
- **`shardingKey`** = `42L` = the **value** of `shop_id` for that row.

---

## 4. Summary table

| Parameter              | Meaning                         | Example 1 (Order)   | Example 2 (ShopCoupon) |
|------------------------|---------------------------------|----------------------|-------------------------|
| `id`                   | Entity primary key (String)     | `"ord-abc-123"`      | `"cp-xyz"`              |
| `shardingColumnName`   | Java property name of shard col | `"userId"`           | `"shopId"`              |
| `shardingKey`          | **Value** of that column (Long) | `1001L`              | `42L`                   |

So: **sharding key value** = the Long you put in the last argument — the actual value of the sharding column for the row you are looking for (e.g. which user, which shop).
