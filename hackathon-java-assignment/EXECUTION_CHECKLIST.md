# Execution Checklist (Current Branch + Commit Per Phase)

## Summary
This is the revised strict plan using your current development branch only. No new branch creation. Each phase ends with a dedicated commit so progress is auditable and reversible.
Phase numbering note: after Phase 5, treat the old Phase 7 workstream as Phase 6 bonus track.

## Current Status
- Phase 1 completed and committed.
- Phase 2 completed and committed.
- Phase 3 completed and committed.
- Phase 4 completed and committed.
- Phase 5 completed and committed.
- Next active phase: Phase 6 (coverage gate).

## Fixed Working Rules
- Work on current branch (`git branch --show-current` must remain unchanged).
- One commit per phase after tests/checkpoint pass.
- No force-push.
- Keep architecture/style consistent with existing Quarkus + hexagonal patterns.
- Coverage gate: JaCoCo `>=80%`.
- Code changes must be surgical and minimal; avoid verbose refactors, achieve outcomes with the smallest safe diff, and keep comments concise and purposeful.
- Follow Quarkus project structure strictly: production code only in `src/main/java`; tests only in `src/test/java`, mirroring package structure from main where applicable.

## Phase 0: Baseline + Environment
1. Run:
```bash
cd /Users/giteshkumarjha/Downloads/hackathon-java-assignment/hackathon-java-assignment
git branch --show-current
git status -sb
java -version
docker --version
chmod +x ./mvnw
MAVEN_USER_HOME=/tmp/maven-home ./mvnw clean test
MAVEN_USER_HOME=/tmp/maven-home ./mvnw test -Dtest=WarehouseConcurrencyIT,WarehouseTestcontainersIT
```
Expected checkpoints:
- Java 17+.
- Baseline failures captured.
- Surefire reports generated.

2. Evidence:
```bash
rg -n "failures=\"[1-9]|errors=\"[1-9]" target/surefire-reports/TEST-*.xml
```

## Phase 1: Warehouse Persistence + Concurrency Stabilization
Scope:
- Fix `WarehouseRepository.update` to respect optimistic locking (`@Version`), avoid bulk update lost-update behavior.
- Resolve concurrency/lost-update defects.
- Handle `remove` contract safely (implement or eliminate dead-path consistently).

Validation:
```bash
MAVEN_USER_HOME=/tmp/maven-home ./mvnw test -Dtest=WarehouseOptimisticLockingTest,ArchiveWarehouseUseCaseTest,WarehouseConcurrencyIT
MAVEN_USER_HOME=/tmp/maven-home ./mvnw test -Dtest=WarehouseConcurrencyIT
MAVEN_USER_HOME=/tmp/maven-home ./mvnw test -Dtest=WarehouseConcurrencyIT
```
Expected:
- All above pass twice consistently.

Commit:
```bash
git add .
git commit -m "Phase 1: stabilize warehouse update path and concurrency behavior"
```

## Phase 2: Minimal Runtime/Testability Hardening
Scope:
- Minimal required changes so app boots and tests run reliably.
- Ensure transaction boundaries are correct in mutation flows.

Validation:
```bash
MAVEN_USER_HOME=/tmp/maven-home ./mvnw -DskipTests quarkus:dev
```
Expected:
- App boots without startup exceptions (`Ctrl+C` to stop).

Commit:
```bash
git add .
git commit -m "Phase 2: minimal runtime and testability hardening"
```

## Phase 3: Store Transaction/Event Semantics
Scope:
- Ensure legacy sync happens only when transaction commits successfully.
- Standardize behavior with proper transactional event semantics and error handling/logging.

Validation:
```bash
MAVEN_USER_HOME=/tmp/maven-home ./mvnw test -Dtest=StoreEventObserverTest,StoreTransactionIntegrationTest
```
Expected:
- Failed store transaction does not call legacy sync.
- Observer tests pass.

Commit:
```bash
git add .
git commit -m "Phase 3: enforce post-commit store legacy synchronization"
```

## Phase 4: Missing Tests (CreateWarehouseUseCase + Gaps)
Scope:
- Implement `CreateWarehouseUseCaseTest` with positive/negative/error conditions:
  - success create
  - duplicate business unit
  - invalid location
  - capacity limit violation
  - stock > capacity
  - timestamp set
