package com.example.paymentledger.ledger;

import java.util.Collection;
import java.util.Optional;

/**
 * Repository abstraction for the ledger store.
 */
public interface LedgerRepository {

    /**
     * Persist the given ledger record if, and only if, there is no existing record for the same event id.
     *
     * @param record ledger record to persist
     * @return the stored record – either the newly stored one or the existing record for the event id
     */
    LedgerRecord saveIfAbsent(LedgerRecord record);

    /**
     * Find a record by its event id.
     */
    Optional<LedgerRecord> findByEventId(String eventId);

    /**
     * Return all ledger records currently stored in memory.
     */
    Collection<LedgerRecord> findAll();
}
