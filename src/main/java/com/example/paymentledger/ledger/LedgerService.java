package com.example.paymentledger.ledger;

import com.example.paymentledger.domain.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service responsible for mapping payment events into internal ledger records
 * and enforcing idempotent writes.
 */
@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private final LedgerRepository ledgerRepository;

    public LedgerService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    /**
     * Transform a payment event into a ledger record and persist it.
     * Duplicate events (same event id) will return the already stored record.
     *
     * @param event reconciled payment event consumed from Kafka
     * @return stored ledger record (new or existing)
     */
    public LedgerRecord recordPaymentEvent(PaymentEvent event) {
        Objects.requireNonNull(event, "payment event must not be null");
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            throw new IllegalArgumentException("payment eventId must be provided for idempotency");
        }

        LedgerRecord record = mapToLedgerRecord(event);
        LedgerRecord stored = ledgerRepository.saveIfAbsent(record);

        if (stored == record) {
            log.debug("Ledger entry created for eventId={} with ledgerId={}",
                    stored.getEventId(), stored.getLedgerId());
        } else {
            log.info("Duplicate payment event detected, re-using existing ledgerId={} for eventId={}",
                    stored.getLedgerId(), stored.getEventId());
        }

        return stored;
    }

    private LedgerRecord mapToLedgerRecord(PaymentEvent event) {
        String ledgerId = UUID.randomUUID().toString();
        OffsetDateTime bookedAt = event.getEventTime() != null ? event.getEventTime() : OffsetDateTime.now();

        return new LedgerRecord(
                ledgerId,
                event.getEventId(),
                event.getPaymentId(),
                event.getAmount(),
                event.getCurrency(),
                event.getStatus(),
                bookedAt
        );
    }
}
