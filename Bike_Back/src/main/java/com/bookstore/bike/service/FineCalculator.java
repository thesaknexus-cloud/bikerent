package com.bookstore.bike.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

public final class FineCalculator {
    private FineCalculator() {}

    public static long lateSeconds(Instant dueAt, Instant returnedAt) {
        return Math.max(0, Duration.between(dueAt, returnedAt).getSeconds());
    }

    /** Proportional paise calculation, rounded to the nearest paise (half up). */
    public static long finePaise(long lateSeconds, long ratePaisePerMinute) {
        if (lateSeconds <= 0 || ratePaisePerMinute <= 0) return 0;
        return BigDecimal.valueOf(lateSeconds)
                .multiply(BigDecimal.valueOf(ratePaisePerMinute))
                .divide(BigDecimal.valueOf(60), 0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
