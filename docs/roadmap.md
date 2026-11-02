# Project roadmap: start to finish

This sequence meets the requirements shown in the course screenshot: GET/POST/PUT/PATCH/DELETE, two API versions with a documented breaking change, microservices, OpenAPI, automated tests, and a Java enterprise framework. Confirm any additional BRONE deliverables with the lecturer.

## 1. Agree the scope (team)

Lock an MVP: browse movies/showtimes, request seats, create a temporary booking, simulate payment, and confirm or expire the booking. Agree the business rules for seat hold duration, cancellations, payment failure, and duplicate seat requests. Keep authentication, real payment providers, messaging brokers, Kubernetes, and frontend outside the first backend milestone unless required.

**Deliverable:** agreed domain glossary, booking state diagram, and API contract checklist.

## 2. Set up collaboration (team lead)

Invite all members to the GitHub repository, create `develop`, protect `main` if repository settings allow, and use issues plus pull requests. Each member clones the same repository and works from feature branches. Do not assign the whole project to one person; each service owner also writes tests and docs.

**Deliverable:** everyone can clone, build, open a PR, and review a teammate's PR.

## 3. Establish the local baseline (all members)

Install the agreed JDK and Docker Desktop. Run Compose, start each service, and check its health and Swagger UI. Agree the package naming convention and environment configuration before adding domain code.

**Deliverable:** reproducible local setup and three independently runnable services.

## 4. Implement Catalog (member 2)

Add movie, cinema/screen, and showtime models and CRUD/business endpoints. Define stable identifiers and the response fields Booking needs to validate a showtime and seat map. Enforce constraints and return clear 4xx errors.

**Deliverable:** catalog API and OpenAPI docs, persistence, and automated tests.

## 5. Implement Booking (team lead)

Create bookings with multiple seats, validate showtime and seat availability, and prevent duplicate active reservations. Model booking status transitions explicitly. A database uniqueness/locking strategy must protect concurrent requests; an in-memory check is insufficient.

**Deliverable:** booking API, persistence, lifecycle rules, and tests for duplicate/concurrent seat claims.

## 6. Implement Payment (member 3)

Implement a deterministic payment simulator with success/failure outcomes, idempotency, and queryable transaction status. Do not collect real card data.

**Deliverable:** payment API, status model, OpenAPI docs, and tests.

## 7. Integrate the business flow (all)

Agree synchronous REST contracts and timeouts. Booking asks Catalog to validate the showtime, creates a short-lived hold, then requests payment. A successful payment confirms the booking; failure/expiry releases seats. Handle retry and repeated payment requests safely. Document what happens if a service becomes unavailable.

**Deliverable:** end-to-end demonstrable flow with success, failure, and expiry scenarios.

## 8. Add API v2 as a real breaking change (all)

Keep `/api/v1` contracts stable and introduce `/api/v2` with an intentional, documented contract change (for example, `showtimeId` plus one `seatNumber` in v1 versus `screeningId`, `seats[]`, and `customerId` in v2). Maintain separate versioned DTOs/controllers while sharing domain logic. Add tests showing both versions work and document migration.

**Deliverable:** OpenAPI descriptions for both versions and a breaking-change rationale.

## 9. Complete quality and delivery (all)

Add unit tests for domain rules, integration tests for persistence/API, and one end-to-end scenario. Make GitHub Actions run the checks for every service on PRs. Review error responses, validation, logging, secrets, and setup instructions.

**Deliverable:** green CI, test evidence, and complete OpenAPI docs.

## 10. Demo and retrospective

Prepare a short demo covering service boundaries, the booking flow, the double-booking guard, one failed payment, v1/v2 contract differences, Swagger UI, and CI. Use issues/PRs/reviews and agreed responsibilities as contribution evidence for the team multiplier.

**Deliverable:** reproducible presentation script and final report aligned to BRONE instructions.

