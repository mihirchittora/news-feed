package com.newsplatform.tag.service;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.common.util.SlugUtils;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.story.repository.StoryRepository;
import com.newsplatform.tag.dto.TagRequest;
import com.newsplatform.tag.dto.TagResponse;
import com.newsplatform.tag.entity.Tag;
import com.newsplatform.tag.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class TagService {
    private final TagRepository repository; private final StoryRepository storyRepository; private final AuditService auditService;
    public TagService(TagRepository repository, StoryRepository storyRepository, AuditService auditService) { this.repository = repository; this.storyRepository = storyRepository; this.auditService = auditService; }
    @Transactional(readOnly = true)
    public List<TagResponse> list(String query) { return (query == null || query.isBlank() ? repository.findAllByOrderByNameAsc() : repository.findByNameContainingIgnoreCaseOrSlugContainingIgnoreCaseOrderByNameAsc(query.trim(), query.trim())).stream().map(TagResponse::from).toList(); }
    @Transactional(readOnly = true)
    public Tag require(UUID id) { return repository.findById(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "TAG_NOT_FOUND", "Tag not found")); }
    @Transactional
    public TagResponse create(TagRequest request, UUID actorId) {
        String name = request.name().trim(); String normalized = normalize(name);
        if (repository.existsByNormalizedName(normalized)) throw conflict("DUPLICATE_TAG", "A tag with this name already exists");
        String slug = uniqueSlug(request.slug(), name, null); Tag tag = repository.save(new Tag(name, normalized, slug));
        auditService.record(actorId, "TAG_CREATED", "TAG", tag.getId(), Map.of("name", name, "slug", slug)); return TagResponse.from(tag);
    }
    @Transactional
    public TagResponse update(UUID id, TagRequest request, UUID actorId) {
        Tag tag = require(id); String name = request.name().trim(); String normalized = normalize(name);
        if (repository.existsByNormalizedNameAndIdNot(normalized, id)) throw conflict("DUPLICATE_TAG", "A tag with this name already exists");
        String slug = uniqueSlug(request.slug(), name, id); tag.update(name, normalized, slug);
        auditService.record(actorId, "TAG_UPDATED", "TAG", id, Map.of("slug", slug)); return TagResponse.from(tag);
    }
    @Transactional
    public void delete(UUID id, UUID actorId) {
        Tag tag = require(id); if (storyRepository.existsByTags_Id(id)) throw conflict("TAG_IN_USE", "Tags used by stories cannot be deleted");
        repository.delete(tag); auditService.record(actorId, "TAG_DELETED", "TAG", id, Map.of("slug", tag.getSlug()));
    }
    public String normalize(String name) { return name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT); }
    private String uniqueSlug(String requested, String name, UUID existingId) {
        String base = SlugUtils.slugify(requested == null || requested.isBlank() ? name : requested); String slug = base; int suffix = 2;
        while ((existingId == null ? repository.existsBySlug(slug) : repository.existsBySlugAndIdNot(slug, existingId))) slug = base + "-" + suffix++;
        return slug;
    }
    private RbacException conflict(String code, String message) { return error(HttpStatus.CONFLICT, code, message); }
    private RbacException error(HttpStatus status, String code, String message) { return new RbacException(status, code, message); }
}
