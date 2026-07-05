package com.zakpruitt.jbay.orders;

import com.zakpruitt.jbay.Amount;

/** A single listing purchased within an {@link Order}. */
public record LineItem(
        String lineItemId,
        String legacyItemId,
        String title,
        String sku,
        int quantity,
        Amount lineItemCost,
        Amount total) {
}
