package com.example.paymentledger.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Typed payment event as carried on the Kafka topic.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentEvent {

    /**
     * Stable, unique identifier of the logical payment event. Used for idempotency.
     */
    private String eventId;

    /**
     * Business payment identifier (for example, PSP payment id).
     */
    private String paymentId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;

    /**
     * Time at which the payment was reconciled in the upstream system.
     */
    private OffsetDateTime eventTime;

    public PaymentEvent() {
        // Default constructor for Jackson
    }

    public PaymentEvent(String eventId,
                        String paymentId,
                        BigDecimal amount,
                        String currency,
                        PaymentStatus status,
                        OffsetDateTime eventTime) {
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.eventTime = eventTime;
    }

    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("paymentId")
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @JsonProperty("amount")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonProperty("status")
    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    @JsonProperty("eventTime")
    public OffsetDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(OffsetDateTime eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentEvent that = (PaymentEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "eventId='" + eventId + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", eventTime=" + eventTime +
                '}';
    }
}
