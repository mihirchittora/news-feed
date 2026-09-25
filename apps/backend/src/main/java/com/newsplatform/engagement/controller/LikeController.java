package com.newsplatform.engagement.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.engagement.dto.StoryEngagementResponse;
import com.newsplatform.engagement.service.LikeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stories/{storyId}/like")
@Tag(name = "Likes")
@SecurityRequirement(name = "bearerAuth")
public class LikeController {
    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public StoryEngagementResponse like(@PathVariable UUID storyId, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return likeService.like(storyId, currentUser.id());
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public StoryEngagementResponse unlike(@PathVariable UUID storyId, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return likeService.unlike(storyId, currentUser.id());
    }
}
