package com.zakpruitt.jbay.internal;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagesTest {

    private static final ZonedDateTime NOW = ZonedDateTime.of(2026, 7, 1, 12, 0, 0, 0, ZoneOffset.UTC);

    @Test
    void sinceWithinNinetyDaysProducesSingleWindowEndingNow() {
        ZonedDateTime since = NOW.minusDays(30);

        List<String> filters = Pages.timeWindowFilters("creationdate", since, NOW);

        assertEquals(List.of("creationdate:[2026-06-01T12:00:00.000Z..2026-07-01T12:00:00.000Z]"), filters);
    }

    @Test
    void longRangeSplitsIntoConsecutiveNinetyDayWindows() {
        ZonedDateTime since = NOW.minusDays(200);

        List<String> filters = Pages.timeWindowFilters("transactionDate", since, NOW);

        assertEquals(List.of(
                "transactionDate:[2025-12-13T12:00:00.000Z..2026-03-13T12:00:00.000Z]",
                "transactionDate:[2026-03-13T12:00:00.000Z..2026-06-11T12:00:00.000Z]",
                "transactionDate:[2026-06-11T12:00:00.000Z..2026-07-01T12:00:00.000Z]"), filters);
    }

    @Test
    void exactlyNinetyDaysProducesSingleWindow() {
        ZonedDateTime since = NOW.minusDays(90);

        List<String> filters = Pages.timeWindowFilters("creationdate", since, NOW);

        assertEquals(1, filters.size());
        assertEquals("creationdate:[2026-04-02T12:00:00.000Z..2026-07-01T12:00:00.000Z]", filters.get(0));
    }

    @Test
    void sinceInTheFutureProducesNoWindows() {
        List<String> filters = Pages.timeWindowFilters("creationdate", NOW.plusDays(1), NOW);

        assertTrue(filters.isEmpty());
    }

    @Test
    void sinceEqualToNowProducesNoWindows() {
        List<String> filters = Pages.timeWindowFilters("creationdate", NOW, NOW);

        assertTrue(filters.isEmpty());
    }

    @Test
    void nonUtcSinceIsConvertedToUtc() {
        ZonedDateTime since = ZonedDateTime.of(2026, 6, 1, 7, 0, 0, 0, ZoneId.of("America/New_York"));

        List<String> filters = Pages.timeWindowFilters("creationdate", since, NOW);

        assertEquals(List.of("creationdate:[2026-06-01T11:00:00.000Z..2026-07-01T12:00:00.000Z]"), filters);
    }
}
