# ShopZone — AI workflow rules

## Scoping

- Implement one vertical slice that stays demoable. Do not open a second service's domain until the current one compiles.
- Do not add Kafka, Eureka, Kubernetes, or a frontend unless the user asks.
- Local Postgres is Docker Compose (`docker-compose.yml` at the repo root). Do not add extra Compose services unless needed to keep the current slice demoable.
- Do not introduce a shared domain library. New types belong in the owning service.

## Delivery

- After a meaningful change, update `context/progress-tracker.md`.
- If a change alters architecture, standards, or scope, update the relevant `context/*.md` file first.
- Prefer a small number of complete files over stubs that “will be filled in later.”
- Document env vars in the README, not in scattered comments.

## Testing

- Each module has a Spring context-load test.
- Persistence tests use PostgreSQL, not H2. user-service IAM tests target Compose Postgres on host port 5433 (`application-test.yml`). Testcontainers remains a later option; Docker Engine 29 on this machine fails Testcontainers over named pipe `docker_cli`.
- Do not add E2E tests that sleep arbitrarily.
