package com.newsplatform.engagement.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.engagement.dto.CommentEngagementResponse;
import com.newsplatform.engagement.service.CommentLikeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Comment likes")
public class CommentLikeController {
    private final CommentLikeService commentLikeService;

    public CommentLikeController(CommentLikeService commentLikeService) {
        this.commentLikeService = commentLikeService;
    }

    @PostMapping("/{commentId}/like")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public CommentEngagementResponse like(@PathVariable UUID commentId, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return commentLikeService.like(commentId, currentUser.id());
    }

    @DeleteMapping("/{commentId}/like")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public CommentEngagementResponse unlike(@PathVariable UUID commentId, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return commentLikeService.unlike(commentId, currentUser.id());
    }
}
