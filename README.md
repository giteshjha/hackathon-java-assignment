# Java Hackathon Assignment - Fulfillment Management System

A production-grade fulfillment management system built with **Quarkus 3.13.3**, following **Hexagonal Architecture** (Ports & Adapters) across all three bounded contexts: Warehouses, Stores, and Products.

## Quick Start

```bash
# Start in dev mode (auto-reloads on code change)
./mvnw quarkus:dev

# Open the UI
open http://localhost:8080/index.html

# Open Swagger UI (API explorer)
open http://localhost:8080/q/swagger-ui

# Open Prometheus metrics
open http://localhost:8080/q/metrics
```

## Running Tests

```bash
# Run all tests + JaCoCo coverage gate (80% minimum)
./mvnw verify

# Run tests only
./mvnw test

# Run a specific test class
./mvnw test -Dtest=ArchiveWarehouseUseCaseUnitTest
```

**Current state:** 65 tests, 0 failures, JaCoCo 80% gate passing.

## Architecture

All three bounded contexts follow **Hexagonal Architecture**:

```
<context>/
  domain/
    models/       ← pure POJOs, no framework annotations
    ports/        ← driving ports (use case interfaces) + driven ports (repository/gateway interfaces)
    usecases/     ← business logic, depends only on ports
  adapters/
    database/     ← JPA entities (DbXxx), repository implementations
    restapi/      ← JAX-RS resource classes (thin, delegates to use cases)
```

- **Warehouse** — OpenAPI-generated REST contract (`warehouse-openapi.yaml`)
- **Store** — hand-coded REST, CDI events for post-commit legacy sync
- **Product** — hand-coded REST, use cases enforce stock constraints

## Key Business Rules

- Product `stock` is the **sum** of all allocations across stores and warehouses
- Adding/updating a product in a store or warehouse updates `product.stock` **transactionally**
- Warehouse capacity and location limits are enforced on create/replace
- Archived warehouses are excluded from search results and cannot be replaced
- Legacy store gateway is notified **after** transaction commit (CDI `AFTER_SUCCESS`)

## Observability

- **Prometheus metrics:** `http://localhost:8080/q/metrics` (Micrometer counters for warehouse/store/product operations)
- **Health checks:** `http://localhost:8080/q/health`
- **OpenAPI spec:** `http://localhost:8080/q/openapi`

## Running in JVM Mode (with PostgreSQL)

```bash
./mvnw package

docker run -it --rm --name quarkus_pg \
  -e POSTGRES_USER=quarkus_test \
  -e POSTGRES_PASSWORD=quarkus_test \
  -e POSTGRES_DB=quarkus_test \
  -p 15432:5432 postgres:13.3

java -jar ./target/quarkus-app/quarkus-run.jar
```

Navigate to <http://localhost:8080/index.html>

---

Read [BRIEFING.md](BRIEFING.md) for domain context and [CODE_ASSIGNMENT.md](CODE_ASSIGNMENT.md) for the original assignment tasks.
