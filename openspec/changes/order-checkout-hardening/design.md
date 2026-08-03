## Context

Semi-complete mall; `createOrder` exists but UI route `/orders/create` missing; address optional.

## Goals

Medium MVP: cart → address → create → list/detail; stock/points/address errors; cancel pending.

## Approach

1. Frontend OrderCreate page using addressApi + orderApi + cart summary
2. Backend reject null/invalid address before mutating stock/points
3. JUnit tests for OrderService happy/error paths (mocked repos where practical)

## Risks

- SQLite + @Transactional test isolation
- Auth filter complicates API tests → prefer service-level UT first; API auth as separate MockMvc if time

## Open questions

None for unattended run — address required locked by PRD.
