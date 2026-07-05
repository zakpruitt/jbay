package com.zakpruitt.jbay;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmountTest {

    @Test
    void parsesDecimalValue() {
        assertEquals(new BigDecimal("12.34"), new Amount("12.34", "USD").asBigDecimal());
        assertEquals(12.34, new Amount("12.34", "USD").asDouble());
    }

    @Test
    void parsesNegativeValue() {
        assertEquals(new BigDecimal("-3.50"), new Amount("-3.50", "USD").asBigDecimal());
    }

    @Test
    void nullValueIsZero() {
        assertEquals(BigDecimal.ZERO, new Amount(null, "USD").asBigDecimal());
    }

    @Test
    void blankValueIsZero() {
        assertEquals(BigDecimal.ZERO, new Amount("   ", "USD").asBigDecimal());
    }

    @Test
    void garbageValueIsZero() {
        assertEquals(BigDecimal.ZERO, new Amount("not-a-number", "USD").asBigDecimal());
    }

    @Test
    void orZeroHandlesNullAmount() {
        assertEquals(0.0, Amount.orZero(null));
        assertEquals(9.99, Amount.orZero(new Amount("9.99", "USD")));
    }
}
