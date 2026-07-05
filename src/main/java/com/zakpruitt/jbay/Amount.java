package com.zakpruitt.jbay;

import java.math.BigDecimal;

/** A monetary amount as eBay returns it: a decimal string plus a currency code. */
public record Amount(String value, String currency) {

    public BigDecimal asBigDecimal() {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public double asDouble() {
        return asBigDecimal().doubleValue();
    }

    /** Null-safe convenience for optional amounts. */
    public static double orZero(Amount amount) {
        return amount == null ? 0 : amount.asDouble();
    }
}
