package com.newsplatform.dashboard.controller;

import com.newsplatform.dashboard.dto.DashboardPeriodType;
import com.newsplatform.dashboard.dto.DashboardResponse;
import com.newsplatform.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Admin Dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('STORY_VIEW_ADMIN', 'BREAKING_NEWS_MANAGE', 'COMMENT_MODERATE', 'NEWSPAPER_VIEW_ADMIN', 'AD_VIEW_ADMIN', 'CATEGORY_MANAGE', 'TAG_MANAGE', 'STAFF_VIEW', 'ROLE_VIEW', 'SUPER_ADMIN')")
    @Operation(summary = "Read the permission-filtered operational dashboard", security = @SecurityRequirement(name = "bearerAuth"))
    public DashboardResponse dashboard(
            @RequestParam(defaultValue = "TODAY") String period,
            Authentication authentication
    ) {
        DashboardPeriodType requested;
        try {
            requested = DashboardPeriodType.valueOf(period.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new com.newsplatform.common.error.RbacException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "INVALID_DASHBOARD_PERIOD",
                    "Period must be TODAY, LAST_7_DAYS, or LAST_30_DAYS"
            );
        }
        return dashboardService.getDashboard(requested, authentication);
    }
}
