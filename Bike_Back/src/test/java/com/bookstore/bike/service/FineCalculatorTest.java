package com.bookstore.bike.service;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FineCalculatorTest {
    @Test void usesProportionalPaiseAtTwoRupeesFiftyPerMinute() {
        assertEquals(0, FineCalculator.finePaise(0, 250));
        assertEquals(7500, FineCalculator.finePaise(30 * 60, 250));
        assertEquals(15000, FineCalculator.finePaise(60 * 60, 250));
        assertEquals(22500, FineCalculator.finePaise(90 * 60, 250));
        assertEquals(30000, FineCalculator.finePaise(120 * 60, 250));
        assertEquals(125, FineCalculator.finePaise(30, 250));
    }

    @Test void earlyAndOnTimeReturnsHaveNoLateTime() {
        Instant due = Instant.parse("2026-01-01T01:00:00Z");
        assertEquals(0, FineCalculator.lateSeconds(due, due.minusSeconds(1)));
        assertEquals(0, FineCalculator.lateSeconds(due, due));
        assertEquals(1, FineCalculator.lateSeconds(due, due.plusSeconds(1)));
    }
}
