package com.example.paymentledger.web;

import com.example.paymentledger.domain.PaymentEvent;
import com.example.paymentledger.domain.PaymentStatus;
import com.example.paymentledger.kafka.PaymentEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Simple API surface to publish payment events for manual testing.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentEventProducer producer;

    public PaymentController(PaymentEventProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/reconciled")
    public ResponseEntity<Void> publishReconciledPayment(@RequestBody PaymentEventPublishRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        if (request.getPaymentId() == null || request.getPaymentId().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        PaymentEvent event = new PaymentEvent(
                UUID.randomUUID().toString(),
                request.getPaymentId(),
                request.getAmount(),
                request.getCurrency(),
                request.getStatus() != null ? request.getStatus() : PaymentStatus.RECONCILED,
                OffsetDateTime.now()
        );

        log.info("HTTP request to publish reconciled payment event: paymentId={} amount={} {}", 
                event.getPaymentId(), event.getAmount(), event.getCurrency());

        producer.publish(event);
        return ResponseEntity.accepted().build();
    }
}
