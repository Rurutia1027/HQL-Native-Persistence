# How shardingColumnName and shardingKey Reach the Target Table

This doc traces how the values we pass in our API (sharding column name + sharding key) flow through Hibernate → SQL → ShardingSphere → **database sharding** and **table sharding** algorithms → final physical table.

---

## 1. What we have in the config (YAML)

For a table like `t_user_coupon` (or `t_orders` in payment-example-v2), the sharding rule looks like:

```yaml
t_user_coupon:
  actualDataNodes: ds_${0..1}.t_user_coupon_${0..31}
  databaseStrategy:
    standard:
      shardingColumn: user_id
      shardingAlgorithmName: user_coupon_database_mod
  tableStrategy:
    standard:
      shardingColumn: user_id
      shardingAlgorithmName: user_coupon_table_mod
```

- **shardingColumn** = the **database column name** ShardingSphere will look for in the SQL (e.g. `user_id`).
- **databaseStrategy** uses **DBHashModShardingAlgorithm** → picks **which database** (ds_0 or ds_1).
- **tableStrategy** uses **TableHashModShardingAlgorithm** → picks **which table** (t_user_coupon_0 … t_user_coupon_31).

So: one column (`user_id`), one value from the SQL; that value is fed into **both** algorithms.

---

## 2. What we do in our code (ShardingAwareQueryServiceImpl)

We build HQL and params:

```java
String hql = "from Order where id = :id and userId = :shardingKey";
params.put("id", orderId);
params.put("shardingKey", 1001L);
delegate.querySingle(hql, params, null);
```

- **shardingColumnName** = `"userId"` → **Java property** name (for HQL).
- **shardingKey** = `1001L` → the **value** we put in the params.

So we only build the HQL and the map; we don’t talk to ShardingSphere or the algorithms directly.

---

## 3. Step-by-step: from our call to the target table

### Step 1: HQL → SQL (Hibernate)

- Hibernate turns the HQL into SQL.
- The entity maps Java `userId` to **DB column** `user_id` (e.g. `@Column(name = "user_id")`).
- So the SQL looks like:
  - `SELECT ... FROM t_orders WHERE id = ? AND user_id = ?`
  - with bindings: `... , 1001` for the second placeholder.

So in the final SQL there is a condition on the **column** `user_id` with **value** `1001`. That column name and value are what ShardingSphere needs.

### Step 2: SQL is sent to the DataSource (ShardingSphere)

- Our app uses a **DataSource** that is actually ShardingSphere’s proxy.
- Every query goes to ShardingSphere, not directly to a single DB.

### Step 3: ShardingSphere parses the SQL and finds the logical table

- From the SQL (e.g. `FROM t_orders`), ShardingSphere knows the **logical table** (e.g. `t_orders`).
- It looks up the **sharding rules** for that table in the YAML.
- For `t_orders` it finds:
  - `databaseStrategy.standard.shardingColumn` = **user_id**
  - `tableStrategy.standard.shardingColumn` = **user_id**

So ShardingSphere knows: “for this table, the sharding column is **user_id**”.

### Step 4: ShardingSphere extracts the sharding value from the SQL

- It looks at the WHERE clause for the column **user_id**.
- It finds `user_id = 1001` (or the bound value for that placeholder).
- So it gets **sharding value = 1001**.

That value is the same as the **shardingKey** we passed in our API; it just came from the SQL instead of from our map.

### Step 5: Database sharding (DBHashModShardingAlgorithm)

- ShardingSphere calls:
  - **DBHashModShardingAlgorithm.doSharding**(availableTargetNames = [ds_0, ds_1], **PreciseShardingValue(columnName = "user_id", value = 1001)**).
- The algorithm uses only the **value** (1001): it does something like `hash(1001) % shardingCount` and maps that to one of the databases.
- It returns e.g. **"ds_0"**.

So: **which DB** = result of **DBHashModShardingAlgorithm** with the **sharding key value** (1001).

### Step 6: Table sharding (TableHashModShardingAlgorithm)

- ShardingSphere calls:
  - **TableHashModShardingAlgorithm.doSharding**(availableTargetNames = [t_orders_0, t_orders_1, ...], **PreciseShardingValue(columnName = "user_id", value = 1001)**).
- Again the algorithm uses the **value** (1001) and does hash mod over the table list.
- It returns e.g. **"t_orders_5"**.

So: **which table** = result of **TableHashModShardingAlgorithm** with the **same sharding key value** (1001).

### Step 7: Rewrite SQL and execute

- ShardingSphere rewrites the SQL so the logical table name is replaced by the physical one (e.g. `t_orders` → `t_orders_5`).
- It gets a connection to **ds_0** (from step 5) and runs the SQL on **ds_0.t_orders_5**.
- The result is returned to the app.

So the “target table” is: **ds_0** + **t_orders_5** = **ds_0.t_orders_5**.

---

## 4. How our parameters relate to this

| What we pass | Role in the flow |
|--------------|-------------------|
| **shardingColumnName** (e.g. `"userId"`) | Used only in **our** HQL. Hibernate maps it to the **DB column name** (e.g. `user_id`). That column name must match **shardingColumn** in the YAML so ShardingSphere can find the value in the SQL. |
| **shardingKey** (e.g. `1001L`) | Becomes the bound value in SQL (`user_id = 1001`). ShardingSphere **extracts** this value and passes it to **both** algorithms. So the same value is used for **database** sharding and **table** sharding. |

So:

- **shardingColumnName** → ensures the generated SQL has the **right column** (user_id), which matches YAML and is used to extract the value.
- **shardingKey** → is the **value** that ends up in the SQL and is then used by **DBHashMod** (pick DB) and **TableHashMod** (pick table) to reach the final **ds_X.table_Y**.

---

## 5. One picture (short)

```
Our API:  findObjectByIdWithShardingKey(Order.class, id, "userId", 1001L)
              ↓
HQL:      from Order where id = :id and userId = :shardingKey   +  params { id, 1001L }
              ↓
Hibernate →  SQL:  SELECT ... FROM t_orders WHERE id = ? AND user_id = 1001
              ↓
ShardingSphere:  logical table = t_orders,  shardingColumn (from YAML) = user_id
              ↓
ShardingSphere:  extracts value 1001 from WHERE user_id = 1001
              ↓
DBHashModShardingAlgorithm.doSharding(..., value=1001)  →  "ds_0"
TableHashModShardingAlgorithm.doSharding(..., value=1001)  →  "t_orders_5"
              ↓
Execute SQL on  ds_0.t_orders_5  →  result
```

So: **sharding column name** (and its mapping to DB column) + **sharding key value** in the SQL are what ShardingSphere uses to run the two algorithms and attach the query to the **target database and target table**.
