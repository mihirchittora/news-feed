package com.newsplatform.engagement.service;

import com.newsplatform.engagement.repository.CommentLikeRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommentEngagementService {
    private final CommentLikeRepository commentLikeRepository;

    public CommentEngagementService(CommentLikeRepository commentLikeRepository) {
        this.commentLikeRepository = commentLikeRepository;
    }

    public Map<UUID, CommentEngagement> forComments(Collection<UUID> commentIds, UUID currentUserId) {
        if (commentIds == null || commentIds.isEmpty()) return Map.of();

        Map<UUID, Long> likes = commentLikeRepository.countByCommentIds(commentIds).stream()
                .collect(Collectors.toMap(CommentLikeRepository.CommentCountProjection::getCommentId, CommentLikeRepository.CommentCountProjection::getCount));
        Set<UUID> likedComments = currentUserId == null
                ? Collections.emptySet()
                : commentLikeRepository.findLikedCommentIds(currentUserId, commentIds);

        return commentIds.stream().distinct().collect(Collectors.toMap(
                Function.identity(),
                commentId -> new CommentEngagement(likes.getOrDefault(commentId, 0L), likedComments.contains(commentId))
        ));
    }

    public record CommentEngagement(long likeCount, boolean likedByCurrentUser) {
    }
}
