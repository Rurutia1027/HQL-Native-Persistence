# Persistence Sharding (TiDB Native) – WIP

This module is being redesigned to provide **TiDB-native sharding support** for the `persistence-common` module, *
*without Apache ShardingSphere**.

Instead of application-side routing and multiple physical data sources, the new design will:

- Connect to a **single TiDB cluster** (one JDBC URL).
- Leverage **TiDB’s internal sharding and partitioning** to distribute data.
- Keep using **HQL / `HqlQueryBuilder` / `QueryService`** from `persistence-common`.

For the detailed design and implementation plan, see:

- `[TIDB_SHARDING_SOLUTION.md](https://github.com/Rurutia1027/HQL-Native-Persistence/issues/13)` (to be used as the
  primary design document)