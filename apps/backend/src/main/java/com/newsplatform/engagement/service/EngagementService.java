package com.newsplatform.engagement.service;

import com.newsplatform.engagement.repository.CommentRepository;
import com.newsplatform.engagement.repository.StoryLikeRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EngagementService {
    private final StoryLikeRepository storyLikeRepository;
    private final CommentRepository commentRepository;

    public EngagementService(StoryLikeRepository storyLikeRepository, CommentRepository commentRepository) {
        this.storyLikeRepository = storyLikeRepository;
        this.commentRepository = commentRepository;
    }

    public Map<UUID, StoryEngagement> forStories(Collection<UUID> storyIds, UUID currentUserId) {
        if (storyIds == null || storyIds.isEmpty()) return Map.of();

        Map<UUID, Long> likes = storyLikeRepository.countByStoryIds(storyIds).stream()
                .collect(Collectors.toMap(StoryLikeRepository.StoryCountProjection::getStoryId, StoryLikeRepository.StoryCountProjection::getCount));
        Map<UUID, Long> comments = commentRepository.countVisibleByStoryIds(storyIds).stream()
                .collect(Collectors.toMap(CommentRepository.StoryCountProjection::getStoryId, CommentRepository.StoryCountProjection::getCount));
        Set<UUID> likedStories = currentUserId == null
                ? Collections.emptySet()
                : storyLikeRepository.findLikedStoryIds(currentUserId, storyIds);

        return storyIds.stream().distinct().collect(Collectors.toMap(
                Function.identity(),
                storyId -> new StoryEngagement(likes.getOrDefault(storyId, 0L), comments.getOrDefault(storyId, 0L), likedStories.contains(storyId))
        ));
    }

    public record StoryEngagement(long likeCount, long commentCount, boolean likedByCurrentUser) {
    }
}
