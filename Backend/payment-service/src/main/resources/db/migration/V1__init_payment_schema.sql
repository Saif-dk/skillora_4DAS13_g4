-- Payment_DB schema (owned exclusively by the Payment microservice)

CREATE SEQUENCE invoice_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE payments (
    id                    BIGSERIAL PRIMARY KEY,
    student_id            VARCHAR(64)    NOT NULL,           -- Keycloak "sub" of the student (User service)
    student_name          VARCHAR(255),                      -- snapshot used on the invoice
    course_id             BIGINT         NOT NULL,           -- reference to Course service (no FK across services)
    course_title          VARCHAR(255),                      -- snapshot taken at payment time
    enrollment_id         BIGINT,                            -- optional reference to Classroom service
    amount                NUMERIC(12, 2) NOT NULL,
    currency              VARCHAR(3)     NOT NULL,
    payment_method        VARCHAR(30)    NOT NULL,
    status                VARCHAR(30)    NOT NULL,
    transaction_reference VARCHAR(100)   UNIQUE,             -- reference given by the payment provider
    failure_reason        VARCHAR(500),
    refunded_amount       NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ    NOT NULL,
    updated_at            TIMESTAMPTZ    NOT NULL,
    payment_date          TIMESTAMPTZ
);

-- A student cannot have two "live" payments for the same course (protects against double clicks / races).
CREATE UNIQUE INDEX uq_payments_live_student_course
    ON payments (student_id, course_id)
    WHERE status IN ('PENDING', 'SUCCESS', 'PARTIALLY_REFUNDED');

CREATE INDEX idx_payments_student_created ON payments (student_id, created_at DESC);
CREATE INDEX idx_payments_status ON payments (status);

CREATE TABLE invoices (
    id             BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(30)    NOT NULL UNIQUE,
    payment_id     BIGINT         NOT NULL UNIQUE REFERENCES payments (id),
    student_id     VARCHAR(64)    NOT NULL,
    description    VARCHAR(255),
    amount         NUMERIC(12, 2) NOT NULL,
    currency       VARCHAR(3)     NOT NULL,
    status         VARCHAR(30)    NOT NULL,
    issue_date     TIMESTAMPTZ    NOT NULL
);

CREATE TABLE refunds (
    id            BIGSERIAL PRIMARY KEY,
    payment_id    BIGINT         NOT NULL REFERENCES payments (id),
    amount        NUMERIC(12, 2) NOT NULL,
    reason        VARCHAR(500)   NOT NULL,
    requested_by  VARCHAR(64)    NOT NULL,                   -- admin who triggered the refund
    refund_date   TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_refunds_payment ON refunds (payment_id);

-- Transactional outbox: events are written in the same DB transaction as the business change,
-- then relayed to RabbitMQ by OutboxPublisher (no lost events if the broker is down).
CREATE TABLE outbox_events (
    id            UUID PRIMARY KEY,
    event_type    VARCHAR(50)  NOT NULL,
    routing_key   VARCHAR(100) NOT NULL,
    payload       TEXT         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    published_at  TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (created_at) WHERE published_at IS NULL;
