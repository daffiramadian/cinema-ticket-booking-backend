# Architecture

## Initial service boundaries

```text
Future frontend
      |
      +---- REST ---- Catalog Service ---- catalog_db
      +---- REST ---- Booking Service ---- booking_db
      |                    |
      |                    +---- REST ---- Catalog Service (showtime validation)
      |                    +---- REST ---- Payment Service
      +---- REST ---- Payment Service ---- payment_db
```

The three applications are separate deployable processes. Each service owns its database/schema and exposes HTTP APIs. A single PostgreSQL container is used for local convenience, with three separate databases; production deployment can use separate database instances. No service reads another service's tables.

## Responsibilities

- **Catalog:** movie, cinema, screen, showtime, and seat-map reference data.
- **Booking:** reservation lifecycle, temporary seat holds, and availability protection.
- **Payment:** simulated payment requests and transaction status.

The scaffold uses synchronous REST to keep the first integration understandable. A gateway, service discovery, message broker, authentication, and Kubernetes are later extensions, not prerequisites for proving service boundaries in the course MVP.

## Data and reliability rules

- Give each service its own database credentials/schema before deployment beyond local development.
- Never share persistence entities across services.
- Booking is the authority for active seat reservations; use database constraints/transactions to prevent race conditions.
- Use timeouts and idempotency for cross-service calls. Do not assume a distributed transaction across all services.
- Keep the payment simulator free of real card data.

