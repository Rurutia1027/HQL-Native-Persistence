# persistence-sharding 到底缺什么？（简要说明）

用最直白的方式说明：**persistence-sharding 模块哪里不完备、缺了什么东西**。

---

## 一、缺的东西总览（4 点）

| # | 缺什么 | 在哪 | 后果 |
|---|--------|------|------|
| 1 | **分片键没有真正参与查询** | `ShardingAwareQueryServiceImpl.findObjectByIdWithShardingKey` | 按「ID + 分片键」查单条时，路由不对，可能扫全部分片 |
| 2 | **DataSource 没有传给 PersistenceService** | `ShardingSphereDataSourceConfig.queryService()` | 用 `queryByJdbc` 会空指针 |
| 3 | **没有提供 ShardingAwareQueryService 的 Bean** | `ShardingSphereDataSourceConfig` | 每个应用都要自己 `new ShardingAwareQueryServiceImpl` |
| 4 | **getShardInfo 返回的表名不对** | `ShardingAwareQueryServiceImpl.getShardInfo` | 返回的是 `t_0` 这种，真实表名是 `t_order_0` 这种，对不上 |

下面逐条说清楚。

---

## 二、第 1 点：分片键没有真正参与查询（逻辑错误）

**位置：** `ShardingAwareQueryServiceImpl.java` 里的 `findObjectByIdWithShardingKey`。

**方法本意：**  
根据「实体 ID + 分片键」（比如订单 ID + 用户 ID）查一条记录，并且**只打一个分片**，这样路由正确、性能好。

**现状：**  
代码里生成的 HQL 是：

```text
from Order where id = :id
```

参数只传了 `id`，**没有把分片键（例如 userId）放进 WHERE**。  
ShardingSphere 是按「分片列是否在 WHERE 里」来路由的。没有 `userId`，它就不知道打哪个分片，可能**查所有分片**或路由错误。

**所以缺的是：**  
在 HQL 里加上分片键条件，例如：

```text
from Order where id = :id and userId = :shardingKey
```

并且把 `shardingKey` 传进参数里。  
要做到这点，需要知道「这个实体用哪个字段做分片」（例如 `userId`）。可以：

- 让调用方多传一个参数：**分片列名字**（如 `"userId"`），通用实现里用这个列名拼 HQL；或  
- 在通用层只提供「按 ID + 分片列名 + 分片值」的通用方法，不自动推断每个实体的分片列。

**总结：** 缺的是「**真正把分片键写进查询**」的逻辑，目前是**少写了一个 AND 条件**。

---

## 三、第 2 点：DataSource 没有传给 PersistenceService（配置不完整）

**位置：** `ShardingSphereDataSourceConfig` 里创建 `QueryService` 的地方：

```java
@Bean
public QueryService queryService(SessionFactory sessionFactory) {
    return new PersistenceService(sessionFactory);  // 只传了 sessionFactory
}
```

**现状：**  
`PersistenceService` 里有一个 `DataSource` 字段，在 `queryByJdbc()` 里会用到（拿连接）。但这里只用了 `new PersistenceService(sessionFactory)`，**没有把 ShardingSphere 的 DataSource 设进去**，所以那个字段是 null。

**后果：**  
任何地方一旦调用 `queryByJdbc(...)`，就会在 `dataSource.getConnection()` 那里 **空指针**。

**所以缺的是：**  
在创建 `QueryService` 时，把当前的 ShardingSphere `DataSource` 也传给 `PersistenceService`（例如用 `PersistenceService(sessionFactory, dataSource)` 或先 new 再 `setDataSource(dataSource)`）。  
这样 `queryByJdbc` 和以后做连接池监控（如 HikariCP 指标）才有着落。

**总结：** 缺的是「**把 DataSource 注入到 PersistenceService**」这一步配置。

---

## 四、第 3 点：没有提供 ShardingAwareQueryService 的 Bean（使用不方便）

**位置：** `ShardingSphereDataSourceConfig` 里只有 `DataSource`、`SessionFactory`、`QueryService`、`TransactionManager` 的 Bean，**没有** `ShardingAwareQueryService`。

**现状：**  
想用「带分片感知」的接口（例如按 ID+分片键查、按分片键列表查），应用要自己写：

```java
ShardingAwareQueryService shardingAwareQueryService = new ShardingAwareQueryServiceImpl(queryService);
```

每个用 persistence-sharding 的应用都要重复这段。

**所以缺的是：**  
在 persistence-sharding 的配置里加一个 Bean，例如：

```java
@Bean
public ShardingAwareQueryService shardingAwareQueryService(QueryService queryService) {
    return new ShardingAwareQueryServiceImpl(queryService);
}
```

这样应用直接 `@Autowired ShardingAwareQueryService` 就行，不用自己 new。

**总结：** 缺的是「**在模块里暴露 ShardingAwareQueryService 的 Bean**」。

---

## 五、第 4 点：getShardInfo 返回的表名不对（API 容易误导）

**位置：** `ShardingAwareQueryServiceImpl.getShardInfo`。

**现状：**  
返回的 `tableName` 是 `"t_" + tableIndex`，例如 `t_0`、`t_1`。  
但真实分片表名一般是「逻辑表名 + 下标」，例如 `t_order_0`、`t_order_1`。  
所以拿到的 `tableName` 和实际表名对不上，调用方会困惑。

**所以缺的是：**  
要么：

- 让方法多一个参数「逻辑表名」，返回 `逻辑表名 + "_" + tableIndex`（如 `t_order_0`）；或  
- 在文档里明确写清楚：**返回的只是表下标/后缀，完整表名要调用方自己用「逻辑表名 + 下标」拼**。

**总结：** 缺的是「**要么改 API 返回真实表名，要么在文档里说明当前返回的含义**」。

---

## 六、对照表：缺什么 vs 补上会怎样

| 缺什么 | 补上之后 |
|--------|----------|
| 分片键没进 HQL | `findObjectByIdWithShardingKey` 能正确路由到单个分片，不会误扫全部分片 |
| DataSource 没注入 PersistenceService | `queryByJdbc` 不会 NPE；后续可以接 HikariCP 等连接池监控 |
| 没有 ShardingAwareQueryService Bean | 应用不用自己 new，直接注入即可 |
| getShardInfo 表名不对/没说明 | 调用方要么拿到真实表名，要么清楚知道要自己拼表名 |

---

## 七、和之前英文文档的对应关系

- **SHARDING-MODULE-SCAN-REPORT.md**：详细扫描报告，里面「2.1、2.2、2.3」和「HikariCP」部分，对应的就是上面这 4 点。
- **SHARDING-COMMON-SCOPE-AND-LIMITS.md**：说明为什么不做「全量」分片支持，只做「配置 + 薄封装」；上面第 1 点的修法（传分片列名、拼 HQL）就是那种薄封装。
- **SHARDING-MODULE-SCAN-REPORT.md 里的 TODO List**：按那个列表做，就会把上面 4 点都补上（文档任务 + 代码任务）。

如果你希望，我可以再按「先做哪一条、后做哪一条」排一个最小可做的顺序，方便你一步步改。
