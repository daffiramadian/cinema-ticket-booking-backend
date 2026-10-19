# Contributing

## Branch and pull request flow

Use `main` for demonstrable stable work and `develop` for group integration. Create short-lived branches from an up-to-date `develop`, for example `feature/catalog-movies`, `feature/booking-seats`, or `feature/payment-simulation`.

1. Pull the latest `develop` before starting.
2. Keep a branch focused on one issue/service change.
3. Include API/OpenAPI and documentation updates with behavior changes.
4. Open a pull request into `develop`; request one teammate review.
5. Merge only after CI passes and the relevant service still starts.
6. Promote a reviewed, integrated `develop` snapshot to `main` for demos/releases.

Use conventional commit prefixes such as `feat(catalog): add movie endpoint`, `fix(booking): reject duplicate seat hold`, and `docs(api): clarify v2 contract`.

## Ownership boundaries

Each service owns its schema and data. Do not query another service's database directly. Communicate through documented HTTP contracts; put cross-service DTOs in the calling service rather than creating a shared database model library.

## Team contribution evidence

Use issues, pull requests, reviews, and a small task board to record design, implementation, tests, documentation, and integration work. Assess contribution by completed work and quality, not commit count alone.

