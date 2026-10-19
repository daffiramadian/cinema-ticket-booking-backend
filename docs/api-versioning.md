# API versioning and breaking changes

Use URI versioning: `/api/v1/...` and `/api/v2/...`. The versions must represent separately maintained public contracts, not aliases that only change the URL.

## Proposed booking example

`POST /api/v1/bookings` accepts a single `showtimeId` and `seatNumber`. The proposed v2 contract accepts a `screeningId`, a list of `seats`, and a `customerId`. This is a breaking change because field names and cardinality change, and v2 adds a required field.

Keep version-specific request/response DTOs and controllers. Map both to shared application/domain services. Keep old behavior available while v1 is supported. Document status codes and provide a migration table in the generated OpenAPI documentation.

The final contract and which service(s) expose v1/v2 must be agreed by the team before implementation. Avoid duplicating every internal endpoint across versions if the course rubric only requires an explicitly versioned public API surface; confirm rubric interpretation with the lecturer.

