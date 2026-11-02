# Cinema Ticket Booking Backend

Backend for a cinema ticket booking system built with microservices. Each service runs independently and owns its own database.

## Services

| Service | Responsibility | Port | Database |
| --- | --- | ---: | --- |
| Catalog | Movies, cinemas, screens, and showtimes | 8081 | `catalog_db` |
| Booking | Seat reservations, temporary holds, and booking status | 8082 | `booking_db` |
| Payment | Simulated payments and transaction status | 8083 | `payment_db` |

Each service is a separate Spring Boot and Maven project under the `services/` directory.

## Architecture

```text
Client
  ├── Catalog Service  → catalog_db
  ├── Booking Service  → booking_db
  └── Payment Service  → payment_db
```

Each service has its own database. Communication between services follows the documented HTTP contracts.

## Requirements

- JDK 21
- Docker Desktop with Compose
- Git

Maven Wrapper files are included in each service, so a separate Maven installation is not required.

## Run locally

Start PostgreSQL and the service databases:

```powershell
docker compose up -d
```

Start each service in a separate terminal.

### Catalog Service

```powershell
cd services/catalog-service
.\mvnw.cmd spring-boot:run
```

### Booking Service

```powershell
cd services/booking-service
.\mvnw.cmd spring-boot:run
```

### Payment Service

```powershell
cd services/payment-service
.\mvnw.cmd spring-boot:run
```

## Service URLs

### Catalog

- Health: <http://localhost:8081/actuator/health>
- Swagger UI: <http://localhost:8081/swagger-ui.html>

### Booking

- Health: <http://localhost:8082/actuator/health>
- Swagger UI: <http://localhost:8082/swagger-ui.html>

### Payment

- Health: <http://localhost:8083/actuator/health>
- Swagger UI: <http://localhost:8083/swagger-ui.html>

When using Swagger UI, select the server that matches the service port:

```text
Catalog → http://localhost:8081
Booking → http://localhost:8082
Payment → http://localhost:8083
```

## API v1

### Catalog

```text
GET    /api/v1/movies
GET    /api/v1/movies/{id}
POST   /api/v1/movies
PUT    /api/v1/movies/{id}
PATCH  /api/v1/movies/{id}
DELETE /api/v1/movies/{id}
```

### Booking

```text
POST  /api/v1/bookings
GET   /api/v1/bookings/{id}
PATCH /api/v1/bookings/{id}
```

The Booking PATCH endpoint handles supported actions such as cancelling a booking.

### Payment

```text
POST /api/v1/payments
GET  /api/v1/payments/{id}
```

Payment is simulated locally. The system does not process real card or banking data.

## Testing

Run Catalog tests:

```powershell
cd services/catalog-service
.\mvnw.cmd test
```

Run Booking tests:

```powershell
cd services/booking-service
.\mvnw.cmd test
```

Run Payment tests:

```powershell
cd services/payment-service
.\mvnw.cmd test
```

Build a service without running tests:

```powershell
.\mvnw.cmd -DskipTests package
```

## Project structure

```text
services/
├── catalog-service/
├── booking-service/
└── payment-service/

docs/
├── api-contract.md
├── api-versioning.md
├── architecture.md
├── booking-flow.md
├── openapi-v1.yaml
└── roadmap.md

infra/
└── postgres/
```

Basic service structure:

```text
src/
├── main/
│   ├── java/
│   └── resources/
└── test/
```

## Configuration

Service configuration is stored in each service's `application.yml`.

The default values can be overridden with these environment variables:

```text
SERVER_PORT
DB_URL
DB_USERNAME
DB_PASSWORD
```

The database credentials in the Compose configuration are for local development only. Do not commit real credentials, tokens, or secrets to the repository.

## CI

GitHub Actions builds all three services on every push and pull request. Service tests run through Maven's verification lifecycle.

## Development workflow

Use a feature branch for each change:

```text
feature branch
    ↓
pull request to develop
    ↓
review and tests
    ↓
merge to develop
```

Keep the `main` branch reserved for stable project releases.
