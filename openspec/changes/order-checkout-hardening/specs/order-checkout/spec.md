## Spec: order-checkout

### Requirements

- System MUST require a valid addressId when creating an order.
- System MUST create pending order, deduct stock and points, and remove selected cart lines on success.
- System MUST fail without side effects on insufficient stock or points.
- System MUST expose a checkout page at `/orders/create` reachable from cart.
- User MUST be able to cancel own pending orders with points refund.

### Acceptance

See PRD AC-CREATE, AC-STOCK, AC-POINTS, AC-ADDR, AC-AUTH, AC-CANCEL, AC-ROUTE.
