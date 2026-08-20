# ShopZone — Code standards

## Language and style

- Java 17, Spring Boot 4.1. Package root: `com.shopzone.<service>`.
- DTOs are Java records. Entities never leave the service layer.
- Service interface + `*ServiceImpl`. `@Transactional` lives on the implementation, never on controllers.
- Controllers: validate → map → delegate → map. Zero business logic.
- Lombok `@Getter`, `@Setter`, and `@NoArgsConstructor` on JPA-mapped types. Do not use `@Data`.

## Persistence

- Flyway migrations in `src/main/resources/db/migration`. `ddl-auto=validate`.
- JPA `Specification<T>` + `Pageable` for list endpoints. No unbounded lists.
- Cross-service references are UUIDs only.
- User mutations and their `audit_log` row commit in the same transaction.

## HTTP

- Bean Validation on every request record; `@Valid` on controller parameters. Validation messages are i18n keys.
- Controllers return `ResponseEntity<ApiResponse<T>>`. Type name: `com.shopzone.<service>.web.ApiResponse` (not springdoc’s `@ApiResponse`).
- `ApiResponse` fields: `success`, `message` (resolved i18n string), `data`, `timestamp`.
- Validation errors: HTTP 400, `success: false`, `data.errors[]` with field + resolved message.
- Locale from `Accept-Language` (default `en`, also `fr`). Never leak SQL or stack traces.
- Pagination envelope: `PageResponse<T>` (`content`, `page`, `size`, `totalElements`, `totalPages`). Never serialize Spring `Page`.
- OpenAPI: `@Operation` summary + description, stable `operationId`, every reachable error status documented.
- Actuator: expose `health` and `info` only unless a later phase adds metrics.

## Security

- `@EnableMethodSecurity` + `@PreAuthorize` with privilege names (`USER_DELETE`, not `ROLE_ADMIN`).
- Passwords: `BCryptPasswordEncoder` strength 12. Never log hashes, refresh tokens, or reset tokens.
- Access JWT HS256 via `JWT_SECRET`. Refresh tokens stored SHA-256 hashed.

## Shared modules

No shared domain jar in this phase. New domain types belong in the owning service.

## Boot 4 starters

- MVC services: `spring-boot-starter-webmvc`, tests: `spring-boot-starter-webmvc-test`
- Gateway: `spring-cloud-starter-gateway-server-webflux`
