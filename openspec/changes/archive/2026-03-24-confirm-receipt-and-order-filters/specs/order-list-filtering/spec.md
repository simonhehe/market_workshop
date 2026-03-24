## ADDED Requirements

### Requirement: User order list can filter by status

The system SHALL support filtering the authenticated user's order list by an optional `status` query parameter on `GET /api/orders`. When `status` is omitted, the system MUST return all orders for the user consistent with existing behavior (subject to soft-delete rules already implemented). When `status` is provided, the system MUST return only orders matching that exact status value for that user.

#### Scenario: Filter returns only matching orders

- **WHEN** the user requests `GET /api/orders?status=shipped` and has orders in `shipped` and other states
- **THEN** the response contains only orders in `shipped` for that user

#### Scenario: No parameter preserves full list

- **WHEN** the user requests `GET /api/orders` with no `status` query
- **THEN** the response contains the full order list for that user as today

#### Scenario: Invalid status value rejected or documented

- **WHEN** the user passes a `status` value that is not in the allowed set defined by the API contract (see design)
- **THEN** the system responds with an error (preferred 400 with error code) OR the behavior is explicitly documented as no-op filter; the chosen behavior MUST match implementation and acceptance criteria

### Requirement: Order list filter visibility on the client

The client SHALL provide a way to choose a status filter (e.g. dropdown or tabs) for "我的订单". The default selection MUST mean "all orders" (no filter parameter sent). Changing the filter MUST refetch the list with the corresponding `status` query parameter when a specific status is selected.

#### Scenario: Default shows all orders

- **WHEN** the user opens the order list page
- **THEN** the client loads orders without a `status` query unless the user selects a specific filter

#### Scenario: User selects a status filter

- **WHEN** the user selects e.g. "待处理" mapped to `pending`
- **THEN** the client calls the API with `status=pending` and displays only those results
