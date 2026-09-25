package com.newsplatform.advertisement.service;

import com.newsplatform.advertisement.dto.AdminAdvertisementListResponse;
import com.newsplatform.advertisement.dto.AdminAdvertisementResponse;
import com.newsplatform.advertisement.dto.AdvertisementPlacementRequest;
import com.newsplatform.advertisement.dto.AdvertisementRequest;
import com.newsplatform.advertisement.dto.PublicAdvertisementResponse;
import com.newsplatform.advertisement.entity.Advertisement;
import com.newsplatform.advertisement.entity.AdvertisementMediaType;
import com.newsplatform.advertisement.entity.AdvertisementPlacement;
import com.newsplatform.advertisement.entity.AdvertisementPlacementType;
import com.newsplatform.advertisement.entity.AdvertisementStatus;
import com.newsplatform.advertisement.repository.AdvertisementRepository;
import com.newsplatform.category.entity.Category;
import com.newsplatform.category.repository.CategoryRepository;
import com.newsplatform.common.error.RbacException;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.story.entity.StoryMedia;
import com.newsplatform.story.repository.StoryMediaRepository;
import com.newsplatform.story.service.MediaStorageService;
import com.newsplatform.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdvertisementService {
    private final AdvertisementRepository repository;
    private final CategoryRepository categoryRepository;
    private final StoryMediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final MediaStorageService mediaStorageService;
    private final AuditService auditService;
    private final Clock clock;

    public AdvertisementService(AdvertisementRepository repository, CategoryRepository categoryRepository,
                                 StoryMediaRepository mediaRepository, UserRepository userRepository,
                                 MediaStorageService mediaStorageService, AuditService auditService, Clock clock) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
        this.mediaStorageService = mediaStorageService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminAdvertisementListResponse listAdmin(String search, String status, String placement, UUID categoryId, int page, int limit) {
        AdvertisementStatus requestedStatus = parseStatus(status);
        AdvertisementPlacementType requestedPlacement = parsePlacement(placement, true);
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        int safePage = Math.max(page, 0);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        // Status is derived from the schedule at read time, so filtering only on the persisted
        // value would hide scheduled campaigns that have just become active (or expired).
        List<Advertisement> all = repository.findAdmin(normalizedSearch, null,
                requestedPlacement, categoryId, PageRequest.of(0, 500));
        Instant now = now();
        List<Advertisement> filtered = all.stream()
                .filter(ad -> requestedStatus == null || ad.statusAt(now) == requestedStatus)
                .toList();
        int from = Math.min(safePage * safeLimit, filtered.size());
        int to = Math.min(from + safeLimit, filtered.size());
        List<AdminAdvertisementResponse> items = filtered.subList(from, to).stream().map(ad -> AdminAdvertisementResponse.from(ad, now)).toList();
        return new AdminAdvertisementListResponse(items, safePage, safeLimit, to < filtered.size());
    }

    @Transactional(readOnly = true)
    public AdminAdvertisementResponse getAdmin(UUID id) { return AdminAdvertisementResponse.from(require(id), now()); }

    @Transactional
    public AdminAdvertisementResponse create(AdvertisementRequest request, UUID actorId) {
        validateDates(request.startAt(), request.endAt());
        validateDestination(request.destinationUrl());
        List<AdvertisementPlacement> placements = resolvePlacements(request.placement());
        StoryMedia media = requireUploadedMedia(request.mediaId());
        Advertisement advertisement = new Advertisement(required(request.title()), required(request.advertiserName()), trim(request.description()),
                normalizeDestination(request.destinationUrl()), request.startAt(), request.endAt(), userRepository.getReferenceById(actorId));
        applyMedia(advertisement, media);
        advertisement.replacePlacements(placements);
        repository.save(advertisement);
        mediaRepository.delete(media);
        auditService.record(actorId, "AD_CREATED", "ADVERTISEMENT", advertisement.getId(), Map.of("title", advertisement.getTitle()));
        return AdminAdvertisementResponse.from(advertisement, now());
    }

    @Transactional
    public AdminAdvertisementResponse update(UUID id, AdvertisementRequest request, UUID actorId) {
        Advertisement advertisement = require(id);
        validateDates(request.startAt(), request.endAt());
        validateDestination(request.destinationUrl());
        List<AdvertisementPlacement> placements = resolvePlacements(request.placement());
        String previousStorageKey = advertisement.getMediaStorageKey();
        boolean placementChanged = placementSignature(advertisement).equals(placementSignature(placements)) == false;
        advertisement.update(required(request.title()), required(request.advertiserName()), trim(request.description()),
                normalizeDestination(request.destinationUrl()), request.startAt(), request.endAt());
        if (request.mediaId() != null) {
            StoryMedia media = requireUploadedMedia(request.mediaId());
            applyMedia(advertisement, media);
            mediaRepository.delete(media);
            if (!previousStorageKey.equals(advertisement.getMediaStorageKey())) mediaStorageService.delete(previousStorageKey);
        }
        advertisement.replacePlacements(placements);
        if (advertisement.getStatus() != AdvertisementStatus.DRAFT && advertisement.getStatus() != AdvertisementStatus.PAUSED
                && advertisement.getStatus() != AdvertisementStatus.EXPIRED) {
            advertisement.setStatus(statusForSchedule(advertisement.getStartAt(), advertisement.getEndAt(), now()));
        }
        auditService.record(actorId, "AD_UPDATED", "ADVERTISEMENT", id, Map.of("title", advertisement.getTitle()));
        if (placementChanged) auditService.record(actorId, "AD_PLACEMENT_CHANGED", "ADVERTISEMENT", id, Map.of("placements", placementSignature(placements)));
        return AdminAdvertisementResponse.from(advertisement, now());
    }

    @Transactional
    public AdminAdvertisementResponse publish(UUID id, UUID actorId) {
        Advertisement advertisement = require(id);
        Instant now = now();
        if (!now.isBefore(advertisement.getEndAt())) throw bad("AD_ALREADY_EXPIRED", "An advertisement cannot be published after its end time");
        advertisement.setStatus(statusForSchedule(advertisement.getStartAt(), advertisement.getEndAt(), now));
        auditService.record(actorId, "AD_PUBLISHED", "ADVERTISEMENT", id, Map.of("status", advertisement.getStatus().name()));
        return AdminAdvertisementResponse.from(advertisement, now);
    }

    @Transactional
    public AdminAdvertisementResponse pause(UUID id, UUID actorId) {
        Advertisement advertisement = require(id);
        Instant now = now();
        if (advertisement.statusAt(now) == AdvertisementStatus.EXPIRED) {
            advertisement.setStatus(AdvertisementStatus.EXPIRED);
            throw bad("AD_ALREADY_EXPIRED", "An expired advertisement cannot be paused");
        }
        if (advertisement.getStatus() != AdvertisementStatus.ACTIVE && advertisement.getStatus() != AdvertisementStatus.SCHEDULED) {
            throw bad("AD_NOT_PAUSABLE", "Only active or scheduled advertisements can be paused");
        }
        advertisement.setStatus(AdvertisementStatus.PAUSED);
        auditService.record(actorId, "AD_PAUSED", "ADVERTISEMENT", id, Map.of());
        return AdminAdvertisementResponse.from(advertisement, now);
    }

    @Transactional
    public AdminAdvertisementResponse resume(UUID id, UUID actorId) {
        Advertisement advertisement = require(id);
        Instant now = now();
        if (now.compareTo(advertisement.getEndAt()) >= 0) {
            advertisement.setStatus(AdvertisementStatus.EXPIRED);
            throw bad("AD_ALREADY_EXPIRED", "An expired advertisement cannot be resumed");
        }
        if (advertisement.getStatus() != AdvertisementStatus.PAUSED) throw bad("AD_NOT_RESUMABLE", "Only paused advertisements can be resumed");
        advertisement.setStatus(statusForSchedule(advertisement.getStartAt(), advertisement.getEndAt(), now));
        auditService.record(actorId, "AD_RESUMED", "ADVERTISEMENT", id, Map.of("status", advertisement.getStatus().name()));
        return AdminAdvertisementResponse.from(advertisement, now);
    }

    @Transactional
    public void delete(UUID id, UUID actorId) {
        Advertisement advertisement = require(id);
        repository.delete(advertisement);
        mediaStorageService.delete(advertisement.getMediaStorageKey());
        auditService.record(actorId, "AD_DELETED", "ADVERTISEMENT", id, Map.of("title", advertisement.getTitle()));
    }

    @Transactional(readOnly = true)
    public List<PublicAdvertisementResponse> publicAds(String placement, String categorySlug) {
        AdvertisementPlacementType type = parsePlacement(placement, false);
        Instant now = now();
        Category requestedCategory = null;
        if (type == AdvertisementPlacementType.CATEGORY_FEED) {
            if (categorySlug == null || categorySlug.isBlank()) throw bad("CATEGORY_REQUIRED", "A category is required for category feed advertisements");
            requestedCategory = categoryRepository.findBySlug(categorySlug.trim()).orElse(null);
            if (requestedCategory == null) return List.of();
        }
        Category category = requestedCategory;
        List<Advertisement> eligible = repository.findEligible(type, now).stream()
                .filter(ad -> ad.getPlacements().stream().anyMatch(placementItem -> matches(placementItem, type, category)))
                .toList();
        if (eligible.isEmpty()) return List.of();
        int offset = (int) ((now.getEpochSecond() / 60) % eligible.size());
        List<Advertisement> rotated = new ArrayList<>(eligible.size());
        rotated.addAll(eligible.subList(offset, eligible.size()));
        rotated.addAll(eligible.subList(0, offset));
        return rotated.stream().map(ad -> PublicAdvertisementResponse.from(ad, type.name())).toList();
    }

    private boolean matches(AdvertisementPlacement placement, AdvertisementPlacementType requestedType, Category requestedCategory) {
        if (placement.getPlacementType() != requestedType) return false;
        if (requestedType != AdvertisementPlacementType.CATEGORY_FEED) return placement.getCategory() == null;
        Category target = placement.getCategory();
        return target != null && (target.getId().equals(requestedCategory.getId())
                || (requestedCategory.getParent() != null && target.getId().equals(requestedCategory.getParent().getId())));
    }

    private List<AdvertisementPlacement> resolvePlacements(AdvertisementPlacementRequest request) {
        AdvertisementPlacementType type = parsePlacement(request.type(), false);
        List<UUID> requestedIds = request.categoryIds() == null ? List.of() : request.categoryIds().stream().distinct().toList();
        if (type != AdvertisementPlacementType.CATEGORY_FEED && !requestedIds.isEmpty()) {
            throw bad("CATEGORY_NOT_ALLOWED", "Only category feed advertisements can target categories");
        }
        if (type == AdvertisementPlacementType.CATEGORY_FEED && requestedIds.isEmpty()) {
            throw bad("CATEGORY_REQUIRED", "Category feed advertisements must target at least one category");
        }
        if (type != AdvertisementPlacementType.CATEGORY_FEED) return List.of(new AdvertisementPlacement(type, null));
        List<Category> categories = categoryRepository.findAllById(requestedIds);
        if (categories.size() != requestedIds.size()) throw bad("CATEGORY_NOT_FOUND", "One or more target categories do not exist");
        Map<UUID, Category> byId = categories.stream().collect(Collectors.toMap(Category::getId, category -> category));
        return requestedIds.stream().map(id -> new AdvertisementPlacement(type, byId.get(id))).toList();
    }

    private StoryMedia requireUploadedMedia(UUID mediaId) {
        if (mediaId == null) throw bad("AD_MEDIA_REQUIRED", "Upload an image or video creative before saving the advertisement");
        StoryMedia media = mediaRepository.findById(mediaId).orElseThrow(() -> bad("INVALID_MEDIA", "The uploaded creative could not be found"));
        if (media.getStory() != null) throw bad("INVALID_MEDIA", "That creative is already attached to a story");
        return media;
    }

    private void applyMedia(Advertisement advertisement, StoryMedia media) {
        AdvertisementMediaType type = media.getType() == com.newsplatform.story.entity.StoryMediaType.IMAGE ? AdvertisementMediaType.IMAGE : AdvertisementMediaType.VIDEO;
        advertisement.setMedia(type, media.getStorageKey(), media.getUrl(), media.getThumbnailUrl(), media.getMimeType(), media.getFileSize());
    }

    private Advertisement require(UUID id) { return repository.findWithDetailsById(id).orElseThrow(() -> new RbacException(HttpStatus.NOT_FOUND, "AD_NOT_FOUND", "Advertisement not found")); }

    private AdvertisementStatus parseStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try { return AdvertisementStatus.valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw bad("INVALID_AD_STATUS", "Status must be DRAFT, SCHEDULED, ACTIVE, PAUSED, or EXPIRED"); }
    }

    private AdvertisementPlacementType parsePlacement(String value, boolean optional) {
        if (value == null || value.isBlank()) {
            if (optional) return null;
            throw bad("INVALID_AD_PLACEMENT", "A valid advertisement placement is required");
        }
        try { return AdvertisementPlacementType.valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw bad("INVALID_AD_PLACEMENT", "Placement must be HOME_BANNER, HOME_FEED, CATEGORY_FEED, or NEWSPAPER"); }
    }

    private void validateDates(Instant startAt, Instant endAt) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) throw bad("INVALID_AD_DATES", "End time must be after start time");
    }

    private void validateDestination(String value) {
        if (value == null || value.isBlank()) return;
        try {
            URI uri = URI.create(value.trim());
            if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) { throw bad("INVALID_DESTINATION_URL", "Destination URL must be a valid HTTP or HTTPS URL"); }
    }

    private AdvertisementStatus statusForSchedule(Instant startAt, Instant endAt, Instant now) {
        if (now.compareTo(endAt) >= 0) return AdvertisementStatus.EXPIRED;
        return now.isBefore(startAt) ? AdvertisementStatus.SCHEDULED : AdvertisementStatus.ACTIVE;
    }

    private String placementSignature(Advertisement advertisement) { return placementSignature(advertisement.getPlacements()); }
    private String placementSignature(List<AdvertisementPlacement> placements) {
        return placements.stream().map(item -> item.getPlacementType().name() + ":" + (item.getCategory() == null ? "" : item.getCategory().getId()))
                .sorted().collect(Collectors.joining(","));
    }

    private String required(String value) { return value == null ? "" : value.trim(); }
    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String normalizeDestination(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private RbacException bad(String code, String message) { return new RbacException(HttpStatus.BAD_REQUEST, code, message); }
    private Instant now() { return Instant.now(clock); }
}
