# Unified Employee and Department Microservices

## 1. Project Overview

This project demonstrates two independently running, JWT-secured Spring Boot microservices:

| Service | Port | Responsibility |
|---|---:|---|
| Employee Service | `8081` | Owns employee data and exposes employee CRUD APIs |
| Department Service | `8082` | Owns department data and exposes department CRUD APIs |

The Employee Service also provides a department lookup endpoint. It calls the Department Service with Spring `RestClient` and forwards the caller's `Authorization` header. Each service owns an independent H2 database.

### Main features

- Employee and Department CRUD APIs
- JWT token generation
- JWT validation before protected controller execution
- Role-based authorization for `ADMIN` and `USER`
- Secure service-to-service communication with JWT propagation
- Database-per-service design
- H2 in-memory databases
- Swagger/OpenAPI documentation
- Spring Data JPA
- Structured logging and exception handling

---

## 2. Important Implementation Notes

This README documents the behavior visible in the supplied source files and OpenAPI definitions.

1. Demo credentials are checked directly in `AuthController`; authentication is not database-backed.
2. `/auth/token` is public because `/auth/**` is excluded by `JwtFilter.shouldNotFilter()`.
3. `JwtFilter` is registered with order `1`, so it runs before protected controller logic.
4. The token contains the username as the subject and a `role` claim. It also contains issued-at and expiration values and is signed with the configured HMAC secret.
5. Both services must use a compatible JWT secret if the same token is to be accepted by both services.
6. `USER` can perform `GET` requests only. Non-GET operations require `ADMIN`.
7. `DepartmentClient` forwards the original `Authorization` header. It does not create a second token.
8. The current filter treats every non-GET protected request as a write operation. Therefore `POST`, `PUT`, `PATCH`, and `DELETE` require `ADMIN`.

---

## 3. Technology Stack

- Java 21
- Spring Boot 3.2+
- Spring Web
- Spring Data JPA
- Spring `RestClient`
- JJWT
- H2 Database
- Swagger/OpenAPI 3
- Maven
- SLF4J

---

## 4. High-Level Architecture

```text
+---------------------------+
| Client                    |
| Swagger UI / Postman      |
+-------------+-------------+
              |
              | 1. POST /auth/token
              | 2. Receive JWT
              | 3. Send Authorization: Bearer <JWT>
              v
+-------------+-------------+
| Employee Service :8081    |
|                           |
|  +---------------------+  |
|  | JwtFilter (order 1) |  |
|  +----------+----------+  |
|             |             |
|  +----------v----------+  |
|  | Employee APIs       |  |
|  +----------+----------+  |
|             |             |
|  +----------v----------+  |
|  | Employee H2 DB      |  |
|  +---------------------+  |
|             |             |
|  DepartmentClient         |
+-------------+-------------+
              |
              | RestClient GET /api/departments/{id}
              | Forward same Authorization header
              v
+-------------+-------------+
| Department Service :8082  |
|                           |
|  +---------------------+  |
|  | JwtFilter (order 1) |  |
|  +----------+----------+  |
|             |             |
|  +----------v----------+  |
|  | Department APIs     |  |
|  +----------+----------+  |
|             |             |
|  +----------v----------+  |
|  | Department H2 DB    |  |
|  +---------------------+  |
+---------------------------+
```
---

## 5. Authentication Is the First Step

### Demo credentials

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `user` | `user123` | `USER` |

These credentials are intended for demonstration only. Production systems should use a user store, hashed passwords, secret management, and an identity provider such as Microsoft Entra ID, Keycloak, or Okta.

### Authentication flow diagram

```text
+--------+
| Client |
+---+----+
    |
    | POST /auth/token
    | { username, password }
    v
+---+---------------------------+
| JwtFilter                     |
| /auth/** matches bypass rule  |
+---+---------------------------+
    |
    v
+---+---------------------------+
| AuthController                |
| Check demo credentials        |
+---+---------------------------+
    |
    | Invalid credentials
    +------------------------------> 401 Unauthorized
    |
    | Valid credentials
    v
+---+---------------------------+
| JwtService.issue()            |
| sub  = username               |
| role = ADMIN or USER          |
| iat  = issue time             |
| exp  = configured expiration  |
| sign = app.jwt.secret         |
+---+---------------------------+
    |
    v
+---+---------------------------------------+
| 200 OK                                    |
| { "accessToken": "...",                   |
|   "tokenType": "Bearer" }                 |
+-------------------------------------------+
```

### Authentication sequence diagram

