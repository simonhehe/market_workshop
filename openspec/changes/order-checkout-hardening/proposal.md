## WHY

Workshop dry-run: harden checkout so cart → address-required order create works end-to-end, with testable ACs.

## WHAT CHANGES

- Add `/orders/create` Vue page + router (before `:id`)
- Require `addressId` in `OrderService#createOrder`
- Add unit tests for create/cancel paths in core package
- Quality report under `workshop/quality/`

## IMPACT

- Frontend cart checkout route becomes usable
- API behavior change: missing address fails (breaking vs previous null-address allow)
- Coverage scope: OrderService/OrderController/Cart*

## Non-goals

Payment, coupons, admin order redesign.
