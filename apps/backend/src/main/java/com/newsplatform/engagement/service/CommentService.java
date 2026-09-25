package com.newsplatform.engagement.service;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.engagement.dto.AdminCommentPageResponse;
import com.newsplatform.engagement.dto.AdminCommentResponse;
import com.newsplatform.engagement.dto.CommentPageResponse;
import com.newsplatform.engagement.dto.CommentResponse;
import com.newsplatform.engagement.dto.CreateCommentRequest;
import com.newsplatform.engagement.dto.ModerationRequest;
import com.newsplatform.engagement.dto.UpdateCommentRequest;
import com.newsplatform.engagement.entity.Comment;
import com.newsplatform.engagement.entity.CommentStatus;
import com.newsplatform.engagement.repository.CommentRepository;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.story.entity.Story;
import com.newsplatform.story.entity.StoryStatus;
import com.newsplatform.story.repository.StoryRepository;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final CommentEngagementService commentEngagementService;
    private final int maxBodyLength;

    public CommentService(CommentRepository commentRepository, StoryRepository storyRepository, UserRepository userRepository,
                          AuditService auditService, int maxBodyLength) {
        this(commentRepository, storyRepository, userRepository, auditService, null, maxBodyLength);
    }

    @Autowired
    public CommentService(CommentRepository commentRepository, StoryRepository storyRepository, UserRepository userRepository,
                          AuditService auditService, CommentEngagementService commentEngagementService,
                          @Value("${app.comments.max-length:2000}") int maxBodyLength) {
        this.commentRepository = commentRepository;
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.commentEngagementService = commentEngagementService;
        this.maxBodyLength = maxBodyLength;
    }

    @Transactional(readOnly = true)
    public CommentPageResponse listPublic(UUID storyId, UUID currentUserId, int requestedLimit, String cursor) {
        requirePublishedStory(storyId);
        int limit = Math.min(Math.max(requestedLimit, 1), 50);
        Cursor decoded = decodeCursor(cursor);
        List<Comment> comments = decoded == null
                ? commentRepository.findVisibleInitial(storyId, PageRequest.of(0, limit + 1))
                : commentRepository.findVisibleAfter(storyId, decoded.createdAt(), decoded.id(), PageRequest.of(0, limit + 1));
        boolean hasMore = comments.size() > limit;
        if (hasMore) comments = comments.subList(0, limit);
        String nextCursor = hasMore && !comments.isEmpty() ? encodeCursor(comments.get(comments.size() - 1)) : null;
        Map<UUID, CommentEngagementService.CommentEngagement> engagements = commentEngagementService == null
                ? Map.of()
                : commentEngagementService.forComments(comments.stream().map(Comment::getId).toList(), currentUserId);
        return new CommentPageResponse(comments.stream().map(comment -> {
            CommentEngagementService.CommentEngagement engagement = engagements.get(comment.getId());
            return CommentResponse.from(comment, currentUserId,
                    engagement == null ? 0 : engagement.likeCount(),
                    engagement != null && engagement.likedByCurrentUser());
        }).toList(), nextCursor, hasMore);
    }

    @Transactional
    public CommentResponse create(UUID storyId, UUID userId, CreateCommentRequest request) {
        Story story = requirePublishedStory(storyId);
        String body = request.body().trim();
        validateBody(body);
        Comment parent = request.parentCommentId() == null ? null : requireReplyParent(story, request.parentCommentId());
        Comment comment = commentRepository.save(new Comment(story, userRepository.getReferenceById(userId), body, parent));
        return CommentResponse.from(comment, userId, 0, false);
    }

    @Transactional
    public void deleteOwn(UUID commentId, UUID userId) {
        Comment comment = requireComment(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            throw new RbacException(HttpStatus.FORBIDDEN, "COMMENT_NOT_OWNER", "You can only delete your own comments");
        }
        if (comment.getStatus() != CommentStatus.DELETED) comment.deleteByOwner();
    }

    @Transactional
    public CommentResponse updateOwn(UUID commentId, UUID userId, UpdateCommentRequest request) {
        Comment comment = requireComment(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            throw new RbacException(HttpStatus.FORBIDDEN, "COMMENT_NOT_OWNER", "You can only update your own comments");
        }
        if (comment.getStatus() != CommentStatus.VISIBLE) {
            throw new RbacException(HttpStatus.BAD_REQUEST, "COMMENT_NOT_EDITABLE", "This comment cannot be edited in its current state");
        }
        String body = request.body().trim();
        validateBody(body);
        comment.updateBody(body);
        return responseFor(comment, userId);
    }

    @Transactional(readOnly = true)
    public AdminCommentPageResponse listAdmin(String status, UUID storyId, String query, int requestedPage, int requestedLimit) {
        CommentStatus parsedStatus = parseStatus(status);
        int page = Math.max(requestedPage, 0);
        int limit = Math.min(Math.max(requestedLimit, 1), 100);
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Comment> comments = commentRepository.findAdmin(parsedStatus, storyId, normalizedQuery, PageRequest.of(page, limit + 1));
        boolean hasMore = comments.size() > limit;
        if (hasMore) comments = comments.subList(0, limit);
        return new AdminCommentPageResponse(comments.stream().map(AdminCommentResponse::from).toList(), page, limit, hasMore);
    }

    @Transactional(readOnly = true)
    public AdminCommentResponse getAdmin(UUID commentId) {
        return AdminCommentResponse.from(requireCommentWithDetails(commentId));
    }

    @Transactional
    public AdminCommentResponse hide(UUID commentId, UUID moderatorId, ModerationRequest request) {
        Comment comment = requireCommentWithDetails(commentId);
        if (comment.getStatus() == CommentStatus.DELETED) invalidTransition();
        User moderator = userRepository.getReferenceById(moderatorId);
        String reason = request == null || request.reason() == null || request.reason().isBlank() ? null : request.reason().trim();
        comment.hide(moderator, reason);
        auditService.record(moderatorId, "COMMENT_HIDDEN", "COMMENT", commentId, Map.of("storyId", comment.getStory().getId().toString()));
        return AdminCommentResponse.from(comment);
    }

    @Transactional
    public AdminCommentResponse restore(UUID commentId, UUID moderatorId) {
        Comment comment = requireCommentWithDetails(commentId);
        if (comment.getStatus() == CommentStatus.DELETED) invalidTransition();
        if (comment.getStatus() == CommentStatus.HIDDEN) {
            comment.restore(userRepository.getReferenceById(moderatorId));
            auditService.record(moderatorId, "COMMENT_RESTORED", "COMMENT", commentId, Map.of("storyId", comment.getStory().getId().toString()));
        }
        return AdminCommentResponse.from(comment);
    }

    @Transactional
    public AdminCommentResponse deleteByModerator(UUID commentId, UUID moderatorId) {
        Comment comment = requireCommentWithDetails(commentId);
        if (comment.getStatus() != CommentStatus.DELETED) {
            comment.deleteByModerator(userRepository.getReferenceById(moderatorId));
            auditService.record(moderatorId, "COMMENT_DELETED_BY_MODERATOR", "COMMENT", commentId, Map.of("storyId", comment.getStory().getId().toString()));
        }
        return AdminCommentResponse.from(comment);
    }

    private Story requirePublishedStory(UUID storyId) {
        return storyRepository.findById(storyId)
                .filter(story -> story.getStatus() == StoryStatus.PUBLISHED)
                .orElseThrow(() -> new RbacException(HttpStatus.NOT_FOUND, "STORY_NOT_FOUND", "Story not found or is not published"));
    }

    private Comment requireComment(UUID id) {
        return commentRepository.findWithModerationDetailsById(id)
                .orElseThrow(() -> new RbacException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "Comment not found"));
    }

    private Comment requireCommentWithDetails(UUID id) {
        return requireComment(id);
    }

    private Comment requireReplyParent(Story story, UUID parentCommentId) {
        Comment parent = requireComment(parentCommentId);
        if (!parent.getStory().getId().equals(story.getId()) || parent.getStatus() != CommentStatus.VISIBLE || parent.getParentComment() != null) {
            throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_COMMENT_PARENT", "Replies must target a visible top-level comment in this story");
        }
        return parent;
    }

    private void validateBody(String body) {
        if (body.isBlank()) throw new RbacException(HttpStatus.BAD_REQUEST, "COMMENT_EMPTY", "Comment body cannot be empty");
        if (body.length() > maxBodyLength) throw new RbacException(HttpStatus.BAD_REQUEST, "COMMENT_TOO_LONG", "Comment body must be " + maxBodyLength + " characters or fewer");
    }

    private CommentResponse responseFor(Comment comment, UUID currentUserId) {
        if (commentEngagementService == null) return CommentResponse.from(comment, currentUserId);
        CommentEngagementService.CommentEngagement engagement = commentEngagementService.forComments(List.of(comment.getId()), currentUserId).get(comment.getId());
        return CommentResponse.from(comment, currentUserId,
                engagement == null ? 0 : engagement.likeCount(),
                engagement != null && engagement.likedByCurrentUser());
    }

    private void invalidTransition() {
        throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_COMMENT_STATE", "This comment cannot transition from its current state");
    }

    private CommentStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return CommentStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_COMMENT_STATUS", "Comment status must be VISIBLE, HIDDEN, or DELETED");
        }
    }

    private String encodeCursor(Comment comment) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString((comment.getCreatedAt().toString() + "|" + comment.getId()).getBytes(StandardCharsets.UTF_8));
    }

    private Cursor decodeCursor(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(raw), StandardCharsets.UTF_8).split("\\|", 2);
            return new Cursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (Exception exception) {
            throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_CURSOR", "The comment cursor is invalid");
        }
    }

    private record Cursor(Instant createdAt, UUID id) {
    }
}