```text
Client          JwtFilter        AuthController       JwtService
  |                 |                  |                  |
  | POST /auth/token|                  |                  |
  |---------------->|                  |                  |
  |                 | bypass /auth/**  |                  |
  |                 |----------------->|                  |
  |                 |                  | check credentials|
  |                 |                  |                  |
  |                 |                  | issue(user, role)|
  |                 |                  |----------------->|
  |                 |                  |<-----------------|
  |                 |                  | signed JWT       |
  |<------------------------------------------------------|
  | 200 accessToken + tokenType=Bearer                    |
```
---

## 6. JWT Contents and Configuration

The generated JWT contains these claims:

```json
{
  "sub": "admin",
  "role": "ADMIN",
  "iat": 1710000000,
  "exp": 1710003600
}
```

The timestamps above are illustrative. Actual values are generated at runtime.

Relevant configuration:

```properties
app.jwt.secret=<shared-secret-with-sufficient-length>
app.jwt.expiration-minutes=60
department.service-url=http://localhost:8082
```

`app.jwt.expiration-minutes` defaults to `60` when it is not configured. The HMAC key is created from `app.jwt.secret`.

### Security consideration

For the forwarded employee token to validate in Department Service, both services must use the same compatible JWT signing secret. Keep that secret outside source control, for example in environment variables, Kubernetes Secrets, Azure Key Vault, or HashiCorp Vault.

---

## 7. Public and Protected Endpoints

### Endpoints that bypass `JwtFilter`

```text
/auth/**
/swagger-ui/**
/v3/api-docs/**
/actuator/health
/h2-console
```

### Bypass decision diagram

```text
Incoming request
       |
       v
+------+------------------+
| Path matches bypass?    |
+------+------------------+
       |
   Yes |                         No
       v                          v
Continue without JWT       Require Bearer JWT
validation                  and apply role rules
```

> Note: The exact H2 console path is normally `/h2-console/**`. The current code checks equality with `/h2-console`, so nested H2 console resources may still pass through the filter. See the review notes near the end of this document.

---

## 8. Protected Request and Authorization Flow

### Authorization decision diagram

```text
Protected request
       |
       v
Authorization header exists and starts with "Bearer "?
       |
   No  +------------------------------> 401 Bearer token required
       |
      Yes
       v
Parse token, verify signature/expiry, read role
       |
Failure+------------------------------> 401 Invalid or expired JWT
       |
    Success
       v
Role is ADMIN or USER?
       |
   No  +------------------------------> 403 Unsupported role
       |
      Yes
       v
HTTP method is GET?
       |
  Yes  +------------------------------> Continue filter chain
       |
      No
       v
Role is ADMIN?
       |
  Yes  +------------------------------> Continue filter chain
       |
   No  +------------------------------> 403 Admin role required
```

### Authorization matrix

| Request type | `ADMIN` | `USER` |
|---|---:|---:|
| `GET` | Allowed | Allowed |
| `POST` | Allowed | Forbidden, `403` |
| `PUT` | Allowed | Forbidden, `403` |
| `PATCH` | Allowed | Forbidden, `403` |
| `DELETE` | Allowed | Forbidden, `403` |

### Protected CRUD request sequence

```text
Client       JwtFilter       Controller       Service       Repository       H2 DB
  |              |               |               |               |             |
  | request+JWT  |               |               |               |             |
  |------------->|               |               |               |             |
  |              | verify token  |               |               |             |
  |              | check role    |               |               |             |
  |              |-------------->|               |               |             |
  |              |               | invoke        |               |             |
  |              |               |-------------->|               |             |
  |              |               |               | JPA operation |             |
  |              |               |               |-------------->|             |
  |              |               |               |               | query/save  |
  |              |               |               |               |------------>|
  |              |               |               |               |<------------|
  |<---------------------------------------------------------------------------|
  | JSON response / HTTP status                                                |
```

### How to explain authorization in the demo

1. Show a successful `GET` using a `USER` token.
2. Attempt a `POST` using the same `USER` token and show `403`.
3. Obtain an `ADMIN` token.
4. Repeat the `POST` and show the successful `201 Created` response.
5. Explain that authorization is enforced in `JwtFilter` before controller execution.

---

## 9. Secure Employee-to-Department Lookup

The key integration endpoint is:

```http
GET /api/employees/department/{id}
Authorization: Bearer <JWT>
```

`DepartmentClient` builds a `RestClient` using `department.service-url`, calls `/api/departments/{id}`, and forwards the incoming `Authorization` header.

### End-to-end flow diagram

