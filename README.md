# Cinema Ticket Booking Backend

Backend scaffold for a three-person course project. The repository uses three independently runnable Spring Boot services with separate data ownership:

| Service | Responsibility | Local port | Database |
| --- | --- | ---: | --- |
| Catalog | Movies, cinemas, screens and showtimes | 8081 | `catalog_db` |
| Booking | Reservations, seat holds and booking lifecycle | 8082 | `booking_db` |
| Payment | Simulated payment attempts and transaction status | 8083 | `payment_db` |

Each service is a standalone Maven project under `services/`. They can be pulled and developed independently in feature branches. The initial scaffold includes health endpoints, OpenAPI/Swagger UI, local PostgreSQL, Dockerfiles, and a GitHub Actions build workflow. Business endpoints and integration are the next implementation step.

## Requirements

- JDK 21
- Maven 3.9 or newer (until the team commits Maven Wrapper files)
- Docker Desktop with Compose
- Git

## Run locally

Start the three databases:

```powershell
docker compose up -d
```

In separate terminals, start each service:

```powershell
cd services/catalog-service
mvn spring-boot:run
```

Repeat for `booking-service` and `payment-service`. In PowerShell use `mvn` as shown; after the team generates and commits Maven Wrapper files, use `mvnw.cmd` instead.

Service URLs:

- Catalog: <http://localhost:8081/actuator/health> and <http://localhost:8081/swagger-ui.html>
- Booking: <http://localhost:8082/actuator/health> and <http://localhost:8082/swagger-ui.html>
- Payment: <http://localhost:8083/actuator/health> and <http://localhost:8083/swagger-ui.html>

Generate and commit Maven Wrapper files once per service (`mvn -N wrapper:wrapper`) so all contributors can build with the same Maven version without a separate Maven installation.

## CI

GitHub Actions builds all three Maven projects on pushes and pull requests. Add service tests as features are implemented; CI will run them through Maven's `verify` lifecycle.

## Security and configuration

Local database values in `compose.yaml` are development-only. Never put real credentials or secrets in Git. Service settings use environment-variable overrides.
