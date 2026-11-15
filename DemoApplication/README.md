# PeopleHub — Employee & Department Management

PeopleHub is a Java 17 / Spring Boot microservices application with an Angular 19 dashboard. The two services own separate MySQL schemas and communicate through REST. The Employee Service owns employee records and department membership; the Department Service owns department records.

## Project layout

- `DemoApplication/employee-service` — employee CRUD, search, pagination, department membership/counts, JWT validation, Department Service REST client.
- `DemoApplication/department-service` — department CRUD, search, pagination, department summaries aggregated from Employee Service.
- `DemoApplication/core-java` — preserved Core Java assignment examples.
- `unified-employee-department-ui` — Angular 19 frontend.
- `DemoApplication/documentation` — architecture, API catalog and local run guide.
- `DemoApplication/postman` — existing Postman collection.

## Prerequisites

- JDK 17 or later
- Maven 3.9+
- Node.js 20+ and npm
- MySQL 8.0+
- A browser

## 1. Configure MySQL

Create the two independent schemas using a MySQL account with database-creation privileges:

```sql
CREATE DATABASE employee_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE department_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

The equivalent statements are in `DemoApplication/database-setup.sql`. The application database user must have privileges to create and alter tables in both schemas. Flyway creates the tables and indexes on first startup; Hibernate validates the schema (`ddl-auto: validate`).

Copy `DemoApplication/.env.example` values into your shell or IDE run configurations. Replace the placeholders; do not commit real credentials. Both services must share the same `JWT_SECRET` (at least 32 bytes). Configure the service-specific `DB_URL`:

- Employee Service: `jdbc:mysql://localhost:3306/employee_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- Department Service: `jdbc:mysql://localhost:3306/department_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`

Required variables: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`. Optional variables: `DB_URL`, `JWT_EXPIRATION_MINUTES`, `CORS_ALLOWED_ORIGINS`, `EMPLOYEE_SERVICE_URL`, `DEPARTMENT_SERVICE_URL`.

## 2. Build and test backend

From `DemoApplication/`:

```bash
mvn clean verify
```

## 3. Start backend services

Start each service in a separate terminal from `DemoApplication/`, supplying the environment variables above:

```bash
mvn -pl department-service spring-boot:run
mvn -pl employee-service spring-boot:run
```

The Department Service defaults to port `8082`; Employee Service defaults to `8081`. Set `DB_URL` separately for each process to its own schema.

Swagger:
- http://localhost:8081/swagger-ui/index.html
- http://localhost:8082/swagger-ui/index.html

## 4. Start frontend

From `unified-employee-department-ui/`:

```bash
npm ci
npm start
```

Open http://localhost:4200. API origins can be overridden before bootstrap by defining `window.__PEOPLEHUB_CONFIG__` in `src/index.html` with `employeeApi` and `departmentApi` properties. Defaults are localhost ports 8081 and 8082.

## Demo authentication

The existing training-only `admin/admin123` and `user/user123` credentials are retained for the demo flow. Do not expose these credentials or the demo authentication implementation in production. Replace it with a proper identity provider and securely stored user credentials before deployment.

## API highlights

- Employee CRUD: `GET/POST /api/employees`, `GET/PUT/DELETE /api/employees/{id}`
- Employee search/pagination: `GET /api/employees/search?q=&departmentId=&page=&size=&sort=`
- Employees in department: `GET /api/employees/department/{id}/employees`
- Department employee count: `GET /api/employees/department/{id}/count`
- Department details plus employees: `GET /api/employees/department/{id}/details`
- Employee counts grouped by department: `GET /api/employees/department-counts`
- Department CRUD: `GET/POST /api/departments`, `GET/PUT/DELETE /api/departments/{id}`
- Department search/pagination: `GET /api/departments/search?q=&page=&size=&sort=`
- All departments with employee counts: `GET /api/departments/with-employee-counts`

All business endpoints require `Authorization: Bearer <JWT>`. ADMIN can read and write; USER can read. Pagination defaults to 20 rows and is capped at 100. Department deletion is protected: the Department Service checks Employee Service and refuses deletion with HTTP 409 while members remain assigned. Supported employee sort fields: `name`, `email`, `id`, `departmentId`; department sort fields: `name`, `location`, `id`.

## Data ownership and relationships

Employee Service stores `department_id` as a logical reference, not a cross-database foreign key. Department Service remains the source of truth for department records. The employee service exposes employee membership and count endpoints; Department Service aggregates counts over REST. This avoids coupling independent service databases.

## Notes

- The original Core Java module, authentication demonstration, CRUD screens, role-based read-only behavior and RestClient integration demonstration are retained.
- The application must be run against a local MySQL server to verify actual database connectivity. H2 has been removed from both runtime dependency graphs and configurations.

## Verification scope

The source includes unit tests and Spring MVC controller-slice tests. This delivery environment did not have Maven installed and could not complete a clean Angular dependency installation, so a full Maven test run and Angular production build could not be verified here. The uploaded ZIP is source-only (generated `target`, `dist`, and `node_modules` directories are excluded); install dependencies locally and run the commands above. A live MySQL connectivity check must be performed on the machine hosting MySQL.
