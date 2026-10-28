# Employee Management REST API

A complete **Employee Management REST API** built with **Java 17 + Spring Boot 3** and **MySQL 8**, following a clean layered architecture (Controller → Service → Repository → Entity) with DTOs, Bean Validation and centralised exception handling.

| | |
|---|---|
| **Language / Framework** | Java 17, Spring Boot 3.2.5 |
| **Database** | MySQL 8 (Spring Data JPA / Hibernate 6) |
| **Build tool** | Maven |
| **Tests** | 15 automated tests — all passing |
| **API docs** | Swagger UI + OpenAPI 3 + Postman collection |

---

## 1. Features

| Requirement | Implementation |
|---|---|
| Create an employee | `POST /api/v1/employees` → `201 Created` + `Location` header |
| View all employees | `GET /api/v1/employees` → paginated list with search & department filter |
| View an employee by ID | `GET /api/v1/employees/{id}` → `200 OK`, `404` when missing |
| Update employee details | `PUT /api/v1/employees/{id}` → `200 OK`, full replacement |
| Delete an employee | `DELETE /api/v1/employees/{id}` → `204 No Content` |
| Input validation | Jakarta Bean Validation on the request DTO → `400` with per-field messages |
| Proper exception handling | `@RestControllerAdvice` → one consistent error envelope for 400/404/409/500 |
| MySQL integration | Spring Data JPA + Hibernate, schema auto-created, unique email constraint |

**Employee fields:** `firstName`, `lastName`, `email`, `department`, `position`, `salary`.

---

## 2. Architecture

```
HTTP request
     │
     ▼
┌─────────────────────────┐
│  Controller             │  HTTP mapping, status codes, no business logic
│  EmployeeController     │  (@Valid on the request body)
└───────────┬─────────────┘
            ▼
┌─────────────────────────┐
│  Service                │  business rules, duplicate checks,
│  EmployeeService        │  @Transactional boundaries
│  └ EmployeeServiceImpl  │
└───────────┬─────────────┘
            ▼
┌─────────────────────────┐
│  Repository             │  Spring Data JPA, derived queries, one JPQL search
│  EmployeeRepository     │
└───────────┬─────────────┘
            ▼
┌─────────────────────────┐
│  Entity                 │  JPA mapping → employees table
│  Employee               │
└─────────────────────────┘

Supporting layers
  dto/        EmployeeRequest, EmployeeResponse, PageResponse, ErrorResponse
  mapper/     EmployeeMapper      (entity ⇄ DTO)
  exception/  GlobalExceptionHandler, ResourceNotFoundException, DuplicateResourceException
  config/     OpenApiConfig       (Swagger metadata)
```

### Package structure

```
src/main/java/com/example/employeemanagement
├── EmployeeManagementApplication.java
├── config/
│   └── OpenApiConfig.java
├── controller/
│   └── EmployeeController.java
├── dto/
│   ├── EmployeeRequest.java          # inbound, carries validation rules
│   ├── EmployeeResponse.java         # outbound
│   ├── PageResponse.java             # stable pagination envelope
│   └── ErrorResponse.java            # single error shape
├── entity/
│   └── Employee.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── DuplicateResourceException.java
├── mapper/
│   └── EmployeeMapper.java
├── repository/
│   └── EmployeeRepository.java
└── service/
    ├── EmployeeService.java
    └── impl/EmployeeServiceImpl.java
```

**Layering rules enforced throughout the code**

- The `Employee` entity never leaves the service layer — the API only exposes DTOs.
- The controller contains no business logic and no `try/catch`; it delegates and shapes HTTP responses only.
- Validation annotations live on `EmployeeRequest`, so every entry point shares the same rules.
- The service is an interface with one implementation — easy to mock, easy to replace.
- Every failure produces the same JSON error envelope, generated centrally.

---

## 3. Database configuration (MySQL)

**Connection settings** — `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/employee_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        auto_quote_keyword: true
```

| Setting | Value |
|---|---|
| Host / port | `localhost:3306` |
| Database name | `employee_db` |
| Username | `root` (override with `DB_USERNAME`) |
| Password | `root` (override with `DB_PASSWORD`) |
| JDBC URL | `jdbc:mysql://localhost:3306/employee_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| DDL mode | `update` — Hibernate creates/updates the table on startup |

Credentials are externalised: environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` override the defaults, so no secrets need to be committed.

**Create the database** (one-time):

