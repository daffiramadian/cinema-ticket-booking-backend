# Booking flow and state transitions

## Happy path

1. A client reads movies and showtimes from Catalog.
2. The client submits a booking request with a showtime and one or more seats.
3. Booking calls Catalog to validate the showtime, then atomically claims the requested seats and creates a short-lived hold.
4. Booking asks Payment to create an idempotent simulated payment attempt.
5. On payment success, Booking changes the reservation to `CONFIRMED`. On a decline or timeout, it marks the reservation `PAYMENT_FAILED` and releases the hold.
6. A scheduled expiry or a request-time check releases holds that passed their deadline.

## Suggested booking states

```text
PENDING_PAYMENT -> CONFIRMED
PENDING_PAYMENT -> PAYMENT_FAILED
PENDING_PAYMENT -> EXPIRED
CONFIRMED -> CANCELLED   (only if the agreed cancellation policy allows it)
```

Define allowed transitions in one place and reject invalid transitions with a clear conflict response. Keep payment status and booking status separate: payment is authoritative for the attempt, while Booking owns the reservation state.

## Seat contention and failure handling

Use a database transaction plus a uniqueness constraint or suitable row lock so concurrent requests cannot hold the same `(showtime, seat)` pair. Add an idempotency key to booking/payment creation so client retries do not create duplicate work. Set finite HTTP timeouts and define what Booking does when Catalog or Payment is unavailable. A timeout can leave an uncertain payment outcome, so use a status lookup/reconciliation path before releasing seats in production-grade behavior.

For the course MVP, document the chosen simplifications and demonstrate at least successful payment, declined payment, expired hold, and two requests racing for one seat.