- Add any adjacent missing assertions needed for changed behavior.

Validation:
```bash
MAVEN_USER_HOME=/tmp/maven-home ./mvnw test -Dtest=CreateWarehouseUseCaseTest,WarehouseValidationTest,ReplaceWarehouseUseCaseTest
```
Expected:
- All pass; deterministic.

Commit:
```bash
git add .
git commit -m "Phase 4: add comprehensive CreateWarehouseUseCase tests"
```

## Phase 5: Complete `QUESTIONS.md`
Scope:
- Fill both answers with concrete tradeoffs and justified testing strategy.

Validation:
```bash
sed -n '1,220p' QUESTIONS.md
```
Expected:
- Both answer blocks non-empty and substantive.

Commit:
```bash
git add QUESTIONS.md
git commit -m "Phase 5: complete assignment discussion answers"
```

## Phase 6: Coverage (JaCoCo >= 80%)
Scope:
- Configure JaCoCo report + check in `pom.xml` with **project-wide** (`BUNDLE`) threshold `0.80`.

Validation:
```bash
MAVEN_USER_HOME=/tmp/maven-home ./mvnw clean test jacoco:report jacoco:check
```
Expected:
- `BUILD SUCCESS`.
- No JaCoCo rule violation.
- Report at `target/site/jacoco/index.html`.

Commit:
```bash
git add pom.xml
git add target/site/jacoco/index.html
git commit -m "Phase 6: enforce JaCoCo coverage gate at 80 percent"
```

## Phase 6 (Bonus Track): `/warehouse/search` + Integration Tests
Scope:
- Add `GET /warehouse/search` with:
  - filters: `location`, `minCapacity`, `maxCapacity`
  - sorting: `sortBy` (`createdAt` default, `capacity`), `sortOrder` (`asc` default/`desc`)
  - pagination: `page` (default 0), `pageSize` (default 10, max 100)
  - archived exclusion, AND logic
- Update OpenAPI and implementation.
- Add integration tests for filter/sort/page/error scenarios.

Validation:
```bash
MAVEN_USER_HOME=/tmp/maven-home ./mvnw test -Dtest=WarehouseEndpointIT
```
Expected:
- Search tests pass; behavior matches spec.

Commit:
```bash
git add .
git commit -m "Phase 7: implement warehouse search API with integration tests"
```

## Phase 7: Full Verification + Final Evidence
Validation:
```bash
MAVEN_USER_HOME=/tmp/maven-home ./mvnw clean test
MAVEN_USER_HOME=/tmp/maven-home ./mvnw test -Dtest=WarehouseConcurrencyIT,WarehouseTestcontainersIT,WarehouseEndpointIT
rg -n "failures=\"[1-9]|errors=\"[1-9]" target/surefire-reports/TEST-*.xml || true
```
Expected:
- No failing tests in reports.

Commit:
```bash
git add .
git commit -m "Phase 8: final verification evidence and cleanup"
```

## Phase 8: Push Current Branch to GitHub
If remote exists:
```bash
git remote -v
git push origin $(git branch --show-current)
```

If remote not set:
```bash
git remote add origin https://github.com/<username>/<repo>.git
git push -u origin $(git branch --show-current)
```

Expected:
- Push success and upstream tracking set.

## Test Matrix (must pass)
- `CreateWarehouseUseCaseTest`
- `ArchiveWarehouseUseCaseTest`
- `ReplaceWarehouseUseCaseTest`
- `WarehouseValidationTest`
- `WarehouseOptimisticLockingTest`
- `WarehouseConcurrencyIT`
- `WarehouseTestcontainersIT`
- `StoreEventObserverTest`
- `StoreTransactionIntegrationTest`
- `WarehouseEndpointIT` (including `/warehouse/search`)

## API/Type Changes to Implement
- OpenAPI update for `/warehouse/search`.
- Repository/service search contract for active warehouses only.
- Centralized warehouse exception mapping consistent with existing JSON error behavior.

## Acceptance Criteria
- All `CODE_ASSIGNMENT.md` tasks complete.
- Bonus search endpoint + tests complete.
- `QUESTIONS.md` completed.
- Coverage `>=80%`.
- Full suite passes.
- Changes pushed from current branch with phase-wise commit history.
