## ADDED Requirements

### Requirement: User confirms receipt for shipped orders

The system SHALL allow an authenticated user to transition their own order from `shipped` to `completed` when the order is in the `shipped` state. The system MUST NOT change points balance or product stock when confirming receipt. The system MUST reject the operation if the order does not belong to the requesting user.

#### Scenario: Successful confirm receipt

- **WHEN** an authenticated user calls the confirm-receipt action for an order that belongs to the user and `status` is `shipped`
- **THEN** the system updates the order `status` to `completed` and returns success

#### Scenario: Reject when order is not shipped

- **WHEN** an authenticated user calls the confirm-receipt action for an order in `pending`, `processing`, `completed`, or `cancelled`
- **THEN** the system rejects the request with an appropriate error (e.g. 400) and MUST NOT change the order `status`

#### Scenario: Reject when order belongs to another user

- **WHEN** a user calls the confirm-receipt action for an order whose `userId` does not match the token
- **THEN** the system rejects the request (same class of error as existing order access denial)

#### Scenario: Idempotent or stable response for already completed

- **WHEN** a user calls the confirm-receipt action for an order that already has `status` `completed` and belongs to the user
- **THEN** the system SHALL return a successful response without introducing inconsistent side effects (no duplicate stock or points changes)

### Requirement: Confirm-receipt action visibility on the client

The client SHALL show a "确认收货" (or equivalent) control only when the order belongs to the current user and `status` is `shipped`. The control MUST be hidden for `pending`, `processing`, `completed`, and `cancelled`.

#### Scenario: Shipped order shows confirm

- **WHEN** the user views order detail or list row for an order in `shipped`
- **THEN** the confirm-receipt control is visible (subject to screen layout constraints)

#### Scenario: Completed order hides confirm

- **WHEN** the user views an order in `completed`
- **THEN** the confirm-receipt control is not visible
