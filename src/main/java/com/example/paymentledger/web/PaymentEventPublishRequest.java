package com.example.paymentledger.web;

import com.example.paymentledger.domain.PaymentStatus;

import java.math.BigDecimal;

/**
 * Request DTO used to trigger publication of a sample payment event.
 */
public class PaymentEventPublishRequest {

    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
