package com.newsplatform.dashboard.service;

import com.newsplatform.dashboard.dto.DashboardPeriodType;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class DashboardPeriodCalculator {
    private final Clock clock;
    private final ZoneId applicationZone;

    public DashboardPeriodCalculator(Clock clock, ZoneId applicationZone) {
        this.clock = clock;
        this.applicationZone = applicationZone;
    }

    public PeriodWindow calculate(DashboardPeriodType type) {
        Instant now = clock.instant();
        LocalDate today = now.atZone(applicationZone).toLocalDate();
        int daysBack = switch (type) {
            case TODAY -> 0;
            case LAST_7_DAYS -> 6;
            case LAST_30_DAYS -> 29;
        };
        Instant from = today.minusDays(daysBack).atStartOfDay(applicationZone).toInstant();
        return new PeriodWindow(type, from, now, applicationZone.getId());
    }

    public record PeriodWindow(DashboardPeriodType type, Instant from, Instant to, String timezone) { }
}
