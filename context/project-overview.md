# ShopZone — Product Overview

ShopZone is an ecommerce platform split into independently deployable Spring Boot microservices. A shopper browses products, adds them to a cart, places an order, pays, and receives notifications while inventory is reserved behind the scenes.

## Domain flow

User → Product catalog → Cart → Inventory reservation → Order → Payment → Notification

## Goals

- Demonstrate a Maven multi-module Spring Boot monorepo where the root parent manages versions and each module is a runnable microservice.
- Keep every service independently startable without a database in the current phase.
- Grow one bounded context at a time without coupling domain models across jars.

## In-scope (current phase)

- Parent POM aggregator with shared Java 17 / Spring Boot 4.1 / Spring Cloud versions
- Seven domain services plus an API gateway
- Actuator health endpoints and gateway path routing to local service ports

## Out of scope (current phase)

- PostgreSQL, Flyway, RabbitMQ, Redis
- Eureka, Config Server, Kubernetes
- Real payment providers, email/SMS vendors, and a customer-facing frontend
- Shared domain library (`shopzone-common`)

## Users and roles (planned)

| Role | Typical actions |
|---|---|
| CUSTOMER | Register, browse products, manage cart, place/track orders |
| ADMIN | Manage catalog, inventory, and users |