```text
+--------+
| Client |
+---+----+
    |
    | GET /api/employees/department/10
    | Authorization: Bearer <JWT>
    v
+---+-------------------------+
| Employee JwtFilter          |
| Validate token and USER /   |
| ADMIN role                  |
+---+-------------------------+
    |
    v
+---+-------------------------+
| Employee Department API     |
+---+-------------------------+
    |
    v
+---+-------------------------+
| DepartmentClient            |
| RestClient                  |
+---+-------------------------+
    |
    | GET http://localhost:8082/api/departments/10
    | Forward same Authorization header
    v
+---+-------------------------+
| Department JwtFilter        |
| Validate token again        |
+---+-------------------------+
    |
    v
+---+-------------------------+
| Department Controller       |
+---+-------------------------+
    |
    v
+---+-------------------------+
| Department Service / JPA    |
+---+-------------------------+
    |
    v
+---+-------------------------+
| Department H2 Database      |
+---+-------------------------+
    |
    | Department JSON
    v
Department Service -> DepartmentClient -> Employee API -> Client
```

### Department lookup sequence diagram

```text
Client     Employee       Employee API     DepartmentClient     Department       Department API/DB
  |        JwtFilter           |                 |               JwtFilter              |
  | GET + Bearer JWT           |                 |                   |                   |
  |---------->|                |                 |                   |                   |
  |           | validate       |                 |                   |                   |
  |           |--------------->|                 |                   |                   |
  |           |                | getDepartment() |                   |                   |
  |           |                |---------------->|                   |                   |
  |           |                |                 | GET + same JWT    |                   |
  |           |                |                 |------------------>|                   |
  |           |                |                 |                   | validate          |
  |           |                |                 |                   |------------------>|
  |           |                |                 |<--------------------------------------|
  |           |                |<----------------| Department JSON                       |
  |<-------------------------------------------------------------------------------------|
  | Department JSON                                                                      |
```
---

## 10. Data Model and Service Ownership

### Employee model

```text
EMPLOYEE
+----------------+----------------+
| Field          | Type           |
+----------------+----------------+
| id             | BIGINT / Long  |
| name           | String         |
| email          | String         |
| departmentId   | BIGINT / Long  |
+----------------+----------------+
```

### Department model

```text
DEPARTMENT
+------------+----------------+
| Field      | Type           |
+------------+----------------+
| id         | BIGINT / Long  |
| name       | String         |
| location   | String         |
+------------+----------------+
```

### Logical relationship

```text
+-----------------------+       logical reference       +-----------------------+
| Employee Service      | employee.departmentId ------> | Department Service    |
| Employee H2 DB        |                               | Department H2 DB      |
+-----------------------+                               +-----------------------+

No cross-service database foreign key and no cross-service database join.
```
---

## 11. API Documentation

## 11.1 Employee Service

Base URL:

```text
http://localhost:8081
```

### Authentication

#### Generate token

```http
POST /auth/token
Content-Type: application/json
```

