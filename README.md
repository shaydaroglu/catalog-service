# Catalog Service

Catalog Service is a Spring Boot service for reading product offerings and validating product IDs.

## Tech Stack

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- H2 for tests
- Docker Compose
- OpenAPI / Swagger UI

## Features

- List products with pagination
- Fetch a product by ID
- Validate that a list of product IDs exists
- RFC 7807-style error responses
- Flyway-managed schema and seed data
- Docker-based local runtime and test workflow

## What I Built And What I Cut

This project focuses on the read and validation responsibilities of a catalog service:

- Built:
  - list product offerings with pagination
  - retrieve a product offering by ID
  - validate a batch of product IDs for downstream services
  - expose consistent error responses
  - manage schema creation and seed data through migrations
- Intentionally omitted:
  - create/update/delete product API
  - no circuit breaker is implemented; the order service returns 502 and lets the caller retry

The write API was left out to keep the assignment focused on the catalog read path and the product-ID validation use case.

## Decisions And Tradeoffs

### Hexagonal Architecture

The service uses a hexagonal-style structure with inbound web adapters, application ports, an application service, and an outbound persistence adapter. This keeps HTTP and JPA concerns outside the core use case logic, which makes the service easier to test and keeps the application layer independent from delivery and storage details. The tradeoff is a few more interfaces and files than a direct controller-to-repository design would require.

### UUID Primary Keys

Products use UUIDs as primary keys so identifiers can be generated without relying on a database sequence and can be safely passed between services without exposing ordering information. The tradeoff is that UUID indexes are larger than numeric keys and are less human-friendly when debugging manually.

### Bulk Validation Instead Of Repeated Single Lookups

The validation use case accepts a list of IDs and checks them in one database query through an ID projection. This is more efficient than making one lookup per product and lets the response report all missing IDs together. The tradeoff is that the endpoint has a slightly more specialized contract than a simple single-product lookup.

### RFC 7807-Style Errors

Errors use a problem-details-style response shape so clients receive a consistent structure with `title`, `status`, `detail`, `instance`, and optional structured `errors`. This is easier for clients to parse than ad hoc error payloads. The tradeoff is a little more response boilerplate for simple failures.

### `POST /validate` Instead Of `GET`

Validation uses `POST /api/v1/products/validate` because the request body contains a list of UUIDs. A `GET` request would require packing that list into the query string, which is less readable, more awkward for larger batches, and subject to URL-length limits. The tradeoff is that the endpoint is not cache-friendly in the same way a pure `GET` lookup would be.

### Flyway Owns The Schema

Flyway migrations create and evolve the schema, while Hibernate runs with validation rather than schema generation. This keeps database changes explicit, reviewable, and reproducible across environments. The tradeoff is that schema changes require writing migrations instead of relying on automatic Hibernate updates.

### Pagination Wrapper

The list endpoint returns a small wrapper containing `items`, `page`, `size`, `totalElements`, and `totalPages` instead of exposing Spring Data's `Page` type directly. This keeps the external API stable and avoids leaking framework-specific serialization details to clients. The tradeoff is maintaining a small DTO mapping layer.

### H2 Tests In PostgreSQL Compatibility Mode

Automated tests use in-memory H2 with PostgreSQL compatibility mode so the test suite is fast and self-contained while still exercising Flyway migrations and JPA wiring. The tradeoff is that H2 is not PostgreSQL, so any SQL that depends on PostgreSQL-specific behavior should still be verified against PostgreSQL before production use.

## Project Structure

```text
src/main/java/com/sercan/catalog_service
├── adapter
│   ├── in/web              # HTTP controllers, DTOs, exception handling
│   └── out/persistence     # JPA entities, repositories, persistence adapter
├── application
│   ├── port/in             # Use case interfaces
│   ├── port/out            # Repository ports
│   └── service             # Application services
└── domain                  # Domain model and domain exceptions
```

## Requirements

- Docker and Docker Compose
- Java 21 only if you want to run Maven directly outside Docker

## Configuration

The service supports these Spring profiles:

- `local` for running the application against a local PostgreSQL instance
- `docker` for running inside Docker Compose
- `test` for automated tests with H2

The default service port is `8081`.

### Environment Variables

The Docker setup uses these variables from `.env`:

| Variable | Purpose |
| --- | --- |
| `POSTGRES_DB` | PostgreSQL database name |
| `POSTGRES_USER` | PostgreSQL username |
| `POSTGRES_PASSWORD` | PostgreSQL password |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile |
| `SPRING_DATASOURCE_URL` | JDBC URL used by the service |
| `SERVER_PORT` | Exposed application port |
| `TZ` | Time zone for the containers |

## Run With Docker

Start PostgreSQL and the service:

```bash
docker compose up --build
```

When the application is running:

- API base URL: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI docs: `http://localhost:8081/api-docs`

Stop the stack:

```bash
docker compose down
```

Remove containers and the database volume:

```bash
docker compose down -v
```

## Run Tests

The supported verification workflow runs tests inside Docker with Java 21:

```bash
docker compose --profile test run --rm catalog-test
```

This runs the full Maven `verify` lifecycle, including unit and integration tests.

## API

### List Products

```http
GET /api/v1/products?page=0&size=20
```

Example response:

```json
{
  "items": [
    {
      "id": "0f7e4e69-9f64-4b52-9d9f-65d2e56fa581",
      "name": "po-1",
      "price": 10.0000
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 10,
  "totalPages": 1
}
```

Pagination rules:

- `page` must be `0` or greater
- `size` must be between `1` and `100`

### Get Product By ID

```http
GET /api/v1/products/{id}
```

Example response:

```json
{
  "id": "0f7e4e69-9f64-4b52-9d9f-65d2e56fa581",
  "name": "po-1",
  "price": 10.0000
}
```

### Validate Product IDs

```http
POST /api/v1/products/validate
Content-Type: application/json
```

Example request:

```json
[
  "0f7e4e69-9f64-4b52-9d9f-65d2e56fa581",
  "850a4922-2019-4314-b1f4-b56b902c324f"
]
```

Returns:

- `200 OK` when all IDs exist
- `404 Not Found` when one or more IDs are missing
- `400 Bad Request` when the request body is empty or malformed

Example missing-products response:

```json
{
  "title": "Products Not Found",
  "status": 404,
  "detail": "One or more product offerings could not be found",
  "instance": "/api/v1/products/validate",
  "timestamp": "2026-05-17T12:00:00Z",
  "errors": {
    "missingIds": [
      "850a4922-2019-4314-b1f4-b56b902c324f"
    ]
  }
}
```

## Database

Flyway migrations live in:

```text
src/main/resources/db/migration
```

Current migrations:

- `V1__init.sql` creates the `products` table
- `V2__seed_product_data.sql` seeds sample product offerings when the table is empty

## Assumptions

- Seed data IDs are generated as UUIDs by the database and are not hard-coded in the migration.
- Seeded offerings such as `po-1`, `po-2`, and `po-3` are available through `GET /api/v1/products`.
- Consumers validate known UUIDs rather than relying on product names as identifiers.
- The service owns catalog reads and validation only; product creation and mutation are outside the current scope.

## Local Development Without Docker

Start PostgreSQL locally, then run the service with the `local` profile:

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

The local profile expects:

```text
jdbc:postgresql://localhost:5432/catalog
username: catalog_user
password: catalog_pass
```

## Build

Build the application image:

```bash
docker build -t catalog-service .
```

Build the application with Maven outside Docker:

```bash
./mvnw package
```

Maven is configured to require Java 21.
