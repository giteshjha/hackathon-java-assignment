# Pull Request: Hexagonal Architecture — Products & Stores Refactoring + Test Cleanup

**Branch:** `feature/hexagonal-architecture` → `dev`
**Commits:** 4 | **Files changed:** 51 | **Net change:** +1,052 / -1,589 lines

---

## Summary

This PR completes the architectural alignment of all three bounded contexts — **Warehouses, Stores, and Products** — to a consistent Hexagonal Architecture (Ports & Adapters) pattern. Prior to this, only `Warehouse` followed this pattern; `Store` and `Product` were flat "transaction script" style with business logic embedded in resource classes and Active Record JPA entities.

Alongside the refactoring, this PR removes 13 redundant or zero-value tests (78 → 65) while keeping the JaCoCo 80% gate fully satisfied.

---

## Changes

### 1. Full Hexagonal Refactoring — `products/` context

**Deleted** (old flat structure):
- `products/Product.java` — PanacheEntity mixing JPA and domain concerns
- `products/ProductRepository.java`
- `products/ProductResource.java` — 87-line fat resource with inline business logic

**Created** (hexagonal structure):
```
products/
  domain/
    models/Product.java                    ← pure POJO, zero framework annotations
    ports/ProductStore.java                ← driven port (persistence interface)
    ports/CreateProductOperation.java      ← driving port (use case interface)
    ports/UpdateProductOperation.java
    ports/DeleteProductOperation.java
    usecases/CreateProductUseCase.java     ← business logic only
    usecases/UpdateProductUseCase.java
    usecases/DeleteProductUseCase.java
  adapters/
    database/DbProduct.java                ← @Entity(name="Product"), JPA-only
    database/ProductRepository.java        ← implements ProductStore + PanacheRepository<DbProduct>
    restapi/ProductResource.java           ← thin JAX-RS adapter, delegates to use cases
```

**Key decisions:**
- `@Entity(name="Product")` on `DbProduct` preserves all existing JPQL queries (`from Product`, `select sp from StoreProduct sp`) without changes
- Port method is `getById()` not `findById()` to avoid return-type conflict with Panache's built-in `findById()`
- `findDbById()` adapter-layer method returns the managed JPA entity for Hibernate dirty-checking in stock mutations

---

### 2. Full Hexagonal Refactoring — `stores/` context

**Deleted:**
- `stores/Store.java` — PanacheEntity
- `stores/StoreProduct.java` → moved to `adapters/database/`
- `stores/StoreResource.java` → moved to `adapters/restapi/`

**Created:**
```
stores/
  domain/
    models/Store.java                      ← pure POJO
    ports/StoreRepository.java             ← driven port
    ports/LegacyStoreGateway.java          ← driven port (replaces concrete injection)
    ports/CreateStoreOperation.java        ← driving ports
    ports/UpdateStoreOperation.java
    ports/PatchStoreOperation.java
    ports/DeleteStoreOperation.java
    usecases/CreateStoreUseCase.java
    usecases/UpdateStoreUseCase.java
    usecases/PatchStoreUseCase.java
    usecases/DeleteStoreUseCase.java
  adapters/
    database/DbStore.java                  ← JPA entity
    database/StoreProduct.java             ← moved here, references DbStore + DbProduct
    database/StoreRepositoryAdapter.java   ← implements StoreRepository + PanacheRepository<DbStore>
    restapi/StoreResource.java             ← thin adapter
```

`LegacyStoreGateway` port introduced so `StoreEventObserver` now depends on an interface, not the concrete `LegacyStoreManagerGateway` implementation — proper dependency inversion. CDI events (`StoreCreatedEvent`, `StoreUpdatedEvent`, `StoreDeletedEvent`) now carry the domain `Store` POJO instead of the JPA entity.

---

### 3. Join Table Updates

- `stores/adapters/database/StoreProduct.java` — references `DbStore` + `DbProduct` (was `Store` + `Product`)
- `warehouses/adapters/database/WarehouseProduct.java` — references `DbProduct` (was `Product`)
- `warehouses/adapters/restapi/WarehouseProductResource.java` — uses `ProductRepository.findDbById()` for join-table stock mutations within `@Transactional` boundaries

---

### 4. Test Reduction (78 → 65 tests)

Removed 13 tests in 4 categories. JaCoCo 80% gate still passes.

| Test | Removed | Reason |
|---|---|---|
| `LocationGatewayTest` | 1 (entire file) | Body was entirely commented out — did nothing |
| `ModelConstructorsTest.generatedWarehouseBeanGettersAndSettersWork` | 1 | Tests getters/setters of auto-generated OpenAPI bean |
| `FulfillmentMetricsTest.multipleIncrementsAccumulate` | 1 | Tests Micrometer library behavior, not our code |
| `WarehouseOptimisticLockingTest.testVersionIncrementsonUpdate` | 1 | Tests JPA `@Version` feature, not our logic |
| `ArchiveWarehouseUseCaseTest` — 3 basic tests | 3 | Exact duplicates of `ArchiveWarehouseUseCaseUnitTest`; **concurrency test kept** |
| `ReplaceWarehouseUseCaseTest` — 3 basic + 3 parameterized | 6 | Exact duplicates of `ReplaceWarehouseUseCaseUnitTest`; **concurrency test kept** |

High-value tests preserved: all concurrency tests, optimistic locking end-to-end test, `StoreTransactionIntegrationTest`, `StoreProductRelationshipTest`, `WarehouseProductRelationshipTest`.

---

### 5. Test Updates (existing tests fixed for new structure)

| Test | Change |
|---|---|
| `ProductResourceUnitTest` | Rewritten to test use cases (`CreateProductUseCase`, etc.) with mocked `ProductStore`; thin resource tested separately |
| `StoreEventObserverTest` | Updated: injects `LegacyStoreGateway` interface; removed `Store.deleteAll()` Active Record call |
| `StoreTransactionIntegrationTest` | Updated: uses `stores.domain.models.Store` POJO in events |
| `StoreSupportUnitTest` | Minor import update for new package location |
| `ModelConstructorsTest` | Removed `Warehouse` (generated bean) import; kept product/store POJO tests |

---

## Invariants Preserved

- All existing JPQL queries (`from Product`, `from Store`, `from DbWarehouse`) continue to work unchanged
- `import.sql` seed data unchanged — referential integrity maintained
- All 65 tests pass, JaCoCo 80% gate met
- REST API surface unchanged — all existing endpoints behave identically

---

## Test Results

```
Tests run: 65, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
JaCoCo coverage: ≥ 80% ✅
```

---

## Architecture After This PR

All three contexts now follow the same pattern:

```
<context>/
  domain/          ← pure Java, zero framework dependencies
    models/        ← POJOs
    ports/         ← interfaces: driven (repository/gateway) + driving (use case)
    usecases/      ← business logic, depends only on ports
  adapters/
    database/      ← JPA entities (DbXxx), PanacheRepository implementations
    restapi/       ← JAX-RS thin adapters, delegate to use case ports
```

### Before vs After

| Context | Before | After |
|---|---|---|
| Warehouse | ✅ Hexagonal | ✅ Hexagonal (unchanged) |
| Store | ❌ Fat resource + PanacheEntity Active Record | ✅ Hexagonal |
| Product | ❌ Fat resource + PanacheEntity Active Record | ✅ Hexagonal |
