package com.newsplatform.story;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.story.dto.BreakingNewsRequest;
import com.newsplatform.story.entity.Story;
import com.newsplatform.story.repository.StoryRepository;
import com.newsplatform.story.service.BreakingNewsService;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BreakingNewsServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-25T10:00:00Z");

    @Mock private StoryRepository storyRepository;
    @Mock private AuditService auditService;

    private BreakingNewsService service;

    @BeforeEach
    void setUp() {
        service = new BreakingNewsService(storyRepository, auditService, Clock.fixed(NOW, ZoneOffset.UTC), 120);
    }

    @Test
    void enablesPublishedStoryWithConfiguredDefaultExpiry() {
        Story story = publishedStory();
        UUID storyId = UUID.randomUUID();
        when(storyRepository.findWithDetailsById(storyId)).thenReturn(Optional.of(story));

        service.enable(storyId, null, UUID.randomUUID());

        assertThat(story.isBreaking()).isTrue();
        assertThat(story.getBreakingStartedAt()).isEqualTo(NOW);
        assertThat(story.getBreakingUntil()).isEqualTo(NOW.plusSeconds(120 * 60L));
        verify(auditService).record(any(), org.mockito.ArgumentMatchers.eq("BREAKING_NEWS_ENABLED"), org.mockito.ArgumentMatchers.eq("STORY"), org.mockito.ArgumentMatchers.eq(storyId), any());
    }

    @Test
    void persistsExplicitExpiryAndCanRemainActiveUntilManuallyRemoved() {
        Story story = publishedStory();
        UUID storyId = UUID.randomUUID();
        Instant expiry = NOW.plusSeconds(3600);
        when(storyRepository.findWithDetailsById(storyId)).thenReturn(Optional.of(story));

        service.enable(storyId, new BreakingNewsRequest(expiry, null), UUID.randomUUID());
        assertThat(story.getBreakingUntil()).isEqualTo(expiry);

        service.enable(storyId, new BreakingNewsRequest(null, true), UUID.randomUUID());
        assertThat(story.getBreakingUntil()).isNull();
        assertThat(story.isActivelyBreaking(NOW.plusSeconds(24 * 60 * 60L))).isTrue();
    }

    @Test
    void rejectsDraftsAndExpiredExplicitDates() {
        Story draft = new Story("Draft", "draft", "Summary", "Body", null,
                new User("Author", "author@example.com", "hash", UserStatus.ACTIVE));
        UUID storyId = UUID.randomUUID();
        when(storyRepository.findWithDetailsById(storyId)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.enable(storyId, null, UUID.randomUUID()))
                .isInstanceOf(RbacException.class)
                .hasMessageContaining("Only published");
        verify(auditService, never()).record(any(), any(), any(), any(), any());

        Story published = publishedStory();
        when(storyRepository.findWithDetailsById(storyId)).thenReturn(Optional.of(published));
        assertThatThrownBy(() -> service.enable(storyId, new BreakingNewsRequest(NOW, null), UUID.randomUUID()))
                .isInstanceOf(RbacException.class)
                .hasMessageContaining("in the future");
    }

    @Test
    void disablingClearsAllBreakingFieldsAndUnpublishingDoesNotRestoreThem() {
        Story story = publishedStory();
        story.enableBreaking(NOW, NOW.plusSeconds(7200));
        UUID storyId = UUID.randomUUID();
        when(storyRepository.findWithDetailsById(storyId)).thenReturn(Optional.of(story));

        service.disable(storyId, UUID.randomUUID());
        assertThat(story.isBreaking()).isFalse();
        assertThat(story.getBreakingStartedAt()).isNull();
        assertThat(story.getBreakingUntil()).isNull();

        story.enableBreaking(NOW, NOW.plusSeconds(7200));
        story.unpublish();
        assertThat(story.isBreaking()).isFalse();
        assertThat(story.isActivelyBreaking(NOW)).isFalse();
    }

    private Story publishedStory() {
        Story story = new Story("Published story", "published-story", "Summary", "Body", null,
                new User("Author", "author@example.com", "hash", UserStatus.ACTIVE));
        story.publish();
        return story;
    }
}
