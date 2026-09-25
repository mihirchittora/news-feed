package com.newsplatform.story.service;

import com.newsplatform.category.entity.Category;
import com.newsplatform.category.repository.CategoryRepository;
import com.newsplatform.common.error.RbacException;
import com.newsplatform.common.util.SlugUtils;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.story.dto.AdminStoryResponse;
import com.newsplatform.story.dto.FeedResponse;
import com.newsplatform.story.dto.StoryRequest;
import com.newsplatform.story.dto.StoryResponse;
import com.newsplatform.story.dto.StorySummaryResponse;
import com.newsplatform.story.entity.Story;
import com.newsplatform.story.entity.StoryMedia;
import com.newsplatform.story.entity.StoryStatus;
import com.newsplatform.story.repository.StoryMediaRepository;
import com.newsplatform.story.repository.StoryRepository;
import com.newsplatform.tag.entity.Tag;
import com.newsplatform.tag.repository.TagRepository;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoryService {
    private final StoryRepository storyRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final StoryMediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final HtmlSanitizer htmlSanitizer;
    private final MediaStorageService mediaStorageService;

    public StoryService(StoryRepository storyRepository, CategoryRepository categoryRepository, TagRepository tagRepository,
                        StoryMediaRepository mediaRepository, UserRepository userRepository, AuditService auditService,
                        HtmlSanitizer htmlSanitizer, MediaStorageService mediaStorageService) {
        this.storyRepository = storyRepository; this.categoryRepository = categoryRepository; this.tagRepository = tagRepository;
        this.mediaRepository = mediaRepository; this.userRepository = userRepository; this.auditService = auditService;
        this.htmlSanitizer = htmlSanitizer; this.mediaStorageService = mediaStorageService;
    }

    @Transactional(readOnly = true)
    public List<AdminStoryResponse> listAdmin(String search, String status, UUID categoryId, int page, int limit) {
        StoryStatus parsed = parseStatus(status);
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return storyRepository.findAdmin(normalizedSearch, parsed, categoryId, PageRequest.of(Math.max(page, 0), Math.min(Math.max(limit, 1), 100))).stream().map(AdminStoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AdminStoryResponse getAdmin(UUID id) { return AdminStoryResponse.from(requireDetails(id)); }

    @Transactional
    public AdminStoryResponse create(StoryRequest request, UUID actorId) {
        String title = request.title().trim();
        Story story = new Story(title, uniqueSlug(title, null), trim(request.summary()), sanitize(request.body()), resolveCategory(request.categoryId()), userRepository.getReferenceById(actorId));
        applyRelations(story, request);
        storyRepository.save(story);
        auditService.record(actorId, "STORY_CREATED", "STORY", story.getId(), Map.of("slug", story.getSlug()));
        return AdminStoryResponse.from(story);
    }

    @Transactional
    public AdminStoryResponse update(UUID id, StoryRequest request, UUID actorId) {
        Story story = requireDetails(id); String title = request.title().trim();
        String slug = story.getStatus() == StoryStatus.PUBLISHED ? story.getSlug() : uniqueSlug(title, id);
        List<StoryMedia> previous = List.copyOf(story.getMedia());
        Set<UUID> requestedMedia = request.mediaIds() == null ? Set.of() : new HashSet<>(request.mediaIds());
        List<StoryMedia> nextMedia = resolveMedia(requestedMedia, id);
        story.update(title, slug, trim(request.summary()), sanitize(request.body()), resolveCategory(request.categoryId()), resolveTags(request.tagIds()), nextMedia);
        previous.stream().filter(media -> !requestedMedia.contains(media.getId())).forEach(media -> mediaStorageService.delete(media.getStorageKey()));
        auditService.record(actorId, "STORY_UPDATED", "STORY", id, Map.of("slug", story.getSlug()));
        return AdminStoryResponse.from(story);
    }

    @Transactional
    public AdminStoryResponse publish(UUID id, UUID actorId) {
        Story story = requireDetails(id); validatePublishable(story); story.publish();
        auditService.record(actorId, "STORY_PUBLISHED", "STORY", id, Map.of("slug", story.getSlug())); return AdminStoryResponse.from(story);
    }

    @Transactional
    public AdminStoryResponse unpublish(UUID id, UUID actorId) {
        Story story = requireDetails(id); story.unpublish();
        auditService.record(actorId, "STORY_UNPUBLISHED", "STORY", id, Map.of("slug", story.getSlug())); return AdminStoryResponse.from(story);
    }

    @Transactional
    public void delete(UUID id, UUID actorId) {
        Story story = requireDetails(id); story.getMedia().forEach(media -> mediaStorageService.delete(media.getStorageKey()));
        storyRepository.delete(story); auditService.record(actorId, "STORY_DELETED", "STORY", id, Map.of("slug", story.getSlug()));
    }

    @Transactional(readOnly = true)
    public FeedResponse publicFeed(String categorySlug, String tagSlug, int requestedLimit, String cursor) {
        int limit = Math.min(Math.max(requestedLimit, 1), 50); Cursor decoded = decodeCursor(cursor);
        String category = blankToNull(categorySlug); String tag = blankToNull(tagSlug);
        List<Story> stories = decoded == null
                ? storyRepository.findPublishedFeedInitial(category, tag, PageRequest.of(0, limit + 1))
                : storyRepository.findPublishedFeedAfter(category, tag, decoded.publishedAt(), decoded.id(), PageRequest.of(0, limit + 1));
        boolean hasMore = stories.size() > limit; if (hasMore) stories = stories.subList(0, limit);
        String next = hasMore && !stories.isEmpty() ? encodeCursor(stories.get(stories.size() - 1)) : null;
        return new FeedResponse(stories.stream().map(StorySummaryResponse::from).toList(), next, hasMore);
    }

    @Transactional(readOnly = true)
    public StoryResponse publicStory(String slug) {
        return StoryResponse.from(storyRepository.findWithDetailsBySlugAndStatus(slug, StoryStatus.PUBLISHED).orElseThrow(() -> notFound("STORY_NOT_FOUND", "Story not found")));
    }

    private Story requireDetails(UUID id) { return storyRepository.findWithDetailsById(id).orElseThrow(() -> notFound("STORY_NOT_FOUND", "Story not found")); }
    private Category resolveCategory(UUID id) { return id == null ? null : categoryRepository.findById(id).orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "Category not found")); }
    private Set<Tag> resolveTags(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        List<Tag> tags = tagRepository.findAllById(ids); if (tags.size() != new HashSet<>(ids).size()) throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_TAG", "One or more tags do not exist"); return new HashSet<>(tags);
    }
    private List<StoryMedia> resolveMedia(Set<UUID> ids, UUID storyId) {
        if (ids.isEmpty()) return List.of(); List<StoryMedia> media = mediaRepository.findAllByIdIn(ids);
        if (media.size() != ids.size() || media.stream().anyMatch(item -> item.getStory() != null && !item.getStory().getId().equals(storyId))) throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_MEDIA", "One or more media items are not available for this story");
        return media;
    }
    private void applyRelations(Story story, StoryRequest request) { story.getTags().addAll(resolveTags(request.tagIds())); List<StoryMedia> media = resolveMedia(request.mediaIds() == null ? Set.of() : new HashSet<>(request.mediaIds()), null); for (int i = 0; i < media.size(); i++) { media.get(i).setStory(story); media.get(i).setSortOrder(i); story.getMedia().add(media.get(i)); } }
    private void validatePublishable(Story story) {
        if (story.getTitle().isBlank() || story.getCategory() == null || textOnly(story.getBody()).isBlank()) throw new RbacException(HttpStatus.BAD_REQUEST, "STORY_INCOMPLETE", "A story must have a title, body, and category before publishing");
    }
    private String sanitize(String body) { return htmlSanitizer.sanitize(body); }
    private String textOnly(String body) { return body.replaceAll("<[^>]+>", " ").replaceAll("&nbsp;", " ").trim(); }
    private String uniqueSlug(String title, UUID existingId) {
        String base = SlugUtils.slugify(title); String slug = base; int suffix = 2;
        while (storyRepository.existsBySlug(slug)) {
            String currentSlug = slug;
            if (existingId != null && storyRepository.findById(existingId).map(story -> story.getSlug().equals(currentSlug)).orElse(false)) break;
            slug = base + "-" + suffix++;
        }
        return slug;
    }
    private StoryStatus parseStatus(String status) { if (status == null || status.isBlank()) return null; try { return StoryStatus.valueOf(status.trim().toUpperCase()); } catch (IllegalArgumentException ex) { throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_STORY_STATUS", "Story status must be DRAFT, PUBLISHED, or UNPUBLISHED"); } }
    private String encodeCursor(Story story) { return Base64.getUrlEncoder().withoutPadding().encodeToString((story.getPublishedAt().toString() + "|" + story.getId()).getBytes(StandardCharsets.UTF_8)); }
    private Cursor decodeCursor(String raw) { if (raw == null || raw.isBlank()) return null; try { String[] parts = new String(Base64.getUrlDecoder().decode(raw), StandardCharsets.UTF_8).split("\\|", 2); return new Cursor(Instant.parse(parts[0]), UUID.fromString(parts[1])); } catch (Exception ex) { throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_CURSOR", "The feed cursor is invalid"); } }
    private record Cursor(Instant publishedAt, UUID id) { }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private RbacException notFound(String code, String message) { return new RbacException(HttpStatus.NOT_FOUND, code, message); }
}
