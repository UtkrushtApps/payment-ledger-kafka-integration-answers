package com.example.paymentledger.domain;

/**
 * External payment status as emitted by the reconciled payments topic.
 */
public enum PaymentStatus {
    RECONCILED,
    FAILED,
    CANCELLED
}
