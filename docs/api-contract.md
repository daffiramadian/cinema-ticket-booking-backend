# Initial API contract checklist

This is a design checklist, not yet an implemented API. Agree exact names, fields, status codes, and validation rules before coding. The course requires all five HTTP methods; assign them to meaningful resources and document them in OpenAPI.

| Service | Method | Draft route | Purpose |
| --- | --- | --- | --- |
| Catalog | GET | `/api/v1/movies`, `/api/v1/movies/{id}` | Browse and read movies |
| Catalog | POST | `/api/v1/movies` | Create a movie |
| Catalog | PUT | `/api/v1/movies/{id}` | Replace a movie |
| Catalog | PATCH | `/api/v1/movies/{id}` | Partially update a movie |
| Catalog | DELETE | `/api/v1/movies/{id}` | Remove a movie if business rules allow |
| Booking | POST | `/api/v1/bookings` | Request a temporary seat reservation |
| Booking | GET | `/api/v1/bookings/{id}` | Read booking state |
| Booking | PATCH | `/api/v1/bookings/{id}` | Apply an allowed lifecycle action |
| Payment | POST | `/api/v1/payments` | Simulate a payment attempt |
| Payment | GET | `/api/v1/payments/{id}` | Read transaction state |

The five-method requirement can be met across the API, not necessarily by forcing CRUD semantics onto payment records. Document why immutable payment attempts are not updated/deleted directly. Choose a meaningful resource in the service that needs PUT/DELETE, such as Catalog movies or showtimes.

For every endpoint specify: request/response examples, validation, status codes, error body, idempotency behavior, and whether it is exposed in v1, v2, or both.