```sql
CREATE DATABASE IF NOT EXISTS employee_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The `employees` table is then created automatically on the first application start. To create it manually instead, run `db/schema-mysql.sql`.

**Table definition:**

```sql
CREATE TABLE employees (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    first_name  VARCHAR(50)   NOT NULL,
    last_name   VARCHAR(50)   NOT NULL,
    email       VARCHAR(120)  NOT NULL,
    department  VARCHAR(60)   NOT NULL,
    position    VARCHAR(60)   NOT NULL,
    salary      DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_employee_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

The `UNIQUE` constraint on `email` is the real guard against duplicates — the service check only produces a friendly message.

---

## 4. Setup and run

### Prerequisites

- JDK 17
- Maven 3.6+ (or the Eclipse/IntelliJ bundled Maven)
- MySQL 8 running on `localhost:3306`

### Steps

```bash
# 1. clone
git clone <your-repo-url>
cd employee-management-api

# 2. create the database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS employee_db CHARACTER SET utf8mb4;"

# 3. (optional) point at different credentials
export DB_USERNAME=root
export DB_PASSWORD=your_password

# 4. run
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

Quick verification:

```bash
curl http://localhost:8080/api/v1/employees
# {"content":[],"page":0,"size":20,"totalElements":0,"totalPages":0,"last":true}
```

### Docker alternative for MySQL

```bash
docker compose up -d      # MySQL 8 on :3306, database employee_db, root/root
mvn spring-boot:run
```

### Tests (no database required — uses in-memory H2)

```bash
mvn test
# Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

### API documentation

| Resource | URL |
|---|---|
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| Postman collection | `Employee-Management-API.postman_collection.json` |

---

## 5. API reference

Base URL: `http://localhost:8080/api/v1/employees` · No authentication required.

| Method | Endpoint | Success | Description |
|---|---|---|---|
| POST | `/api/v1/employees` | `201` | Create an employee |
| GET | `/api/v1/employees` | `200` | List employees (paged, searchable) |
| GET | `/api/v1/employees/{id}` | `200` | Get one employee |
| PUT | `/api/v1/employees/{id}` | `200` | Update an employee |
| DELETE | `/api/v1/employees/{id}` | `204` | Delete an employee |

### 5.1 Create — `POST /api/v1/employees`

**Request**

```http
POST /api/v1/employees
Content-Type: application/json
```

```json
{
  "firstName": "Grace",
  "lastName": "Hopper",
  "email": "grace@example.com",
  "department": "Engineering",
  "position": "Backend Developer",
  "salary": 62000.00
}
```

**Response — `201 Created`** (`Location: http://localhost:8080/api/v1/employees/1`)

```json
{
  "id": 1,
  "firstName": "Grace",
  "lastName": "Hopper",
  "fullName": "Grace Hopper",
  "email": "grace@example.com",
  "department": "Engineering",
  "position": "Backend Developer",
  "salary": 62000.00
}
```

### 5.2 List — `GET /api/v1/employees`

**Request**

```http
GET /api/v1/employees?page=0&size=20&sort=lastName,asc
```

| Query parameter | Example | Meaning |
|---|---|---|
| `search` | `?search=hop` | case-insensitive match on first name, last name or email |
| `department` | `?department=Engineering` | exact, case-insensitive department filter |
| `page` / `size` | `?page=0&size=20` | pagination |
| `sort` | `?sort=lastName,asc` | any entity property |

**Response — `200 OK`**

```json
{
  "content": [
    {
      "id": 1,
      "firstName": "Grace",
      "lastName": "Hopper",
      "fullName": "Grace Hopper",
      "email": "grace@example.com",
      "department": "Engineering",
      "position": "Backend Developer",
      "salary": 62000.00
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

### 5.3 Get by id — `GET /api/v1/employees/{id}`

**Response — `200 OK`** — same object as above.

### 5.4 Update — `PUT /api/v1/employees/{id}`

Full replacement: all six fields are required.

**Request**

```json
{
  "firstName": "Grace",
  "lastName": "Hopper",
  "email": "grace@example.com",
  "department": "Research",
  "position": "Principal Engineer",
  "salary": 95000.00
}
```

**Response — `200 OK`** — the updated employee.

### 5.5 Delete — `DELETE /api/v1/employees/{id}`

**Response — `204 No Content`**, empty body.

---

## 6. Input validation

All rules are declared on `EmployeeRequest` and enforced by `@Valid`:

| Field | Rules |
|---|---|
| `firstName` | `@NotBlank`, 2–50 characters |
| `lastName` | `@NotBlank`, 2–50 characters |
| `email` | `@NotBlank`, `@Email`, max 120 characters, **must be unique** |
| `department` | `@NotBlank`, max 60 characters |
| `position` | `@NotBlank`, max 60 characters |
| `salary` | `@NotNull`, greater than 0, max 10 integer digits + 2 decimals |

Emails are trimmed and lower-cased before saving, so `Grace@Example.com` and `grace@example.com` cannot both exist.

**`400 Bad Request` response:**

```json
{
  "timestamp": "2024-05-11T10:16:02.441",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/employees",
  "fieldErrors": {
    "firstName": "First name must be between 2 and 50 characters",
    "email": "Email must be a valid email address",
    "salary": "Salary must be greater than 0"
  }
}
```

---

## 7. Exception handling

One `@RestControllerAdvice` translates every exception into the same `ErrorResponse` shape, so clients never have to guess.

| Situation | HTTP status | Handler |
|---|---|---|
| Bean validation fails on `@Valid` body | `400` | `MethodArgumentNotValidException` |
| Malformed JSON / wrong field type | `400` | `HttpMessageNotReadableException` |
| Non-numeric id in path (`/employees/abc`) | `400` | `MethodArgumentTypeMismatchException` |
| Employee id does not exist | `404` | `ResourceNotFoundException` |
| Duplicate email | `409` | `DuplicateResourceException` |
| DB unique constraint violation (race condition) | `409` | `DataIntegrityViolationException` |
| Unexpected error | `500` | `Exception` — details logged, never leaked |

**`404 Not Found`**

```json
{
  "timestamp": "2024-05-11T10:16:40.001",
  "status": 404,
  "error": "Not Found",
  "message": "Employee not found with id: 9999",
  "path": "/api/v1/employees/9999"
}
```

**`409 Conflict`**

```json
{
  "timestamp": "2024-05-11T10:17:11.220",
  "status": 409,
  "error": "Conflict",
  "message": "An employee with email 'grace@example.com' already exists",
  "path": "/api/v1/employees"
}
```

**`500 Internal Server Error`** — the client sees only:

```json
{
  "timestamp": "2024-05-11T10:18:03.118",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please try again later.",
  "path": "/api/v1/employees"
}
```

while the full stack trace is written to the server log.

---

## 8. Tests

`mvn test` → **15 tests, 0 failures**.

**Unit tests — `EmployeeServiceImplTest`** (JUnit 5 + Mockito, repository mocked)

1. `create` saves the employee and returns the generated id
2. `create` rejects a duplicate email (`DuplicateResourceException`)
3. `findById` throws `ResourceNotFoundException` for an unknown id
4. `update` overwrites the mutable fields
5. `delete` removes an existing employee
6. `delete` throws for an unknown id

**Integration tests — `EmployeeControllerIntegrationTest`** (`@SpringBootTest` + MockMvc + H2, full HTTP stack)

1. `POST` returns `201` with a `Location` header
2. `POST` with an invalid body returns `400` with per-field errors
3. `POST` duplicate email returns `409`
4. `GET /{id}` returns the employee
5. `GET` unknown id returns `404` with the path echoed in the body
6. `GET` list supports pagination and department filtering
7. `PUT` updates and returns the new values
8. `PUT` unknown id returns `404`
9. `DELETE` returns `204`, a second `DELETE` returns `404`

---

## 9. Postman collection

`Employee-Management-API.postman_collection.json` contains 11 requests grouped into Create / Read / Update / Delete / Error cases, each with an assertion on the expected status code.

1. Postman → **Import → File** → select the collection.
2. Check the collection variable `baseUrl` = `http://localhost:8080`.
3. Reset ids so `/{id}` requests line up:
   ```sql
   TRUNCATE TABLE employees;
   ```
4. Click **… → Run collection** → all requests should pass.

---



- **DTOs instead of exposing the entity.** Prevents mass assignment (`id` cannot be injected), keeps the JSON contract independent of the schema, and puts validation on the input model rather than the persistence model.
- **Service interface + implementation.** The controller depends on the abstraction, which makes unit testing with Mockito trivial and allows alternative implementations.
- **Validation on the DTO, not the entity.** The entity represents persisted state; the DTO represents what a client is allowed to send.
- **Email uniqueness enforced twice.** A service-level check produces a clean `409` message; the database `UNIQUE` constraint is the actual guarantee under concurrency, and `DataIntegrityViolationException` maps to the same `409`.
- **One error envelope.** Clients parse a single structure for every failure, and a `fieldErrors` map is present only for validation errors.
- **Pagination envelope (`PageResponse`)** instead of serialising Spring's `Page`, whose JSON shape is an implementation detail that changes between versions.
- **Constructor injection** everywhere — no field injection, dependencies are explicit and the classes are testable.
- **No Lombok.** DTOs are Java records (immutable, concise); the entity keeps explicit accessors so the project compiles in any IDE without annotation processor setup.
- **`@Transactional(readOnly = true)` at class level**, overridden on write methods, so read paths avoid unnecessary dirty checking.
- **`ddl-auto: update` for this assessment**, with `validate` + Flyway/Liquibase named as the production alternative.

---



- `PATCH /employees/{id}` for partial updates
- separate `Department` entity with a JPA relationship
- optimistic locking with `@Version`
- Spring Security with role-based access
- Actuator health/metrics endpoints
- Testcontainers-based MySQL integration test
