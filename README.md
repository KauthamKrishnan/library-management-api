# Library Management API

A RESTful API for a simple library system, built with **Java 17** and **Spring Boot 3.5**. It lets API users register borrowers and books, list books, and borrow/return copies on behalf of a borrower.

- Runs out of the box on an **in-memory H2** database (no setup required).
- Switch to **MySQL** with a single environment variable for multi-environment deployments.
- Data validation, structured error handling, unit + integration tests with JaCoCo coverage, and a ready-to-use Dockerfile.

---

## Table of contents

- [Tech stack](#tech-stack)
- [Quick start (H2)](#quick-start-h2)
- [Running with MySQL](#running-with-mysql)
- [Configuration & environment variables](#configuration--environment-variables)
- [API reference](#api-reference)
- [Error format](#error-format)
- [Data model](#data-model)
- [Database choice justification](#database-choice-justification)
- [Testing & coverage](#testing--coverage)
- [Docker](#docker)
- [Postman collection](#postman-collection)
- [Assumptions](#assumptions)

---

## Tech stack

| Concern            | Choice                                             |
|--------------------|----------------------------------------------------|
| Language / runtime | Java 17                                            |
| Framework          | Spring Boot 3.5.x (Web, Data JPA, Validation)      |
| Build / packaging  | Maven (wrapper included: `mvnw` / `mvnw.cmd`)      |
| Database           | H2 in-memory (default) · MySQL (profile)           |
| Boilerplate        | Lombok (entities)                                  |
| DTOs               | Java records                                       |
| Tests              | JUnit 5, Mockito, Spring MockMvc, JaCoCo           |

Base package: `com.example.library` — organised into `controller`, `service`, `repository`, `entity`, `dto`, and `exception`.

---

## Quick start (H2)

Prerequisites: **JDK 17**. No database installation needed.

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` with the `h2` profile active by default.

- H2 console: `http://localhost:8080/h2-console`
  (JDBC URL: `jdbc:h2:mem:librarydb`, user `sa`, empty password)

Smoke test:

```bash
curl -X POST http://localhost:8080/api/v1/borrowers \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Reader","email":"alice@example.com"}'
```

---

## Running with MySQL

Activate the `mysql` profile and provide connection details via environment variables.

```bash
# macOS / Linux
export SPRING_PROFILES_ACTIVE=mysql
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/librarydb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=secret
./mvnw spring-boot:run
```

```powershell
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/librarydb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "secret"
.\mvnw.cmd spring-boot:run
```

The `mysql` profile provides sensible localhost defaults, so if your MySQL matches them you only need `SPRING_PROFILES_ACTIVE=mysql`.

---

## Configuration & environment variables

Configuration lives in profile-specific properties files under `src/main/resources`:

- `application.properties` — common settings; selects the active profile (`h2` by default).
- `application-h2.properties` — H2 datasource + H2 console.
- `application-mysql.properties` — MySQL datasource, fully driven by env vars.

| Variable                     | Default (h2 / mysql)                        | Description                          |
|------------------------------|---------------------------------------------|--------------------------------------|
| `SPRING_PROFILES_ACTIVE`     | `h2`                                        | Active profile (`h2` or `mysql`).    |
| `SERVER_PORT`                | `8080`                                      | HTTP port.                           |
| `SPRING_DATASOURCE_URL`      | localhost MySQL URL (mysql profile)         | JDBC URL for MySQL.                  |
| `SPRING_DATASOURCE_USERNAME` | `root` (mysql profile)                      | Database username.                   |
| `SPRING_DATASOURCE_PASSWORD` | `root` (mysql profile)                      | Database password.                   |

---

## API reference

Base path: `/api/v1`. All request and response bodies are JSON.

### Register a borrower

`POST /api/v1/borrowers` → **201 Created**

```json
{ "name": "Alice Reader", "email": "alice@example.com" }
```

Response:

```json
{ "id": 1, "name": "Alice Reader", "email": "alice@example.com" }
```

Errors: `400` invalid/blank fields · `409` email already registered.

### Register a book

`POST /api/v1/books` → **201 Created**

```json
{ "isbn": "978-0132350884", "title": "Clean Code", "author": "Robert C. Martin" }
```

Response:

```json
{ "id": 1, "isbn": "978-0132350884", "title": "Clean Code", "author": "Robert C. Martin", "available": true, "borrowerId": null }
```

Errors: `400` invalid/blank fields · `409` ISBN already exists with a different title/author.
Registering the same ISBN with matching title/author creates a **new copy with a new id**.

### List all books

`GET /api/v1/books` → **200 OK**

```json
[
  { "id": 1, "isbn": "978-0132350884", "title": "Clean Code", "author": "Robert C. Martin", "available": false, "borrowerId": 1 }
]
```

### Borrow a book

`POST /api/v1/borrowers/{borrowerId}/borrow/{bookId}` → **200 OK**

Response: the updated book (`available: false`, `borrowerId` set).

Errors: `404` borrower or book not found · `409` book already borrowed.

### Return a book

`POST /api/v1/borrowers/{borrowerId}/return/{bookId}` → **200 OK**

Response: the updated book (`available: true`, `borrowerId: null`).

Errors: `404` borrower or book not found · `409` book not currently borrowed, or held by a different borrower.

---

## Error format

Every error returns a consistent JSON body:

```json
{
  "timestamp": "2026-07-08T10:15:30.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/borrowers",
  "fieldErrors": [
    { "field": "email", "message": "email must be a valid address" }
  ]
}
```

`fieldErrors` is populated only for validation failures; it is an empty array otherwise.

| Status | When                                                                     |
|--------|--------------------------------------------------------------------------|
| 400    | Validation failure or malformed JSON body                                |
| 404    | Referenced borrower or book id does not exist                            |
| 409    | Duplicate email, ISBN conflict, already borrowed, or invalid return      |
| 500    | Unexpected server error (details are not leaked to the client)           |

---

## Data model

- **Borrower** — `id` (generated), `name` (required), `email` (required, valid, unique).
- **Book** — `id` (generated), `isbn`, `title`, `author`, and a nullable link to the current borrower (`null` = available). An optimistic-lock `version` column guards against concurrent borrows.

**ISBN rules**

- Two books with the same title/author but different ISBNs are different books.
- Two books with the same ISBN must share the same title and author (enforced; violations return `409`).
- Multiple copies of the same ISBN are allowed; each copy gets its own id.

---

## Database choice justification

The application targets **two databases via Spring Data JPA**:

- **H2 (in-memory) — default.** Zero setup for a reviewer: clone, run, and every endpoint works immediately with no external service. Ideal for local development and automated tests.
- **MySQL — production-style profile.** A widely used, mature relational database. The data here is inherently relational (borrowers, books, and the borrow relationship with uniqueness and referential constraints), so a relational store with ACID transactions is the natural fit — it lets the database enforce the unique-email constraint and, combined with the `@Version` optimistic lock, guarantees a copy can be borrowed by only one member at a time.

Because access goes through JPA, the same code runs on either database; switching is purely a matter of configuration (the `mysql` profile), demonstrating environment portability.

---

## Testing & coverage

```bash
./mvnw clean verify
```

This runs:

- **Service unit tests** (Mockito) — ISBN new/matching/conflicting, borrow success/already-borrowed/not-found, return success/not-borrowed/wrong-borrower, duplicate email.
- **Controller web tests** (`@WebMvcTest`) — status codes and validation for every endpoint.
- **End-to-end test** (`@SpringBootTest` on H2) — full borrow/return flow plus failure cases.

The JaCoCo coverage report is generated at:

```
target/site/jacoco/index.html
```

---

## Docker

A multi-stage `Dockerfile` builds the jar and runs it on a slim JRE image as a non-root user. It runs standalone on H2 by default.

```bash
docker build -t library-management-api .
docker run -p 8080:8080 library-management-api
```

Run against MySQL by passing environment variables:

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=mysql \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/librarydb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  library-management-api
```

---

## Postman collection

Import `postman/library-management-api.postman_collection.json`. It covers every endpoint plus the full borrow/return flow, and automatically captures the generated `borrowerId` and `bookId` into collection variables. Set the `baseUrl` variable if you are not on `http://localhost:8080`.

---

## Assumptions

These fill gaps not explicitly specified by the task:

1. **No due dates, reservations, or fines** — borrowing/returning only.
2. **A borrower may hold multiple different books** at once; there is no per-member limit.
3. **Email uniquely identifies a borrower**; duplicate emails are rejected (`409`).
4. **Returning requires that the borrower actually holds the copy**; otherwise it is rejected (`409`).
5. **ISBN is not globally unique** — multiple copies share an ISBN, and each copy is a distinct book with its own id.
6. **Ids are server-generated** and returned to the client.
7. **No authentication/authorization** — out of scope for this exercise.
8. **No pagination** on the book list, and **no delete/update** endpoints (only the required actions).
9. A single "book" record represents one physical copy.
