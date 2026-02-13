# Sharding Common: Why Full Support Is Hard and a Lean Scope

Implementing **full** sharding support inside the persistence-sharding common module is hard. This doc explains why and proposes a **lean scope** so the module stays maintainable and clear about what it does and does not do.

---

## 1. Why “full” sharding in a common module is hard

- **ShardingSphere already does the heavy work.** Routing, SQL rewrite, merge, and execution are inside ShardingSphere. The common module only needs to: create the DataSource from YAML, build Hibernate on top, and expose a QueryService. Pushing “full” logic into the common layer duplicates or fights ShardingSphere’s model (e.g. per-table rules, algorithm config).

- **Entity-specific knowledge.** Correct single-shard routing (e.g. “find by ID + sharding key”) needs the **sharding column name per entity** (e.g. `user_id`, `shop_id`). That varies by table and by app. A generic common module either has to introduce a registry/annotation/convention for every entity, or accept a parameter every time (e.g. sharding column name). Both add API surface and complexity.

- **Table naming and topology.** Physical table names (e.g. `t_order_3`) and topology (how many DBs, how many tables per DB) are defined in ShardingSphere YAML and can differ per environment. A common `getShardInfo` that returns “real” table names would have to mirror that config or read it at runtime—again, either complex or duplicated.

- **Different apps, different needs.** Some apps only need “transparent” sharding (HQL with sharding key in WHERE; ShardingSphere routes). Others need explicit shard info, admin tools, or cross-shard aggregation. A single “full” abstraction in the common module tends to either do too much or stay vague and half-correct (like `findObjectByIdWithShardingKey` without the key in the query).

So **full** support in the sharding common layer is hard; a **bounded, clear scope** is more realistic.

---

## 2. What the sharding common module can realistically own

A lean scope that is easier to implement and maintain:

| Responsibility | Owner | Notes |
|----------------|--------|--------|
| Create ShardingSphere DataSource from YAML | **persistence-sharding** | Already there. Keep it. |
| Build Hibernate SessionFactory on that DataSource | **persistence-sharding** | Already there. |
| Expose QueryService (and optionally pass DataSource into PersistenceService) | **persistence-sharding** | Small wiring change. |
| Provide ShardingAwareQueryService as an optional bean | **persistence-sharding** | Add one bean; implementation can stay thin. |
| Custom sharding algorithms (e.g. DBHashMod, TableHashMod) | **persistence-sharding** | Already there; ShardingSphere calls them. |
| **Routing of every query** | **ShardingSphere** | Apps write HQL that includes the sharding column where needed. |
| **Single-shard “find by ID + sharding key”** | **App or thin helper** | Either app writes `from Order where id = :id and userId = :userId`, or a helper that takes **sharding column name + value** and builds that HQL. No need for the common module to “know” every entity’s sharding column. |
| **Shard info (which DB/table for a key)** | **App or optional util** | Can stay a small util (e.g. `ShardingUtil`) with explicit params; no need for full logical table names in the common API if that requires config duplication. |

So: the **common module** does **config, wiring, and optional thin utilities**. It does **not** try to own entity-specific routing rules or full “shard info” that depends on every app’s table layout.

---

## 3. Concretely: what to implement vs what to document

**Implement in persistence-sharding (limited surface):**

- Wire ShardingSphere DataSource into PersistenceService (so `queryByJdbc` and metrics don’t NPE).
- Register a `ShardingAwareQueryService` bean that delegates to QueryService.
- Optionally: one **explicit** helper, e.g. `findByIdAndShardingKey(Class, String id, String shardingColumnName, Long shardingKey)` that builds HQL `where id = :id and <shardingColumnName> = :shardingKey` and runs it. No convention, no registry—caller passes the column name. That fixes the current bug without the module “knowing” all entities.
- Document that `getShardInfo` returns indices / a suffix only; callers compose with their logical table name if needed.

**Document as app responsibility:**

- “For single-shard routing, ensure the sharding column is in the WHERE clause.” (So either use the helper above or write HQL yourself.)
- “ShardingSphere YAML defines actual data nodes and table names; the common module does not duplicate that.”
- “When to use QueryService vs ShardingAwareQueryService”: transparent HQL → QueryService; explicit by-id+shard key → optional helper or ShardingAwareQueryService with clear semantics.

That way the **sharding common** does not aim for “full” sharding support; it aims for **correct, minimal support** plus clear boundaries so apps know what they must do.

---

## 4. Summary

- **Full** sharding support in the sharding common module is **hard** because of entity-specific config, table topology, and ShardingSphere already doing the real work.
- A **lean scope** is easier: the module owns **DataSource + Hibernate + QueryService wiring**, optional **ShardingAwareQueryService** bean, and a **thin, explicit** helper for “find by ID + sharding key” (with column name as parameter). Routing and topology stay in ShardingSphere and app code.
- Document what the module **does** and **does not** do so that “full” support is not expected from the common layer; the rest stays in ShardingSphere and in the application.
