package com.example.paymentledger.kafka;

import com.example.paymentledger.domain.PaymentEvent;
import com.example.paymentledger.ledger.LedgerRecord;
import com.example.paymentledger.ledger.LedgerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that transforms reconciled payment events into internal ledger records.
 */
@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final LedgerService ledgerService;

    public PaymentEventConsumer(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.reconciled-payments}",
            containerFactory = "paymentEventKafkaListenerContainerFactory",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onPaymentEvent(@Payload PaymentEvent event,
                               ConsumerRecord<String, PaymentEvent> record,
                               Acknowledgment acknowledgment) {
        try {
            log.info("Received payment event from Kafka: topic={} partition={} offset={} key={} paymentId={}",
                    record.topic(), record.partition(), record.offset(), record.key(),
                    event != null ? event.getPaymentId() : null);

            if (event == null) {
                log.warn("Received null PaymentEvent payload, skipping");
                acknowledgment.acknowledge();
                return;
            }

            LedgerRecord ledgerRecord = ledgerService.recordPaymentEvent(event);
            log.debug("Payment event processed into ledger: ledgerId={} eventId={} paymentId={}",
                    ledgerRecord.getLedgerId(), ledgerRecord.getEventId(), ledgerRecord.getPaymentId());

            // Acknowledge only after successful processing to achieve at-least-once semantics
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            // Let the error handler decide on retries / skipping
            log.error("Error handling payment event from topic={} partition={} offset={} key={}",
                    record.topic(), record.partition(), record.offset(), record.key(), ex);
            throw ex;
        }
    }
}
