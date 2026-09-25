package com.newsplatform.story.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.story.dto.AdminBreakingNewsListResponse;
import com.newsplatform.story.dto.AdminStoryResponse;
import com.newsplatform.story.dto.BreakingNewsRequest;
import com.newsplatform.story.dto.PublicBreakingNewsListResponse;
import com.newsplatform.story.service.BreakingNewsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Breaking News")
public class BreakingNewsController {
    private final BreakingNewsService service;

    public BreakingNewsController(BreakingNewsService service) { this.service = service; }

    @GetMapping("/breaking-news")
    public ResponseEntity<PublicBreakingNewsListResponse> listPublic(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.listPublic(limit));
    }

    @GetMapping("/admin/breaking-news")
    @PreAuthorize("hasAuthority('BREAKING_NEWS_MANAGE')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminBreakingNewsListResponse listAdmin(@RequestParam(defaultValue = "ACTIVE") String status,
                                                    @RequestParam(defaultValue = "100") int limit) {
        return service.listAdmin(status, limit);
    }

    @PostMapping("/admin/stories/{storyId}/breaking")
    @PreAuthorize("hasAuthority('BREAKING_NEWS_MANAGE')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminStoryResponse enable(@PathVariable UUID storyId,
                                     @RequestBody(required = false) BreakingNewsRequest request,
                                     @AuthenticationPrincipal AuthenticatedUser actor) {
        return service.enable(storyId, request, actor.id());
    }

    @DeleteMapping("/admin/stories/{storyId}/breaking")
    @PreAuthorize("hasAuthority('BREAKING_NEWS_MANAGE')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminStoryResponse disable(@PathVariable UUID storyId, @AuthenticationPrincipal AuthenticatedUser actor) {
        return service.disable(storyId, actor.id());
    }
}
