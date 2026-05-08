# Quantity Measurement App
 
A Spring Boot REST API for performing quantity measurement operations — compare, convert, add, subtract, and divide across Length, Volume, Weight, and Temperature units. All operations are persisted to a database for full audit and history support.
 
---
 
## Table of contents
 
- [Overview](#overview)
- [Technology stack](#technology-stack)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [API reference](#api-reference)
  - [POST endpoints](#post-endpoints)
  - [GET endpoints](#get-endpoints)
  - [Request and response shapes](#request-and-response-shapes)
  - [Error responses](#error-responses)
- [Supported units](#supported-units)
- [Running tests](#running-tests)
- [Accessing developer tools](#accessing-developer-tools)
- [Architecture notes](#architecture-notes)
---
 
## Overview
 
UC17 transforms a standalone Java quantity measurement application (UC16) into a production-ready Spring Boot REST service. All original business logic is preserved. The persistence layer is upgraded from raw JDBC to Spring Data JPA, and all functionality is exposed through RESTful HTTP endpoints with JSON responses.
 
Key capabilities:
 
- Compare two quantities of the same type (e.g. 1 FOOT vs 12 INCHES → `true`)
- Convert a quantity to a different unit (e.g. 100°C → 212°F)
- Add, subtract, and divide quantities with optional target-unit output
- Retrieve full operation history by type, measurement category, or error status
- Auto-generated Swagger UI documentation
- Spring Actuator health and metrics endpoints
- In-memory H2 database for development; MySQL-ready for production
---
 
## Technology stack
 
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Persistence | Spring Data JPA + Hibernate |
| Database (dev) | H2 in-memory |
| Database (prod) | MySQL 8 |
| Validation | Jakarta Bean Validation |
| Security | Spring Security (CORS + permit-all for dev) |
| API docs | SpringDoc OpenAPI / Swagger UI |
| Monitoring | Spring Boot Actuator |
| Boilerplate reduction | Lombok |
| Build tool | Maven |
| Testing | JUnit 5, Mockito, MockMvc, TestRestTemplate |
 
---
 
## Project structure
 
```
quantity-measurement-app/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/app/quantitymeasurement/
    │   │   ├── QuantityMeasurementAppApplication.java   # Entry point + Swagger metadata
    │   │   │
    │   │   ├── model/                                   # DTOs and JPA entity
    │   │   │   ├── QuantityDTO.java                     # Single quantity (value + unit + type)
    │   │   │   ├── QuantityInputDTO.java                # Request body wrapper (2–3 QuantityDTOs)
    │   │   │   ├── QuantityMeasurementDTO.java          # Response DTO with fromEntity/toEntity
    │   │   │   ├── QuantityMeasurementEntity.java       # @Entity mapped to DB table
    │   │   │   └── OperationType.java                   # Enum: ADD, SUBTRACT, DIVIDE, COMPARE, CONVERT
    │   │   │
    │   │   ├── controller/
    │   │   │   └── QuantityMeasurementController.java   # @RestController — all endpoints
    │   │   │
    │   │   ├── service/
    │   │   │   ├── IQuantityMeasurementService.java     # Service interface
    │   │   │   └── QuantityMeasurementServiceImpl.java  # Business logic + unit conversion
    │   │   │
    │   │   ├── repository/
    │   │   │   └── QuantityMeasurementRepository.java   # JpaRepository + custom queries
    │   │   │
    │   │   ├── exception/
    │   │   │   ├── QuantityMeasurementException.java    # Custom domain exception
    │   │   │   └── GlobalExceptionHandler.java          # @ControllerAdvice — centralized errors
    │   │   │
    │   │   └── config/
    │   │       └── SecurityConfig.java                  # CORS, CSRF, session config
    │   │
    │   └── resources/
    │       ├── application.properties                   # Dev configuration (H2)
    │       └── application-prod.properties              # Prod overrides (MySQL)
    │
    └── test/
        └── java/com/app/quantitymeasurement/
            ├── controller/
            │   └── QuantityMeasurementControllerTest.java   # @WebMvcTest + MockMvc
            └── QuantityMeasurementApplicationTests.java     # @SpringBootTest integration tests
```
 
---
 
## Getting started
 
### Prerequisites
 
- Java 17+
- Maven 3.6+
- (Optional) MySQL 8 for production profile
### Clone and build
 
```bash
git clone <your-repo-url>
cd quantity-measurement-app
mvn clean install
```
 
### Run in development mode
 
```bash
mvn spring-boot:run
```
 
The application starts on `http://localhost:8080` using an in-memory H2 database. No external database setup is required.
 
### Run as a JAR
 
```bash
mvn clean package
java -jar target/quantity-measurement-app-1.0.0.jar
```
 
### Run with production profile (MySQL)
 
```bash
java -jar target/quantity-measurement-app-1.0.0.jar --spring.profiles.active=prod
```
 
Make sure MySQL is running and the credentials in `application-prod.properties` match your setup before switching to this profile.
 
---
 
## Configuration
 
### Development (`application.properties`)
 
| Property | Value | Description |
|---|---|---|
| `server.port` | `8080` | HTTP port |
| `spring.datasource.url` | `jdbc:h2:mem:quantitymeasurementdb` | In-memory H2 database |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-creates/updates the schema |
| `spring.h2.console.enabled` | `true` | Enables H2 browser console |
| `spring.jpa.show-sql` | `true` | Logs all SQL to console |
| `springdoc.swagger-ui.enabled` | `true` | Enables Swagger UI |
| `management.endpoints.web.exposure.include` | `health,metrics,info` | Actuator endpoints |
 
### Production (`application-prod.properties`)
 
Activated with `--spring.profiles.active=prod`. Overrides the datasource to MySQL and quiets logging:
 
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/quantity_measurement
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.root=WARN
```
 
---
 
## API reference
 
**Base URL:** `http://localhost:8080/api/v1/quantities`
 
All POST endpoints accept and return `application/json`. All responses use the `QuantityMeasurementDTO` shape (see [Request and response shapes](#request-and-response-shapes)).
 
### POST endpoints
 
| Method | Path | Description |
|---|---|---|
| POST | `/compare` | Compare two quantities — returns `resultString: "true"` or `"false"` |
| POST | `/convert` | Convert a quantity to the unit of the second operand |
| POST | `/add` | Add two quantities; result in `thisQuantityDTO`'s unit |
| POST | `/add-with-target-unit` | Add two quantities; result in `targetQuantityDTO`'s unit |
| POST | `/subtract` | Subtract `thatQuantity` from `thisQuantity`; result in `thisQuantityDTO`'s unit |
| POST | `/subtract-with-target-unit` | Subtract with explicit target unit |
| POST | `/divide` | Divide `thisQuantity` by `thatQuantity` (base-unit ratio) |
 
### GET endpoints
 
| Method | Path | Description |
|---|---|---|
| GET | `/history/operation/{operation}` | All measurements for a given operation type |
| GET | `/history/type/{type}` | All measurements for a given measurement type |
| GET | `/count/{operation}` | Count of successful (non-error) operations |
| GET | `/history/errored` | All measurements that resulted in an error |
 
**Valid `{operation}` values:** `ADD`, `SUBTRACT`, `DIVIDE`, `COMPARE`, `CONVERT`
 
**Valid `{type}` values:** `LengthUnit`, `VolumeUnit`, `WeightUnit`, `TemperatureUnit`
 
---
 
### Request and response shapes
 
#### Request body — `QuantityInputDTO`
 
All POST endpoints share the same request body shape. `targetQuantityDTO` is optional and only required for `add-with-target-unit` and `subtract-with-target-unit`.
 
```json
{
  "thisQuantityDTO": {
    "value": 1.0,
    "unit": "FEET",
    "measurementType": "LengthUnit"
  },
  "thatQuantityDTO": {
    "value": 12.0,
    "unit": "INCHES",
    "measurementType": "LengthUnit"
  },
  "targetQuantityDTO": {
    "value": 0.0,
    "unit": "INCHES",
    "measurementType": "LengthUnit"
  }
}
```
 
#### Response body — `QuantityMeasurementDTO`
 
All POST endpoints return the same response shape. Fields not applicable to the operation (e.g. `resultString` for arithmetic operations) will be `null` or `0.0`.
 
```json
{
  "thisValue": 1.0,
  "thisUnit": "FEET",
  "thisMeasurementType": "LengthUnit",
  "thatValue": 12.0,
  "thatUnit": "INCHES",
  "thatMeasurementType": "LengthUnit",
  "operation": "COMPARE",
  "resultString": "true",
  "resultValue": 0.0,
  "resultUnit": null,
  "resultMeasurementType": null,
  "error": false,
  "errorMessage": null
}
```
 
#### Example: compare (1 FOOT = 12 INCHES)
 
```bash
curl -X POST http://localhost:8080/api/v1/quantities/compare \
  -H "Content-Type: application/json" \
  -d '{
    "thisQuantityDTO": {"value": 1.0, "unit": "FEET", "measurementType": "LengthUnit"},
    "thatQuantityDTO": {"value": 12.0, "unit": "INCHES", "measurementType": "LengthUnit"}
  }'
```
 
```json
{ "operation": "COMPARE", "resultString": "true", "error": false }
```
 
#### Example: convert (100°C → °F)
 
```bash
curl -X POST http://localhost:8080/api/v1/quantities/convert \
  -H "Content-Type: application/json" \
  -d '{
    "thisQuantityDTO": {"value": 100.0, "unit": "CELSIUS", "measurementType": "TemperatureUnit"},
    "thatQuantityDTO": {"value": 0.0, "unit": "FAHRENHEIT", "measurementType": "TemperatureUnit"}
  }'
```
 
```json
{ "operation": "CONVERT", "resultValue": 212.0, "resultUnit": "FAHRENHEIT", "error": false }
```
 
#### Example: add with target unit (1 FOOT + 12 INCHES → result in INCHES)
 
```bash
curl -X POST http://localhost:8080/api/v1/quantities/add-with-target-unit \
  -H "Content-Type: application/json" \
  -d '{
    "thisQuantityDTO": {"value": 1.0, "unit": "FEET", "measurementType": "LengthUnit"},
    "thatQuantityDTO": {"value": 12.0, "unit": "INCHES", "measurementType": "LengthUnit"},
    "targetQuantityDTO": {"value": 0.0, "unit": "INCHES", "measurementType": "LengthUnit"}
  }'
```
 
```json
{ "operation": "ADD", "resultValue": 24.0, "resultUnit": "INCHES", "error": false }
```
 
#### Example: get operation history
 
```bash
curl http://localhost:8080/api/v1/quantities/history/operation/COMPARE
```
 
Returns a JSON array of `QuantityMeasurementDTO` objects for all COMPARE operations ever performed.
 
#### Example: get error history
 
```bash
curl http://localhost:8080/api/v1/quantities/history/errored
```
 
```json
[
  {
    "operation": "ADD",
    "error": true,
    "errorMessage": "add Error: Cannot perform arithmetic between different measurement categories: LengthUnit and WeightUnit"
  }
]
```
 
---
 
### Error responses
 
All errors follow a consistent `ErrorResponse` shape returned by `GlobalExceptionHandler`:
 
```json
{
  "timestamp": "2026-03-30T10:45:22.123456",
  "status": 400,
  "error": "Quantity Measurement Error",
  "message": "Unit must be valid for the specified measurement type",
  "path": "/api/v1/quantities/compare"
}
```
 
| Scenario | HTTP status | Trigger |
|---|---|---|
| Invalid request body (missing field, wrong type) | `400 Bad Request` | Bean Validation failure (`@Valid`) |
| Invalid unit for measurement type | `400 Bad Request` | `@AssertTrue isValidUnit()` in `QuantityDTO` |
| Incompatible measurement types (LengthUnit + WeightUnit) | `400 Bad Request` | `QuantityMeasurementException` |
| Division by zero | `500 Internal Server Error` | `ArithmeticException` |
| Any other unhandled exception | `500 Internal Server Error` | Catch-all in `GlobalExceptionHandler` |
 
> **Note:** Error operations are always saved to the database even when they fail. This is intentional — the service does not use `@Transactional` at the class level so that error records are never rolled back. They are retrievable via `GET /history/errored`.
 
---
 
## Supported units
 
### LengthUnit
 
`FEET`, `INCHES`, `YARDS`, `CENTIMETERS`, `METERS`, `KILOMETERS`, `MILES`
 
Base unit for conversion: **INCHES**
 
### VolumeUnit
 
`MILLILITER`, `LITRE`, `GALLON`, `CUBIC_METER`, `CUBIC_CENTIMETER`
 
Base unit for conversion: **MILLILITER**
 
### WeightUnit
 
`GRAM`, `KILOGRAM`, `MILLIGRAM`, `POUND`, `TONNE`
 
Base unit for conversion: **GRAM**
 
### TemperatureUnit
 
`CELSIUS`, `FAHRENHEIT`, `KELVIN`
 
Base unit for conversion: **CELSIUS**
 
> Both operands in any operation must share the same `measurementType`. Mixing types (e.g. LengthUnit and WeightUnit) returns a `400 Bad Request`.
 
---
 
## Running tests
 
### Run all tests
 
```bash
mvn test
```
 
### Run only controller unit tests (no DB required)
 
```bash
mvn test -Dtest=QuantityMeasurementControllerTest
```
 
### Run only integration tests (full Spring context + H2)
 
```bash
mvn test -Dtest=QuantityMeasurementApplicationTests
```
 
### Generate HTML test report
 
```bash
mvn surefire-report:report
open target/site/surefire-report.html
```
 
### Test coverage summary
 
| Test class | Type | What it tests |
|---|---|---|
| `QuantityMeasurementControllerTest` | Unit (`@WebMvcTest`) | Controller endpoints in isolation with mocked service |
| `QuantityMeasurementApplicationTests` | Integration (`@SpringBootTest`) | Full stack against real H2 — compare, convert, add, subtract, divide, history, error handling |
 
The integration test suite covers 35 scenarios including all happy-path operations, validation failures, incompatible-type errors, divide-by-zero, history retrieval, and error history audit.
 
---
 
## Accessing developer tools
 
### Swagger UI
 
Interactive API documentation — try any endpoint directly from the browser:
 
```
http://localhost:8080/swagger-ui.html
```
 
### H2 Console
 
Browser-based database viewer for the in-memory development database:
 
```
http://localhost:8080/h2-console
```
 
| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:quantitymeasurementdb` |
| Username | `sa` |
| Password | *(leave blank)* |
 
### Actuator endpoints
 
| Endpoint | Description |
|---|---|
| `http://localhost:8080/actuator/health` | Application health status |
| `http://localhost:8080/actuator/metrics` | JVM and request metrics |
| `http://localhost:8080/actuator/info` | Application info |
 
---
 
## Architecture notes
 
### Layer responsibilities
 
| Layer | Class | Responsibility |
|---|---|---|
| Controller | `QuantityMeasurementController` | Receive HTTP requests, validate input with `@Valid`, delegate to service, return `ResponseEntity` |
| Service interface | `IQuantityMeasurementService` | Defines the contract — controller depends on this, not the implementation |
| Service impl | `QuantityMeasurementServiceImpl` | Business logic, unit conversion, calls repository to persist every result |
| Repository | `QuantityMeasurementRepository` | Spring Data JPA — CRUD + named queries + one custom `@Query` |
| Model | `QuantityMeasurementEntity` | JPA entity persisted to `quantity_measurement_entity` table |
| DTOs | `QuantityDTO`, `QuantityInputDTO`, `QuantityMeasurementDTO` | Clean API surface — decoupled from the entity |
| Exception handling | `GlobalExceptionHandler` | `@ControllerAdvice` — all errors handled in one place, consistent JSON shape |
| Security | `SecurityConfig` | CORS, CSRF disabled, stateless sessions, H2 console access permitted |
 
### DTO vs Entity separation
 
The API uses three distinct DTO classes rather than exposing the JPA entity directly:
 
- `QuantityDTO` — a single quantity (value + unit + measurementType), used as a building block
- `QuantityInputDTO` — the full POST request body, wrapping two or three `QuantityDTO` objects
- `QuantityMeasurementDTO` — the operation result, returned as the JSON response
`QuantityMeasurementDTO` includes static factory methods (`fromEntity`, `toEntity`, `fromEntityList`, `toEntityList`) that centralise all conversion between the API layer and the persistence layer.
 
### Why no `@Transactional` on the service?
 
The service intentionally omits `@Transactional` at the class level. Every operation — including failures — calls `repository.save()` so the result (including error details) is always persisted. A class-level transaction would roll back on exception and lose the error record, making `GET /history/errored` unreliable.
 
### Database indexes
 
The `quantity_measurement_entity` table has three indexes optimised for the history query methods:
 
- `idx_operation` on `operation` column — used by `findByOperation()`
- `idx_measurement_type` on `this_measurement_type` — used by `findByThisMeasurementType()`
- `idx_created_at` on `created_at` — used by `findByCreatedAtAfter()`
