# ShopZone — UI context

This repository is backend-first. There is no customer-facing SPA in the current phase.

## Surfaces

- **API gateway** at `http://localhost:8080` — all REST traffic.
- **User-service Swagger** at `http://localhost:8081/swagger-ui.html` (also via gateway once routed).
- **Actuator health** on each service at `/actuator/health` (gateway on 8080; domain services on 8081–8087).

## API conventions (affect any future UI)

- Envelope: `{ success, message, data, timestamp }`. `message` is already localized.
- Validation errors: HTTP 400, `success: false`, `data.errors: [{ field, message }]`.
- Pagination: `page` (0-based), `size` (default 20, max 100), `sort`. List payloads wrap `PageResponse` inside `data`.
- Auth: `Authorization: Bearer <accessJwt>`. Refresh with `POST /api/auth/refresh`.
- Locale: `Accept-Language: en` (default) or `fr`.

## Visual / brand notes (if a UI is added later)

- Ecommerce storefront: product grid, cart drawer, checkout. High contrast, readable prices, clear stock status.
- Status colors: in-stock = success, low-stock = warning, out-of-stock / failed payment = danger.
