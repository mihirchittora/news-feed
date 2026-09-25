package com.newsplatform.advertisement;

import com.newsplatform.advertisement.dto.AdvertisementPlacementRequest;
import com.newsplatform.advertisement.dto.AdvertisementRequest;
import com.newsplatform.advertisement.entity.Advertisement;
import com.newsplatform.advertisement.entity.AdvertisementPlacement;
import com.newsplatform.advertisement.entity.AdvertisementPlacementType;
import com.newsplatform.advertisement.repository.AdvertisementRepository;
import com.newsplatform.advertisement.service.AdvertisementService;
import com.newsplatform.category.entity.Category;
import com.newsplatform.category.entity.CategoryStatus;
import com.newsplatform.category.repository.CategoryRepository;
import com.newsplatform.common.error.RbacException;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.story.entity.StoryMedia;
import com.newsplatform.story.entity.StoryMediaType;
import com.newsplatform.story.repository.StoryMediaRepository;
import com.newsplatform.story.service.MediaStorageService;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.entity.UserStatus;
import com.newsplatform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-25T10:00:00Z");
    private static final UUID ACTOR_ID = UUID.randomUUID();

    @Mock private AdvertisementRepository repository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private StoryMediaRepository mediaRepository;
    @Mock private UserRepository userRepository;
    @Mock private MediaStorageService mediaStorageService;
    @Mock private AuditService auditService;

    private AdvertisementService service;
    private User actor;

    @BeforeEach
    void setUp() {
        actor = new User("Editor", "editor@example.com", "hash", UserStatus.ACTIVE);
        service = new AdvertisementService(repository, categoryRepository, mediaRepository, userRepository, mediaStorageService, auditService, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void rejectsInvalidDatesAndArbitraryPlacements() {
        AdvertisementRequest invalidDates = request(NOW.plusSeconds(60), NOW, "HOME_BANNER", List.of());
        assertThatThrownBy(() -> service.create(invalidDates, ACTOR_ID)).isInstanceOfSatisfying(RbacException.class,
                error -> assertThat(error.getCode()).isEqualTo("INVALID_AD_DATES"));

        AdvertisementRequest invalidPlacement = request(NOW, NOW.plusSeconds(3600), "TOP_RIGHT_HEADER", List.of());
        assertThatThrownBy(() -> service.create(invalidPlacement, ACTOR_ID)).isInstanceOfSatisfying(RbacException.class,
                error -> assertThat(error.getCode()).isEqualTo("INVALID_AD_PLACEMENT"));
        verify(mediaRepository, never()).findById(any());
    }

    @Test
    void requiresCategoriesOnlyForCategoryFeed() {
        AdvertisementRequest missingCategories = request(NOW, NOW.plusSeconds(3600), "CATEGORY_FEED", List.of());
        assertThatThrownBy(() -> service.create(missingCategories, ACTOR_ID)).isInstanceOfSatisfying(RbacException.class,
                error -> assertThat(error.getCode()).isEqualTo("CATEGORY_REQUIRED"));
    }

    @Test
    void publishesFutureCampaignAsScheduledAndCurrentCampaignAsActive() {
        Advertisement future = campaign(NOW.plusSeconds(3600), NOW.plusSeconds(7200));
        UUID futureId = future.getId();
        when(repository.findWithDetailsById(futureId)).thenReturn(Optional.of(future));
        service.publish(futureId, ACTOR_ID);
        assertThat(future.getStatus()).isEqualTo(com.newsplatform.advertisement.entity.AdvertisementStatus.SCHEDULED);

        Advertisement current = campaign(NOW.minusSeconds(60), NOW.plusSeconds(3600));
        UUID currentId = current.getId();
        when(repository.findWithDetailsById(currentId)).thenReturn(Optional.of(current));
        service.publish(currentId, ACTOR_ID);
        assertThat(current.getStatus()).isEqualTo(com.newsplatform.advertisement.entity.AdvertisementStatus.ACTIVE);

        Advertisement expired = campaign(NOW.minusSeconds(7200), NOW.minusSeconds(60));
        UUID expiredId = expired.getId();
        when(repository.findWithDetailsById(expiredId)).thenReturn(Optional.of(expired));
        assertThatThrownBy(() -> service.publish(expiredId, ACTOR_ID)).isInstanceOfSatisfying(RbacException.class,
                error -> assertThat(error.getCode()).isEqualTo("AD_ALREADY_EXPIRED"));
    }

    @Test
    void pausesAndResumesAccordingToSchedule() {
        Advertisement active = campaign(NOW.minusSeconds(60), NOW.plusSeconds(3600));
        active.setStatus(com.newsplatform.advertisement.entity.AdvertisementStatus.ACTIVE);
        UUID id = active.getId();
        when(repository.findWithDetailsById(id)).thenReturn(Optional.of(active));
        service.pause(id, ACTOR_ID);
        assertThat(active.getStatus()).isEqualTo(com.newsplatform.advertisement.entity.AdvertisementStatus.PAUSED);
        service.resume(id, ACTOR_ID);
        assertThat(active.getStatus()).isEqualTo(com.newsplatform.advertisement.entity.AdvertisementStatus.ACTIVE);
    }

    @Test
    void parentCategoryCampaignIsEligibleForChildButNotSibling() {
        UUID sportsId = UUID.randomUUID();
        UUID cricketId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();
        Category sports = new Category("Sports", "sports", null, CategoryStatus.ACTIVE, 0);
        Category cricket = new Category("Cricket", "cricket", null, CategoryStatus.ACTIVE, 0, sports);
        Category business = new Category("Business", "business", null, CategoryStatus.ACTIVE, 0);
        ReflectionTestUtils.setField(sports, "id", sportsId);
        ReflectionTestUtils.setField(cricket, "id", cricketId);
        ReflectionTestUtils.setField(business, "id", businessId);
        Advertisement ad = campaign(NOW.minusSeconds(60), NOW.plusSeconds(3600));
        ad.setStatus(com.newsplatform.advertisement.entity.AdvertisementStatus.ACTIVE);
        ad.replacePlacements(List.of(new AdvertisementPlacement(AdvertisementPlacementType.CATEGORY_FEED, sports)));
        when(repository.findEligible(AdvertisementPlacementType.CATEGORY_FEED, NOW)).thenReturn(List.of(ad));
        when(categoryRepository.findBySlug("cricket")).thenReturn(Optional.of(cricket));
        when(categoryRepository.findBySlug("business")).thenReturn(Optional.of(business));

        assertThat(service.publicAds("CATEGORY_FEED", "cricket")).hasSize(1);
        assertThat(service.publicAds("CATEGORY_FEED", "business")).isEmpty();
    }

    @Test
    void replacingAnUnchangedPlacementKeepsTheExistingRow() {
        Advertisement ad = campaign(NOW.minusSeconds(60), NOW.plusSeconds(3600));
        AdvertisementPlacement existing = new AdvertisementPlacement(AdvertisementPlacementType.HOME_FEED, null);
        ad.replacePlacements(List.of(existing));

        ad.replacePlacements(List.of(new AdvertisementPlacement(AdvertisementPlacementType.HOME_FEED, null)));

        assertThat(ad.getPlacements()).containsExactly(existing);
        assertThat(existing.getAdvertisement()).isSameAs(ad);
    }

    private AdvertisementRequest request(Instant start, Instant end, String placement, List<UUID> categoryIds) {
        return new AdvertisementRequest("Offer", "Advertiser", "Description", "https://example.com", start, end,
                new AdvertisementPlacementRequest(placement, categoryIds), UUID.randomUUID());
    }

    private Advertisement campaign(Instant start, Instant end) {
        Advertisement advertisement = new Advertisement("Offer", "Advertiser", "Description", "https://example.com", start, end, actor);
        ReflectionTestUtils.setField(advertisement, "id", UUID.randomUUID());
        advertisement.setMedia(com.newsplatform.advertisement.entity.AdvertisementMediaType.IMAGE, "creative.jpg", "http://localhost:8080/api/v1/media/creative.jpg", null, "image/jpeg", 10);
        return advertisement;
    }
}
