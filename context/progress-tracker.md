# ShopZone — Progress tracker

## Current phase

Product catalog vertical slice (next).

## Completed

- Root parent POM (`com.shopzone:shopzone`, Spring Boot 4.1.0, Spring Cloud 2025.1.2)
- Maven Wrapper and `.gitignore`
- Domain services: user, product, cart, inventory, order, payment, notification (ports 8081–8087)
- `infra/api-gateway` with Spring Cloud Gateway routes and CORS
- Context docs (`project-overview`, `architecture`, `ui-context`, `code-standards`, `ai-workflow-rules`)
- Root README with build and run commands
- Per-service PostgreSQL datasource in `application.yml` (`*-db`)
- Docker Compose Postgres 17 on host port 5433 (`user-db` plus other `*-db` names)
- user-service IAM: Flyway schema/seed, HS256 access JWT + hashed refresh rotation, RBAC, audit, forgot/reset password, `ApiResponse` + i18n (en/fr), OpenAPI
- Gateway routes for `/api/auth/**`, `/api/users/**`, `/api/roles/**`, `/api/privileges/**`, `/api/audit/**`
- `mvn -pl user-service -am verify` (context load + IAM API tests against Compose Postgres)

## In progress

- None

## Next

- Product catalog vertical slice
- Optional: email via notification-service for password reset
- Optional: other services as HS256 resource servers using `JWT_SECRET`
- Optional: Testcontainers for IAM tests once Docker Engine 29 named-pipe (`docker_cli`) is compatible

## Notes

- IAM tests use Compose Postgres on `127.0.0.1:5433`, not H2 and not Testcontainers. Testcontainers 1.21.3 against Docker Engine 29 failed on Windows named pipe `docker_cli` (`/info` HTTP 400).
