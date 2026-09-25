package com.newsplatform.engagement.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.engagement.dto.CommentPageResponse;
import com.newsplatform.engagement.dto.CommentResponse;
import com.newsplatform.engagement.dto.CreateCommentRequest;
import com.newsplatform.engagement.dto.UpdateCommentRequest;
import com.newsplatform.engagement.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/stories/{storyId}/comments")
    public CommentPageResponse list(
            @PathVariable UUID storyId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return commentService.listPublic(storyId, currentUser == null ? null : currentUser.id(), limit, cursor);
    }

    @PostMapping("/stories/{storyId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public CommentResponse create(@PathVariable UUID storyId, @Valid @RequestBody CreateCommentRequest request,
                                  @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return commentService.create(storyId, currentUser.id(), request);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public void deleteOwn(@PathVariable UUID commentId, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        commentService.deleteOwn(commentId, currentUser.id());
    }

    @PutMapping("/comments/{commentId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public CommentResponse updateOwn(@PathVariable UUID commentId, @Valid @RequestBody UpdateCommentRequest request,
                                     @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return commentService.updateOwn(commentId, currentUser.id(), request);
    }
}
