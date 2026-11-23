# Payment Service (Skillora e-learning platform)

Spring Boot 3 / Java 17 microservice that owns **payments, invoices and refunds**.
Stack follows the project presentation: PostgreSQL (`payment_db`), Keycloak JWT, RabbitMQ, OpenFeign, Eureka, Docker.

## Run it

```bash
docker compose up -d payment-db rabbitmq            # PostgreSQL + RabbitMQ
mvn spring-boot:run -Dspring-boot.run.profiles=dev  # service on http://localhost:8084
mvn test                                            # unit tests
```

Swagger UI: http://localhost:8084/swagger-ui.html (click *Authorize* and paste a Keycloak access token).

| Env variable | Default | Purpose |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | local `payment_db` | PostgreSQL |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8080/realms/elearning` | must equal the `iss` claim of your tokens |
| `COURSE_SERVICE_URL` | *(empty → Eureka `course-service`)* | direct Course service URL for local dev |
| `EUREKA_ENABLED` / `EUREKA_URL` | `false` | service discovery |
| `RABBITMQ_HOST` ... | `localhost` / `guest` | broker |
| `PAYMENT_WEBHOOK_SECRET` | `change-me` | HMAC secret shared with the provider. **Change it.** |
| `PAYMENT_CURRENCY` | `EUR` | used when the Course service sends no currency |

## API (slide 34 contract) - all under `/api`

| Method | Endpoint | Role | Codes |
|---|---|---|---|
| POST | `/payments` | STUDENT | 201, 400, 404, 409, 502 (+401/403/503) |
| GET | `/payments/{id}/status` | owner / ADMIN | 200, 403, 404 |
| GET | `/payments/{id}` | owner / ADMIN (used by Complaint service) | 200, 403, 404 |
| GET | `/payments/me` | any user | 200, 401 |
| GET | `/payments?status=&studentId=` | ADMIN (payment follow-up) | 200, 403 |
| POST | `/payments/webhook` | public, HMAC-signed | 200, 400 |
| POST | `/payments/{id}/refund` | ADMIN | 200, 403, 404, 409, 502 |
| GET | `/invoices/{id}/download` | owner / ADMIN | 200, 403, 404 (PDF) |

Errors are `application/problem+json`. Lists are paginated: `?page=0&size=20&sort=createdAt,desc`.

### Payment flow

1. `POST /payments` `{ "courseId": 5, "paymentMethod": "CARD" }`
   - the service calls **Course** `GET /api/courses/{id}` (Feign, forwards the user's JWT): must exist, be `PUBLISHED`, have a `price`;
   - **the price comes from the Course service, never from the client**;
   - a student cannot have two live (PENDING / SUCCESS / PARTIALLY_REFUNDED) payments for one course (409, also enforced by a partial unique index);
   - the payment is created `PENDING`, opened at the provider, and the response contains `transactionReference` + `checkoutUrl`.
2. The student pays on the provider's page. No card data ever touches the platform.
3. The provider calls `POST /payments/webhook` with header `X-Payment-Signature: hex(HMAC_SHA256(secret, rawBody))` and body
   `{"transactionReference":"...","status":"SUCCESS"|"FAILED","reason":"..."}`.
   On SUCCESS: status → `SUCCESS`, **invoice created**, `PaymentSuccessful` event stored. Replays are ignored (idempotent).
4. `PENDING` payments older than 30 min are set to `FAILED` (`PaymentFailed`) so the student can retry.

### Try it without a real provider (profile `dev`)

```bash
# 1. as a student (token from Keycloak)
curl -X POST localhost:8084/api/payments -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" -d '{"courseId":1,"paymentMethod":"CARD"}'
# -> note "transactionReference": "SIM-..."

# 2a. dev shortcut that plays the provider
curl -X POST "localhost:8084/api/dev/payments/SIM-xxxx/simulate?outcome=SUCCESS" -H "Authorization: Bearer $TOKEN"

# 2b. or the real signed webhook
BODY='{"transactionReference":"SIM-xxxx","status":"SUCCESS"}'
SIG=$(printf '%s' "$BODY" | openssl dgst -sha256 -hmac "change-me" | awk '{print $2}')
curl -X POST localhost:8084/api/payments/webhook -H "Content-Type: application/json" -H "X-Payment-Signature: $SIG" -d "$BODY"

# 3. status, invoice, refund (admin token)
curl localhost:8084/api/payments/1/status -H "Authorization: Bearer $TOKEN"
curl -OJ localhost:8084/api/invoices/1/download -H "Authorization: Bearer $TOKEN"
curl -X POST localhost:8084/api/payments/1/refund -H "Authorization: Bearer $ADMIN_TOKEN" \
     -H "Content-Type: application/json" -d '{"amount":50.00,"reason":"Partial refund"}'   # omit amount = full refund
```

## Events (RabbitMQ)

Topic exchange **`payment.events`** (durable). Each consumer declares its own queue and binds it:

| Event | Routing key | Consumers (slide 21) |
|---|---|---|
| `PaymentSuccessful` | `payment.successful` | Course (grant access), Classroom (activate enrollment) |
| `PaymentFailed` | `payment.failed` | - |
| `PaymentRefunded` | `payment.refunded` | Course, Classroom (revoke access when `fullyRefunded` is true) |

```json
{
  "eventId": "6f1c...", "eventType": "PaymentSuccessful", "occurredAt": "2026-04-12T10:30:00Z",
  "data": { "paymentId": 1, "courseId": 5, "enrollmentId": null, "studentId": "<keycloak sub>",
            "amount": 150.00, "currency": "EUR" }
}
```

Events are written to an **outbox table in the same DB transaction** as the payment change and relayed by `OutboxPublisher`:
no event is lost if RabbitMQ is down; delivery is at-least-once, so consumers should ignore an `eventId` they have already seen.

Consumer example (Classroom / Course):
```java
@RabbitListener(bindings = @QueueBinding(
    value = @Queue(name = "classroom.payment-successful", durable = "true"),
    exchange = @Exchange(name = "payment.events", type = "topic", durable = "true"),
    key = "payment.successful"))
void onPaymentSuccessful(String json) { /* parse, check eventId, activate enrollment */ }
```

## Data model (Payment_DB, Flyway `V1__init_payment_schema.sql`)

`Payment 1 — 0..1 Invoice`, `Payment 1 — * Refund`, plus `outbox_events`. Other services are referenced by id only
(`student_id` = Keycloak `sub`, `course_id`, `enrollment_id`), never joined across databases.
