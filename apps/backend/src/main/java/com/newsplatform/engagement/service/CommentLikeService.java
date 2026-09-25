package com.newsplatform.engagement.service;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.engagement.dto.CommentEngagementResponse;
import com.newsplatform.engagement.entity.Comment;
import com.newsplatform.engagement.entity.CommentStatus;
import com.newsplatform.engagement.repository.CommentLikeRepository;
import com.newsplatform.engagement.repository.CommentRepository;
import com.newsplatform.story.entity.StoryStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CommentLikeService {
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentEngagementService commentEngagementService;

    public CommentLikeService(CommentRepository commentRepository, CommentLikeRepository commentLikeRepository,
                               CommentEngagementService commentEngagementService) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.commentEngagementService = commentEngagementService;
    }

    @Transactional
    public CommentEngagementResponse like(UUID commentId, UUID userId) {
        Comment comment = requireVisibleComment(commentId);
        commentLikeRepository.insertIfAbsent(userId, comment.getId());
        return currentState(comment.getId(), userId);
    }

    @Transactional
    public CommentEngagementResponse unlike(UUID commentId, UUID userId) {
        Comment comment = requireVisibleComment(commentId);
        commentLikeRepository.deleteForUserAndComment(userId, comment.getId());
        return currentState(comment.getId(), userId);
    }

    private CommentEngagementResponse currentState(UUID commentId, UUID userId) {
        CommentEngagementService.CommentEngagement engagement = commentEngagementService.forComments(java.util.List.of(commentId), userId).get(commentId);
        return new CommentEngagementResponse(engagement.likeCount(), engagement.likedByCurrentUser());
    }

    private Comment requireVisibleComment(UUID commentId) {
        return commentRepository.findWithModerationDetailsById(commentId)
                .filter(comment -> comment.getStatus() == CommentStatus.VISIBLE)
                .filter(comment -> comment.getStory().getStatus() == StoryStatus.PUBLISHED)
                .orElseThrow(() -> new RbacException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "Comment not found"));
    }
}
