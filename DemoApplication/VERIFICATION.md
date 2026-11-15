# Delivery verification record

## Source checks completed

- Reviewed the Maven parent and service module POMs, service application configuration, entity/repository/service/controller layers, REST clients, JWT/CORS configuration, Flyway migrations, README documentation, and the included unit/controller tests.
- Confirmed that the two services use independent MySQL schemas and Flyway migrations, with Hibernate schema validation enabled.
- Confirmed there are no H2 runtime dependencies or H2 JDBC configurations in the backend source.
- Confirmed pagination/search endpoints exist for employees and departments and that supported sort fields are allow-listed.
- Added a department deletion guard: a department with assigned employees now returns HTTP 409 rather than leaving employee records with a dangling department ID.
- Added/updated local setup and API documentation, including environment-variable placeholders and the separate database URLs.

## Checks blocked by this execution environment

- `mvn clean test` could not start because Maven is not installed in the execution environment (`mvn: command not found`).
- Angular production build could not complete. The ZIP's original `node_modules` contained a Windows-only esbuild binary, and a clean `npm ci` could not finish within the available execution window. The ZIP intentionally excludes `node_modules`; run `npm ci` on the target machine before `npm run build`.
- No local MySQL server or credentials were available, so live migration, connectivity, and cross-service end-to-end verification were not performed.

## Required local verification

From `DemoApplication/` run `mvn clean verify` with JDK 17+ and Maven 3.9+. From `unified-employee-department-ui/` run `npm ci`, `npm run build`, and `npm test -- --watch=false`. Then start MySQL, create the two schemas, set the environment variables in `.env.example`, and start both services as described in `README.md`.
