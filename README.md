# ShopZone

Ecommerce microservices monorepo. The root Maven parent manages versions; each module is an independently runnable Spring Boot service.

## Stack

- Java 17
- Spring Boot 4.1.0, Spring Cloud 2025.1.2
- Maven Wrapper (`mvnw.cmd`)

## Modules

```
Client → api-gateway :8080
           ├─ user-service          :8081
           ├─ product-service       :8082
           ├─ cart-service          :8083
           ├─ inventory-service     :8084
           ├─ order-service         :8085
           ├─ payment-service       :8086
           └─ notification-service  :8087
```

## Build

From the ShopZone root:

```powershell
docker compose up -d postgres
.\mvnw.cmd clean verify
```

user-service tests expect Compose Postgres on port 5433 (`shopzone` / `shopzone`).

Build a subset (and the parent):

```powershell
.\mvnw.cmd -pl user-service,product-service -am package
```

## Run one service

```powershell
.\mvnw.cmd -pl user-service spring-boot:run
```

Nested gateway module:

```powershell
.\mvnw.cmd -pl infra/api-gateway spring-boot:run
```

Override a port with `SERVER_PORT`. Override gateway upstreams with `USER_URI`, `PRODUCT_URI`, `CART_URI`, `INVENTORY_URI`, `ORDER_URI`, `PAYMENT_URI`, or `NOTIFICATION_URI`.

## Local Postgres

```powershell
docker compose up -d postgres
```

Creates `user-db` plus the other `*-db` names on host port **5433** (avoids a local Postgres on 5432). Credentials: `shopzone` / `shopzone`.

For `spring-boot:run`, point at Compose:

```powershell
$env:DB_PORT=5433
.\mvnw.cmd -pl user-service spring-boot:run
```

## User-service IAM

```powershell
.\mvnw.cmd -pl user-service spring-boot:run
```

- Swagger: http://localhost:8081/swagger-ui.html
- Bootstrap admin: `admin@shopzone.dev` / `Admin123!`
- Access JWT: HS256, 15 minutes (`JWT_SECRET`, min 32 bytes; local default is documented in `user-service` `application.yml`)
- Locale: `Accept-Language: en` or `fr`

Example login:

```powershell
curl -s http://localhost:8081/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@shopzone.dev\",\"password\":\"Admin123!\"}"
```

## Databases

Each domain service uses its own PostgreSQL database on `localhost:5432` (user/password `shopzone` / `shopzone`). Override with `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`.

| Service | Database |
|---|---|
| user-service | user-db |
| product-service | product-db |
| cart-service | cart-db |
| inventory-service | inventory-db |
| order-service | order-db |
| payment-service | payment-db |
| notification-service | notification-db |

## Health

| Service | URL |
|---|---|
| Gateway | http://localhost:8080/actuator/health |
| User | http://localhost:8081/actuator/health |
| Product | http://localhost:8082/actuator/health |
| Cart | http://localhost:8083/actuator/health |
| Inventory | http://localhost:8084/actuator/health |
| Order | http://localhost:8085/actuator/health |
| Payment | http://localhost:8086/actuator/health |
| Notification | http://localhost:8087/actuator/health |
