package com.newsplatform.engagement.service;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.engagement.dto.StoryEngagementResponse;
import com.newsplatform.engagement.repository.StoryLikeRepository;
import com.newsplatform.story.entity.Story;
import com.newsplatform.story.entity.StoryStatus;
import com.newsplatform.story.repository.StoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class LikeService {
    private final StoryRepository storyRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final EngagementService engagementService;

    public LikeService(StoryRepository storyRepository, StoryLikeRepository storyLikeRepository, EngagementService engagementService) {
        this.storyRepository = storyRepository;
        this.storyLikeRepository = storyLikeRepository;
        this.engagementService = engagementService;
    }

    @Transactional
    public StoryEngagementResponse like(UUID storyId, UUID userId) {
        requirePublishedStory(storyId);
        storyLikeRepository.insertIfAbsent(userId, storyId);
        return currentState(storyId, userId);
    }

    @Transactional
    public StoryEngagementResponse unlike(UUID storyId, UUID userId) {
        requirePublishedStory(storyId);
        storyLikeRepository.deleteForUserAndStory(userId, storyId);
        return currentState(storyId, userId);
    }

    private StoryEngagementResponse currentState(UUID storyId, UUID userId) {
        EngagementService.StoryEngagement engagement = engagementService.forStories(java.util.List.of(storyId), userId).get(storyId);
        return new StoryEngagementResponse(engagement.likeCount(), engagement.commentCount(), engagement.likedByCurrentUser());
    }

    private Story requirePublishedStory(UUID storyId) {
        return storyRepository.findById(storyId)
                .filter(story -> story.getStatus() == StoryStatus.PUBLISHED)
                .orElseThrow(() -> new RbacException(HttpStatus.NOT_FOUND, "STORY_NOT_FOUND", "Story not found or is not published"));
    }
}
