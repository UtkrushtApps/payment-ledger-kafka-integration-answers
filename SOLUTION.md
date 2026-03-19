# Solution Steps

1. Create the main Spring Boot application class `PaymentLedgerKafkaApplication` so the service can start as a normal Spring Boot app.

2. Define the external payment domain model: create `PaymentStatus` enum to represent reconciled/failed/cancelled states, and create `PaymentEvent` with fields like `eventId`, `paymentId`, `amount`, `currency`, `status`, and `eventTime`, annotating it for Jackson JSON serialization/deserialization.

3. Implement the internal ledger model: add `LedgerRecord` class representing a stored ledger entry, containing a generated `ledgerId`, the originating `eventId`, the business `paymentId`, amount, currency, status, and `bookedAt` timestamp.

4. Define a repository abstraction for the ledger: create `LedgerRepository` with methods `saveIfAbsent`, `findByEventId`, and `findAll` to encapsulate how ledger records are stored and retrieved.

5. Provide an in-memory, thread-safe implementation of the repository: implement `InMemoryLedgerRepository` using a `ConcurrentHashMap` keyed by `eventId`, implement `saveIfAbsent` via `putIfAbsent` to guarantee idempotency, and add logging to trace new vs. duplicate records.

6. Create a `LedgerService` that orchestrates mapping a `PaymentEvent` into a `LedgerRecord` and persists it via `LedgerRepository`. Validate that `eventId` is present, generate a new `ledgerId`, choose a `bookedAt` time (using `eventTime` or `now()`), then call `saveIfAbsent` and log whether the record is new or a duplicate.

7. Configure Kafka producer and consumer beans in `KafkaConfig`: enable Kafka with `@EnableKafka`, inject `spring.kafka.bootstrap-servers`, build a `ProducerFactory<String, PaymentEvent>` and `KafkaTemplate` using `JsonSerializer`, and a `ConsumerFactory<String, PaymentEvent>` and `ConcurrentKafkaListenerContainerFactory` using `JsonDeserializer<PaymentEvent>` with trusted packages set.

8. In `KafkaConfig`, define a `DefaultErrorHandler` bean with a small fixed backoff that logs unrecoverable errors (including topic/partition/offset/key) and skips bad records while keeping the listener container running, then plug it into the listener container factory via `setCommonErrorHandler`.

9. Implement the Kafka producer: create `PaymentEventProducer` that depends on `KafkaTemplate<String, PaymentEvent>` and the reconciled-payments topic name from configuration, validate that `eventId` is set, then call `kafkaTemplate.send(topic, eventId, event)` and register a callback to log success metadata or failures.

10. Implement the Kafka consumer: create `PaymentEventConsumer` with a `@KafkaListener` bound to the reconciled-payments topic, using the custom `paymentEventKafkaListenerContainerFactory` and a configured group id. In the listener method, log record metadata, guard against null payloads, call `ledgerService.recordPaymentEvent(event)` to persist it exactly once, and manually acknowledge the record only after successful processing; on exceptions, log the error and rethrow so the shared error handler can decide retries/skips.

11. Expose a small REST API for manual testing: create `PaymentEventPublishRequest` DTO (paymentId, amount, currency, status) and `PaymentController` with a POST `/api/payments/reconciled` endpoint that validates the request, builds a `PaymentEvent` with a new UUID eventId and current timestamp, logs the publication attempt, and delegates to `PaymentEventProducer.publish` before returning HTTP 202.

12. Add `application.yml` configuration: set `spring.kafka.bootstrap-servers`, a consumer `group-id`, listener `ack-mode` to `manual`, and define `app.kafka.topics.reconciled-payments`. Configure logging levels so the application and Kafka integration log informative messages for observability.

