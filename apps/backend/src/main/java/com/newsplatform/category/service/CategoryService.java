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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository repository;
    private final StoryRepository storyRepository;
    private final AuditService auditService;

    public CategoryService(CategoryRepository repository, StoryRepository storyRepository, AuditService auditService) { this.repository = repository; this.storyRepository = storyRepository; this.auditService = auditService; }

    @Transactional(readOnly = true)
    public List<PublicCategoryResponse> listPublic() {
        List<Category> categories = visibleCategories();
        Map<UUID, List<Category>> children = categories.stream()
                .filter(category -> category.getParent() != null)
                .collect(Collectors.groupingBy(category -> category.getParent().getId(), LinkedHashMap::new, Collectors.toList()));
        return categories.stream().filter(category -> category.getParent() == null)
                .map(category -> publicResponse(category, children)).toList();
    }

    @Transactional(readOnly = true)
    public PublicCategoryResponse publicBySlug(String slug) {
        Category category = repository.findBySlug(slug)
                .filter(this::isVisible)
                .orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "Category not found"));
        Map<UUID, List<Category>> children = visibleCategories().stream()
                .filter(item -> item.getParent() != null)
                .collect(Collectors.groupingBy(item -> item.getParent().getId(), LinkedHashMap::new, Collectors.toList()));
        return publicResponse(category, children);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAdmin() { return repository.findAllByOrderByDisplayOrderAscNameAsc().stream().map(CategoryResponse::from).toList(); }
    @Transactional(readOnly = true)
    public Category require(UUID id) { return repository.findById(id).orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "Category not found")); }
    @Transactional
    public CategoryResponse create(CategoryRequest request, UUID actorId) {
        String name = request.name().trim();
        Category parent = resolveParent(request.parentId(), null);
        String slug = uniqueSlug(request.slug(), name, null);
        if (repository.existsSiblingName(name, parent == null ? null : parent.getId())) throw conflict("DUPLICATE_CATEGORY_NAME", "A sibling category with this name already exists");
        Category category = repository.save(new Category(name, slug, trim(request.description()), parseStatus(request.status()), request.displayOrder() == null ? 0 : request.displayOrder(), parent));
        auditService.record(actorId, "CATEGORY_CREATED", "CATEGORY", category.getId(), Map.of("name", name, "slug", slug));
        return CategoryResponse.from(category);
    }
    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request, UUID actorId) {
        Category category = require(id); String name = request.name().trim();
        Category parent = resolveParent(request.parentId(), id);
        if (repository.existsSiblingNameAndIdNot(name, parent == null ? null : parent.getId(), id)) throw conflict("DUPLICATE_CATEGORY_NAME", "A sibling category with this name already exists");
        String slug = uniqueSlug(request.slug(), name, id);
        int displayOrder = request.displayOrder() == null ? category.getDisplayOrder() : request.displayOrder();
        boolean reordered = category.getDisplayOrder() != displayOrder;
        if (category.getParent() == null && parent != null && repository.existsByParentId(id)) {
            throw conflict("CATEGORY_DEPTH_EXCEEDED", "Move or remove child categories before nesting this category");
        }
        category.update(name, slug, trim(request.description()), parseStatus(request.status()), displayOrder, parent);
        auditService.record(actorId, "CATEGORY_UPDATED", "CATEGORY", id, Map.of("slug", slug));
        if (reordered) auditService.record(actorId, "CATEGORY_REORDERED", "CATEGORY", id, Map.of("displayOrder", displayOrder));
        return CategoryResponse.from(category);
    }
    @Transactional
    public void delete(UUID id, UUID actorId) {
        Category category = require(id);
        if (repository.existsByParentId(id)) throw conflict("CATEGORY_HAS_CHILDREN", "Categories with child categories cannot be deleted; move or delete them first");
        if (storyRepository.existsByCategoryId(id)) throw conflict("CATEGORY_IN_USE", "Categories with stories cannot be deleted; deactivate it instead");
        repository.delete(category); auditService.record(actorId, "CATEGORY_DELETED", "CATEGORY", id, Map.of("slug", category.getSlug()));
    }
    private String uniqueSlug(String requested, String name, UUID existingId) {
        String base = SlugUtils.slugify(requested == null || requested.isBlank() ? name : requested);
        boolean exists = existingId == null ? repository.existsBySlug(base) : repository.existsBySlugAndIdNot(base, existingId);
        if (exists) throw conflict("DUPLICATE_CATEGORY_SLUG", "A category with this slug already exists");
        return base;
    }

    private Category resolveParent(UUID parentId, UUID categoryId) {
        if (parentId == null) return null;
        if (categoryId != null && parentId.equals(categoryId)) throw conflict("CATEGORY_CYCLE", "A category cannot be its own parent");
        Category parent = repository.findById(parentId).orElseThrow(() -> notFound("CATEGORY_PARENT_NOT_FOUND", "Parent category not found"));
        if (parent.getParent() != null) throw conflict("CATEGORY_DEPTH_EXCEEDED", "Only top-level categories can be parents");
        return parent;
    }

    private List<Category> visibleCategories() {
        return repository.findByStatusOrderByDisplayOrderAscNameAsc(CategoryStatus.ACTIVE).stream().filter(this::isVisible).toList();
    }

    private boolean isVisible(Category category) {
        return category.getStatus() == CategoryStatus.ACTIVE
                && (category.getParent() == null || category.getParent().getStatus() == CategoryStatus.ACTIVE);
    }

    private PublicCategoryResponse publicResponse(Category category, Map<UUID, List<Category>> children) {
        List<PublicCategoryResponse> childResponses = children.getOrDefault(category.getId(), List.of()).stream()
                .map(child -> publicResponse(child, children)).toList();
        return PublicCategoryResponse.from(category, childResponses);
    }
    private CategoryStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return CategoryStatus.ACTIVE;
        try { return CategoryStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY_STATUS", "Category status must be ACTIVE or INACTIVE"); }
    }
    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private RbacException notFound(String code, String message) { return new RbacException(HttpStatus.NOT_FOUND, code, message); }
    private RbacException conflict(String code, String message) { return new RbacException(HttpStatus.CONFLICT, code, message); }
}
