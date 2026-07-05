package com.zakpruitt.jbay.orders;

import com.zakpruitt.jbay.Amount;

/** Listed prices on the order. For money actually collected, see the Finances transactions. */
public record PricingSummary(
        Amount priceSubtotal,
        Amount deliveryCost,
        Amount tax,
        Amount total) {
}
