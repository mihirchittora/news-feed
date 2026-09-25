package com.newsplatform.engagement.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.engagement.dto.AdminCommentPageResponse;
import com.newsplatform.engagement.dto.AdminCommentResponse;
import com.newsplatform.engagement.dto.ModerationRequest;
import com.newsplatform.engagement.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/admin/comments")
@Tag(name = "Comment moderation")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('COMMENT_MODERATE') or hasAuthority('SUPER_ADMIN')")
public class ModerationController {
    private final CommentService commentService;

    public ModerationController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public AdminCommentPageResponse list(@RequestParam(required = false) String status,
                                         @RequestParam(required = false) UUID storyId,
                                         @RequestParam(required = false) String query,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int limit) {
        return commentService.listAdmin(status, storyId, query, page, limit);
    }

    @GetMapping("/{id}")
    public AdminCommentResponse get(@PathVariable UUID id) {
        return commentService.getAdmin(id);
    }

    @PostMapping("/{id}/hide")
    public AdminCommentResponse hide(@PathVariable UUID id, @Valid @RequestBody(required = false) ModerationRequest request,
                                     @AuthenticationPrincipal AuthenticatedUser moderator) {
        return commentService.hide(id, moderator.id(), request);
    }

    @PostMapping("/{id}/restore")
    public AdminCommentResponse restore(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser moderator) {
        return commentService.restore(id, moderator.id());
    }

    @DeleteMapping("/{id}")
    public AdminCommentResponse delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser moderator) {
        return commentService.deleteByModerator(id, moderator.id());
    }
}
