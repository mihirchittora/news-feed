package com.newsplatform.dashboard;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.dashboard.service.DashboardPeriodCalculator;
import com.newsplatform.dashboard.service.DashboardService;
import com.newsplatform.dashboard.repository.DashboardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class DashboardServiceAuthorizationTest {
    @Test
    void normalUserCannotReadDashboard() {
        var service = new DashboardService(
                mock(DashboardRepository.class),
                new DashboardPeriodCalculator(Clock.systemUTC(), ZoneId.of("UTC")),
                24
        );
        var authentication = new UsernamePasswordAuthenticationToken(
                "user", "n/a", List.of(new SimpleGrantedAuthority("USER"))
        );

        assertThatThrownBy(() -> service.getDashboard(null, authentication))
                .isInstanceOf(RbacException.class)
                .hasMessageContaining("administrative dashboard");
    }
}
