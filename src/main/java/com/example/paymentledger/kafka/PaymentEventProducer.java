package com.example.paymentledger.kafka;

import com.example.paymentledger.domain.PaymentEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Objects;

/**
 * Kafka producer responsible for publishing reconciled payment events.
 */
@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final String topicName;

    public PaymentEventProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate,
                                @Value("${app.kafka.topics.reconciled-payments}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    /**
     * Publish the given event to Kafka using its eventId as the message key.
     */
    public void publish(PaymentEvent event) {
        Objects.requireNonNull(event, "payment event must not be null");
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            throw new IllegalArgumentException("eventId must be set on PaymentEvent before publishing");
        }

        String key = event.getEventId();
        log.info("Sending payment event to Kafka: topic={}, key={}, paymentId={}",
                topicName, key, event.getPaymentId());

        kafkaTemplate.send(topicName, key, event)
                .addCallback(new ListenableFutureCallback<SendResult<String, PaymentEvent>>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Failed to send payment event to Kafka: topic={} key={} paymentId={} ",
                                topicName, key, event.getPaymentId(), ex);
                    }

                    @Override
                    public void onSuccess(SendResult<String, PaymentEvent> result) {
                        RecordMetadata metadata = result.getRecordMetadata();
                        log.debug("Successfully sent payment event: topic={} partition={} offset={} key={} paymentId={} ",
                                metadata.topic(), metadata.partition(), metadata.offset(), key, event.getPaymentId());
                    }
                });
    }
}
