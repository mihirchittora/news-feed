package com.newsplatform.category.service;

import com.newsplatform.category.dto.CategoryRequest;
import com.newsplatform.category.dto.CategoryResponse;
import com.newsplatform.category.dto.PublicCategoryResponse;
import com.newsplatform.category.entity.Category;
import com.newsplatform.category.entity.CategoryStatus;
import com.newsplatform.category.repository.CategoryRepository;
import com.newsplatform.common.error.RbacException;
import com.newsplatform.common.util.SlugUtils;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.story.repository.StoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository repository;
    private final StoryRepository storyRepository;
    private final AuditService auditService;

    public CategoryService(CategoryRepository repository, StoryRepository storyRepository, AuditService auditService) { this.repository = repository; this.storyRepository = storyRepository; this.auditService = auditService; }

    @Transactional(readOnly = true)
    public List<PublicCategoryResponse> listPublic() { return repository.findByStatusOrderByDisplayOrderAscNameAsc(CategoryStatus.ACTIVE).stream().map(PublicCategoryResponse::from).toList(); }
    @Transactional(readOnly = true)
    public List<CategoryResponse> listAdmin() { return repository.findAllByOrderByDisplayOrderAscNameAsc().stream().map(CategoryResponse::from).toList(); }
    @Transactional(readOnly = true)
    public Category require(UUID id) { return repository.findById(id).orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "Category not found")); }
    @Transactional
    public CategoryResponse create(CategoryRequest request, UUID actorId) {
        String name = request.name().trim(); String slug = uniqueSlug(request.slug(), name, null);
        if (repository.existsByNameIgnoreCase(name)) throw conflict("DUPLICATE_CATEGORY_NAME", "A category with this name already exists");
        Category category = repository.save(new Category(name, slug, trim(request.description()), parseStatus(request.status()), request.displayOrder() == null ? 0 : request.displayOrder()));
        auditService.record(actorId, "CATEGORY_CREATED", "CATEGORY", category.getId(), Map.of("name", name, "slug", slug));
        return CategoryResponse.from(category);
    }
    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request, UUID actorId) {
        Category category = require(id); String name = request.name().trim();
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) throw conflict("DUPLICATE_CATEGORY_NAME", "A category with this name already exists");
        String slug = uniqueSlug(request.slug(), name, id);
        category.update(name, slug, trim(request.description()), parseStatus(request.status()), request.displayOrder() == null ? category.getDisplayOrder() : request.displayOrder());
        auditService.record(actorId, "CATEGORY_UPDATED", "CATEGORY", id, Map.of("slug", slug));
        return CategoryResponse.from(category);
    }
    @Transactional
    public void delete(UUID id, UUID actorId) {
        Category category = require(id);
        if (storyRepository.existsByCategoryId(id)) throw conflict("CATEGORY_IN_USE", "Categories with stories cannot be deleted; deactivate it instead");
        repository.delete(category); auditService.record(actorId, "CATEGORY_DELETED", "CATEGORY", id, Map.of("slug", category.getSlug()));
    }
    private String uniqueSlug(String requested, String name, UUID existingId) {
        String base = SlugUtils.slugify(requested == null || requested.isBlank() ? name : requested);
        String slug = base; int suffix = 2;
        while ((existingId == null ? repository.existsBySlug(slug) : repository.existsBySlugAndIdNot(slug, existingId))) slug = base + "-" + suffix++;
        return slug;
    }
    private CategoryStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return CategoryStatus.ACTIVE;
        try { return CategoryStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY_STATUS", "Category status must be ACTIVE or INACTIVE"); }
    }
    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private RbacException notFound(String code, String message) { return new RbacException(HttpStatus.NOT_FOUND, code, message); }
    private RbacException conflict(String code, String message) { return new RbacException(HttpStatus.CONFLICT, code, message); }
}
