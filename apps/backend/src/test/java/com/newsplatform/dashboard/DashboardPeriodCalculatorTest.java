package com.newsplatform.dashboard;

import com.newsplatform.dashboard.dto.DashboardPeriodType;
import com.newsplatform.dashboard.service.DashboardPeriodCalculator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardPeriodCalculatorTest {
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final Instant NOW = Instant.parse("2026-09-25T12:30:00Z");

    private final DashboardPeriodCalculator calculator = new DashboardPeriodCalculator(
            Clock.fixed(NOW, ZoneId.of("UTC")), ZONE
    );

    @Test
    void todayUsesApplicationCalendarBoundaryAndCurrentInstant() {
        var period = calculator.calculate(DashboardPeriodType.TODAY);

        assertThat(period.from()).isEqualTo(Instant.parse("2026-09-24T18:30:00Z"));
        assertThat(period.to()).isEqualTo(NOW);
        assertThat(period.timezone()).isEqualTo("Asia/Kolkata");
    }

    @Test
    void sevenDaysUsesTodayAndPreviousSixCalendarDays() {
        var period = calculator.calculate(DashboardPeriodType.LAST_7_DAYS);

        assertThat(period.from()).isEqualTo(Instant.parse("2026-09-18T18:30:00Z"));
        assertThat(period.to()).isEqualTo(NOW);
    }

    @Test
    void thirtyDaysUsesTodayAndPreviousTwentyNineCalendarDays() {
        var period = calculator.calculate(DashboardPeriodType.LAST_30_DAYS);

        assertThat(period.from()).isEqualTo(Instant.parse("2026-08-26T18:30:00Z"));
        assertThat(period.to()).isEqualTo(NOW);
    }
}
