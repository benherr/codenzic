# Submission package

Everything the assessment email asks for, and where to find it.

| # | Required | Provided by |
|---|---|---|
| 1 | GitHub repository link | Push this folder — commands below |
| 2 | README with setup instructions | `README.md` (setup, run, DB config, API reference) |
| 3 | API documentation or Postman collection | Swagger UI at `/swagger-ui.html` + `Employee-Management-API.postman_collection.json` |
| 4 | Database configuration details | `README.md` §3, `src/main/resources/application.yml`, `db/schema-mysql.sql` |
| 5 | Sample request and response | `README.md` §5 (all five endpoints), plus the Postman collection |

---

## 1. Push to GitHub

Create an empty repository on github.com (no README, no .gitignore), then:

```bash
cd employee-management-api
git init
git add .
git commit -m "Employee Management REST API - Spring Boot 3, MySQL, layered architecture"
git branch -M main
git remote add origin https://github.com/<your-username>/employee-management-api.git
git push -u origin main
```

Send the repository URL. `.gitignore` already excludes `target/` and IDE files, so no build output is committed.

Optional but recommended: add a screenshot of Swagger UI and of the `employees` table to a `docs/screenshots/` folder — visual proof of a working app in the README.

---

## 2. What the reviewer will see first

`README.md`, in this order: features table → architecture diagram → database configuration → setup commands → API reference with real request/response bodies → validation → exception handling → tests → design decisions.

If they only read one section, make it §2 (architecture) and §10 (design decisions) — the email says they score **code organization**, **API design**, **database integration**, **error handling**, and **ability to explain your own implementation**.

---

## 3. Their evaluation criteria → your evidence

| They assess | Point them here |
|---|---|
| Code organization and structure | Package tree in `README.md` §2; one class per responsibility; controller has no `try/catch`, service has no HTTP types |
| Understanding of Spring Boot architecture | Layer diagram; constructor injection; `@Transactional` placement; `@RestControllerAdvice` |
| API design approach | `README.md` §5 — status codes (201 + `Location`, 204 on delete), pagination envelope, filtering, versioned base path `/api/v1` |
| Database integration | `Entity` mapping, `JpaRepository` + derived queries + JPQL, unique constraint, `application.yml` externalised credentials |
| Error handling | `GlobalExceptionHandler` + the status-code table in §7, including the DB-constraint → 409 fallback |
| Problem-solving approach | Design decisions §10 (why DTOs, why double uniqueness check, why `PageResponse`) |
| Ability to explain your implementation | `docs/ASSESSMENT-NOTES.md` — demo script and rehearsed answers |

---

## 4. Before you send it — final checklist

- [ ] `mvn test` → `Tests run: 15, Failures: 0, Errors: 0`
- [ ] `mvn spring-boot:run` → starts on port 8080 without errors
- [ ] `http://localhost:8080/swagger-ui.html` loads and lists 5 endpoints
- [ ] All five operations tried once in Swagger or Postman
- [ ] `SELECT * FROM employees;` returns rows in MySQL
- [ ] No password committed to `application.yml` beyond the local `root/root` default
- [ ] `target/` not committed
- [ ] README renders correctly on GitHub (tables, code blocks)
- [ ] `docs/ASSESSMENT-NOTES.md` read once — you can explain every layer

---

## 5. Database details (copy-paste for the submission form)

```
Database  : MySQL 8
Host/Port : localhost:3306
Schema    : employee_db
Table     : employees
Username  : root
Password  : root
JDBC URL  : jdbc:mysql://localhost:3306/employee_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DDL mode  : spring.jpa.hibernate.ddl-auto=update (table auto-created on startup)
Init SQL  : db/schema-mysql.sql (optional manual creation)
```

Credentials are overridable through the `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` environment variables.
