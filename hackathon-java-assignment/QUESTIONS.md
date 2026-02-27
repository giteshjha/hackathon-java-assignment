# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?


**Answer:**
```txt
OpenAPI-first (Warehouse API) pros:
- Contract is explicit and shareable across teams (backend, frontend, QA).
- Generated interfaces reduce drift between implementation and API spec.
- Stronger consistency for validation, response structure, and documentation.
- Easier long-term governance when multiple services/teams consume the API.

OpenAPI-first cons:
- Slower iteration for very small endpoint changes because spec + generation cycle must be respected.
- If codegen conventions are not carefully controlled, generated layers can feel rigid.
- Extra tooling/build complexity.

Hand-coded endpoints (Product/Store) pros:
- Fast to implement and refactor for small, internal, or evolving features.
- Full control over code style and behavior without generation constraints.
- Lower initial setup complexity.

Hand-coded endpoints cons:
- Higher risk of undocumented changes and contract drift.
- Requires stronger discipline to keep docs/tests aligned manually.
- Harder to standardize across many endpoints over time.

What I would choose:
- For business-critical and externally consumed APIs, I prefer OpenAPI-first.
- For small internal endpoints and fast prototyping, hand-coded is acceptable.
- In this project, I would keep Warehouse OpenAPI-first and gradually move Product/Store to spec-first once behavior stabilizes.
```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Given time constraints, I would prioritize tests that protect business correctness and data integrity first:

1) Domain/use-case unit tests (highest ROI)
- Validate rules: uniqueness, location validity, capacity/stock constraints, archive/replace constraints.
- Fast feedback, deterministic, easy to run repeatedly.

2) Transaction/concurrency and persistence-sensitive tests
- Protect against lost updates, optimistic-locking issues, and rollback semantics.
- These catch the most expensive production failures in this codebase.

3) API integration tests (selected critical flows)
- Verify endpoint wiring, status codes, and key error paths.
- Focus first on Warehouse and Store transaction-sensitive endpoints.

4) Broader integration and edge tests
- Add as confidence layer after core behavior is stable.

How to keep coverage effective over time:
- Enforce CI gates: tests + JaCoCo threshold (>=80%).
- Keep a risk-based test matrix per feature (positive, negative, boundary, concurrency/error path).
- Add regression tests for every production bug found.
- Avoid coverage gaming: prioritize meaningful assertions over only line coverage.
- Keep tests isolated and reliable to prevent flaky pipelines.
```
