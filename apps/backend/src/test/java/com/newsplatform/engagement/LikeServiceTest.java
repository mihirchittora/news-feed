package com.newsplatform.engagement;

import com.newsplatform.engagement.dto.StoryEngagementResponse;
import com.newsplatform.engagement.repository.StoryLikeRepository;
import com.newsplatform.engagement.service.EngagementService;
import com.newsplatform.engagement.service.LikeService;
import com.newsplatform.story.entity.Story;
import com.newsplatform.story.repository.StoryRepository;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {
    @Mock private StoryRepository storyRepository;
    @Mock private StoryLikeRepository storyLikeRepository;
    @Mock private EngagementService engagementService;

    private LikeService likeService;

    @BeforeEach
    void setUp() {
        likeService = new LikeService(storyRepository, storyLikeRepository, engagementService);
    }

    @Test
    void usesTheDatabaseConflictSafeInsertAndReturnsCurrentState() {
        UUID storyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Story story = new Story("Published story", "published-story", "Summary", "Body", null,
                new User("Author", "author@example.com", "hash", UserStatus.ACTIVE));
        story.publish();
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(engagementService.forStories(any(), any())).thenReturn(Map.of(storyId, new EngagementService.StoryEngagement(4, 2, true)));

        StoryEngagementResponse response = likeService.like(storyId, userId);

        verify(storyLikeRepository).insertIfAbsent(userId, storyId);
        assertThat(response.likeCount()).isEqualTo(4);
        assertThat(response.likedByCurrentUser()).isTrue();
    }

    @Test
    void unlikeIsSafeWhenTheLikeDoesNotExist() {
        UUID storyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Story story = new Story("Published story", "published-story", "Summary", "Body", null,
                new User("Author", "author@example.com", "hash", UserStatus.ACTIVE));
        story.publish();
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(engagementService.forStories(any(), any())).thenReturn(Map.of(storyId, new EngagementService.StoryEngagement(0, 0, false)));

        StoryEngagementResponse response = likeService.unlike(storyId, userId);

        verify(storyLikeRepository).deleteForUserAndStory(userId, storyId);
        assertThat(response.likeCount()).isZero();
        assertThat(response.likedByCurrentUser()).isFalse();
    }
}
