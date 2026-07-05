package com.zakpruitt.jbay.finances;

import com.zakpruitt.jbay.Amount;

import java.time.ZonedDateTime;

/**
 * A ledger entry from the eBay Finances API ({@code /sell/finances/v1/transaction}).
 * Transaction types include SALE, REFUND, SHIPPING_LABEL, NON_SALE_CHARGE, and ADJUSTMENT;
 * these carry the money actually moved, as opposed to the listed prices on the order.
 */
public record Transaction(
        String transactionId,
        String orderId,
        String transactionType,
        String transactionStatus,
        String transactionDate,
        Amount amount,
        Amount totalFeeAmount) {

    public ZonedDateTime transactionDateTime() {
        return transactionDate == null ? null : ZonedDateTime.parse(transactionDate);
    }
}