Request:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Successful response:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer"
}
```

### Employee endpoints

| Method | Endpoint | Purpose | Success |
|---|---|---|---:|
| `GET` | `/api/employees` | Get all employees | `200` |
| `POST` | `/api/employees` | Create an employee | `201` |
| `GET` | `/api/employees/{id}` | Get one employee | `200` |
| `PUT` | `/api/employees/{id}` | Update an employee | `200` |
| `DELETE` | `/api/employees/{id}` | Delete an employee | `204` |
| `GET` | `/api/employees/department/{id}` | Retrieve a department through Department Service | `200` |

Employee payload:

```json
{
  "id": 1,
  "name": "Anita",
  "email": "anita@example.com",
  "departmentId": 10
}
```

The `Authorization` header is explicitly described in OpenAPI for `/api/employees/department/{id}`. In runtime behavior, all protected endpoints require the header because `JwtFilter` enforces it globally unless the path is bypassed.

## 11.2 Department Service

Base URL:

```text
http://localhost:8082
```

### Authentication

```http
POST /auth/token
Content-Type: application/json
```

### Department endpoints

| Method | Endpoint | Purpose | Success |
|---|---|---|---:|
| `GET` | `/api/departments` | Get all departments | `200` |
| `POST` | `/api/departments` | Create a department | `201` |
| `GET` | `/api/departments/{id}` | Get one department | `200` |
| `PUT` | `/api/departments/{id}` | Update a department | `200` |
| `DELETE` | `/api/departments/{id}` | Delete a department | `204` |

Department payload:

```json
{
  "id": 10,
  "name": "Engineering",
  "location": "Pune"
}
```

---

## 12. HTTP Status Behavior

| Situation | Expected status | Source of behavior |
|---|---:|---|
| Valid request | `200`, `201`, or `204` | Controller/OpenAPI contract |
| Invalid login credentials | `401` | `AuthController` |
| Missing or malformed Bearer header | `401` | `JwtFilter` |
| Invalid, wrongly signed, or expired JWT | `401` | `JwtFilter` catch block |
| Token role is not `ADMIN` or `USER` | `403` | `JwtFilter` |
| `USER` performs a non-GET operation | `403` | `JwtFilter` |
| Employee or department is missing | `404` | Project API contract |
| Department REST invocation fails | Application error | `DepartmentClient` wraps the client failure in `IllegalStateException` |

---

## 13. Local Demo Order

1. Start Department Service on port `8082`.
2. Start Employee Service on port `8081`.
3. Open both Swagger UIs.
4. Call Employee Service `POST /auth/token` with `user/user123`.
5. Use the USER token for `GET /api/employees` and show success.
6. Use the USER token for `POST /api/employees` and show `403`.
7. Call `POST /auth/token` with `admin/admin123`.
8. Use the ADMIN token to create a department in Department Service.
9. Use the ADMIN token to create an employee with that `departmentId`.
10. Call `GET /api/employees/department/{id}` using either valid role.
11. Explain that Employee Service forwards the same JWT to Department Service.
12. Optionally remove the header or use an expired token to demonstrate `401`.

### Example demo data

Create department:

```json
{
  "name": "Engineering",
  "location": "Pune"
}
```

Create employee:

```json
{
  "name": "Anita",
  "email": "anita@example.com",
  "departmentId": 10
}
```

---

## 14. Demo
---

## 15. Code Review Findings and Recommended Improvements

These points are useful if the demo includes design discussion.

### 15.1 Use a real identity provider for production

Current credentials are hardcoded for demonstration. For production, use Microsoft Entra ID, Keycloak, Okta, or a database-backed user service with BCrypt/Argon2 password hashing.

### 15.2 Avoid exposing secrets in source control

Load `app.jwt.secret` from an environment variable or secret manager. Both services must share the verification secret in the current symmetric-key design.

### 15.3 Prefer standard Spring Security configuration

The current project performs authentication and authorization in a custom servlet filter. A production design would normally define a Spring Security `SecurityFilterChain`, configure stateless sessions, use an authentication entry point/access denied handler, and potentially use OAuth 2.0 Resource Server JWT support.

### 15.4 Avoid double filter registration

`JwtFilter` is a `@Component` and is also registered with a `FilterRegistrationBean`. Depending on Spring Boot registration behavior, this pattern can cause duplicate registration. Prefer one registration strategy and test that the filter runs exactly once per request.

### 15.5 Improve the H2 bypass rule

The current rule uses `p.equals("/h2-console")`. H2 commonly loads resources below `/h2-console/`. If console access is intended, use a controlled development-only matcher such as `/h2-console/**`. Never expose the H2 console in production.

### 15.6 Document security in OpenAPI consistently

Instead of declaring `Authorization` manually on one endpoint, define an OpenAPI HTTP Bearer security scheme and apply it globally to protected endpoints. This makes Swagger UI's **Authorize** button work consistently.

### 15.7 Return typed DTOs

`DepartmentClient` currently returns `Object` and deserializes to `Map.class`. A `DepartmentResponse` DTO provides compile-time safety and clearer API contracts.

### 15.8 Preserve downstream status information

Wrapping every `RestClientException` in `IllegalStateException` can hide whether Department Service returned `401`, `403`, `404`, or `500`. Map downstream errors to meaningful upstream responses, for example `404`, `502 Bad Gateway`, or `503 Service Unavailable`.

### 15.9 Add resilience controls

For production service-to-service calls, configure connection/read timeouts and consider Resilience4j circuit breaker, retry, and bulkhead policies. Do not retry authorization failures or validation errors.

### 15.10 Add automated tests

Recommended tests:

- `/auth/token` succeeds for both demo users.
- Invalid credentials return `401`.
- Missing, malformed, expired, and wrongly signed JWTs return `401`.
- Unsupported roles return `403`.
- USER GET succeeds and USER write returns `403`.
- ADMIN CRUD succeeds.
- Authorization header is forwarded by `DepartmentClient`.
- Department Service errors are mapped correctly.
- Public endpoint bypass rules work without exposing unintended paths.

---

## 16. Demo Closing Points

- Authentication happens before protected API use.
- Authorization is enforced before controller execution.
- ADMIN has read/write access; USER has read-only access.
- The caller's JWT is propagated from Employee Service to Department Service.
- Each service validates the token independently.
- Each service owns its own database.
- The design avoids cross-service foreign keys and direct database access.
- Swagger/OpenAPI makes both services easy to demonstrate.
