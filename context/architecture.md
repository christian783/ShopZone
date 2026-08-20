# ShopZone — Architecture

## Stack

- Java 17 (Spring Boot 4.1 supports 17–26)
- Spring Boot 4.1.0, Spring Cloud 2025.1.2 (Oakwood)
- Maven multi-module monorepo
- Spring Cloud Gateway as the only client-facing application port: 8080
- PostgreSQL, one logical database per domain service (`*-db`)

## Services

| Service | Port | Database | Owns |
|---|---|---|---|
| api-gateway | 8080 | — | Routing, CORS |
| user-service | 8081 | user-db | Accounts, JWT auth, RBAC, audit |
| product-service | 8082 | product-db | Catalog |
| cart-service | 8083 | cart-db | Shopping carts |
| inventory-service | 8084 | inventory-db | Stock levels |
| order-service | 8085 | order-db | Orders |
| payment-service | 8086 | payment-db | Payments |
| notification-service | 8087 | notification-db | Outbound notifications |

The root `pom.xml` is `packaging=pom`. It is not a Spring Boot application. It lists modules, pins versions, and imports `spring-cloud-dependencies`.

## Identity (user-service)

- Access JWT: **HS256** with `JWT_SECRET` (min 32 bytes), 15 minute TTL. Claims: `sub` (userId), `email`, `authorities` (privilege names).
- Opaque refresh tokens: SHA-256 hashed in `user-db`, 7 day TTL, rotated on use; reuse of a revoked family member revokes the whole family.
- Passwords: BCrypt strength 12.
- RBAC: users many-to-many roles, roles many-to-many privileges (privileges are a seeded catalog).
- Forgot password: hashed reset token in `user-db`; reset URL is logged (no email in this phase).
- Soft delete on users. Mutations write an `audit_log` row in the same transaction.
- Gateway forwards `Authorization`; it is not a JWT resource server yet.

## Gateway routes

| Path | Upstream |
|---|---|
| `/api/auth/**` | user-service |
| `/api/users/**` | user-service |
| `/api/roles/**` | user-service |
| `/api/privileges/**` | user-service |
| `/api/audit/**` | user-service |
| `/api/products/**` | product-service |
| `/api/carts/**` | cart-service |
| `/api/inventory/**` | inventory-service |
| `/api/orders/**` | order-service |
| `/api/payments/**` | payment-service |
| `/api/notifications/**` | notification-service |

Local URIs default to `http://localhost:<port>` and can be overridden with `USER_URI`, `PRODUCT_URI`, and the matching env vars.

## Invariants

- Each service is independently runnable and produces its own executable jar.
- Cross-service calls go through HTTP (via the gateway or explicit URIs). No shared JPA entities across modules.
- No Eureka in this phase; local discovery is host + port (Compose DNS later if needed).
- Domain services each own one PostgreSQL database named `<service>-db` (for example `user-service` → `user-db`). The gateway has no database.
- JDBC URL host/port default to `localhost:5432`; override with `DB_HOST` / `DB_PORT`. Credentials default to `shopzone` / `shopzone` (`DB_USER` / `DB_PASSWORD`).
- `@Transactional` never spans an HTTP call to another service.

## Layout

```
ShopZone/
  pom.xml
  docker-compose.yml
  user-service/
  product-service/
  cart-service/
  inventory-service/
  order-service/
  payment-service/
  notification-service/
  infra/api-gateway/
```
