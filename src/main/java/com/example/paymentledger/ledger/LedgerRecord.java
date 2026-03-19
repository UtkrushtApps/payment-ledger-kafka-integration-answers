package com.example.paymentledger.ledger;

import com.example.paymentledger.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Internal representation of a payment entry in the ledger.
 */
public class LedgerRecord {

    private final String ledgerId;
    private final String eventId;
    private final String paymentId;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentStatus status;
    private final OffsetDateTime bookedAt;

    public LedgerRecord(String ledgerId,
                        String eventId,
                        String paymentId,
                        BigDecimal amount,
                        String currency,
                        PaymentStatus status,
                        OffsetDateTime bookedAt) {
        this.ledgerId = ledgerId;
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.bookedAt = bookedAt;
    }

    public String getLedgerId() {
        return ledgerId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public OffsetDateTime getBookedAt() {
        return bookedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LedgerRecord that = (LedgerRecord) o;
        return Objects.equals(ledgerId, that.ledgerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ledgerId);
    }

    @Override
    public String toString() {
        return "LedgerRecord{" +
                "ledgerId='" + ledgerId + '\'' +
                ", eventId='" + eventId + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", bookedAt=" + bookedAt +
                '}';
    }
}
