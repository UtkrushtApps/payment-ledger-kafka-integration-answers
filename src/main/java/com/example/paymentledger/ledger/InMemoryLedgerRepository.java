package com.example.paymentledger.ledger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, in-memory ledger implementation used for idempotent storage of payment events.
 */
@Repository
public class InMemoryLedgerRepository implements LedgerRepository {

    private static final Logger log = LoggerFactory.getLogger(InMemoryLedgerRepository.class);

    /**
     * Keyed by external event id to guarantee exactly-once semantics within the ledger.
     */
    private final Map<String, LedgerRecord> recordsByEventId = new ConcurrentHashMap<>();

    @Override
    public LedgerRecord saveIfAbsent(LedgerRecord record) {
        LedgerRecord existing = recordsByEventId.putIfAbsent(record.getEventId(), record);
        if (existing != null) {
            log.debug("Ledger record for eventId={} already exists with ledgerId={}",
                    record.getEventId(), existing.getLedgerId());
            return existing;
        }
        log.info("Stored new ledger record: ledgerId={}, eventId={} paymentId={}",
                record.getLedgerId(), record.getEventId(), record.getPaymentId());
        return record;
    }

    @Override
    public Optional<LedgerRecord> findByEventId(String eventId) {
        return Optional.ofNullable(recordsByEventId.get(eventId));
    }

    @Override
    public Collection<LedgerRecord> findAll() {
        return recordsByEventId.values();
    }
}
