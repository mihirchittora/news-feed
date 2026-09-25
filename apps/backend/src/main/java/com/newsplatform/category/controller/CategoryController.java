package com.newsplatform.category.controller;

import com.newsplatform.category.dto.CategoryRequest;
import com.newsplatform.category.dto.CategoryResponse;
import com.newsplatform.category.dto.PublicCategoryResponse;
import com.newsplatform.category.service.CategoryService;
import com.newsplatform.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Categories")
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service) { this.service = service; }
    @GetMapping("/categories") public List<PublicCategoryResponse> publicCategories() { return service.listPublic(); }
    @GetMapping("/categories/{slug}") public PublicCategoryResponse publicCategory(@PathVariable String slug) { return service.publicBySlug(slug); }
    @GetMapping("/admin/categories") @PreAuthorize("hasAuthority('CATEGORY_MANAGE') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public List<CategoryResponse> list() { return service.listAdmin(); }
    @GetMapping("/admin/categories/{id}") @PreAuthorize("hasAuthority('CATEGORY_MANAGE') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public CategoryResponse get(@PathVariable UUID id) { return CategoryResponse.from(service.require(id)); }
    @PostMapping("/admin/categories") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('CATEGORY_MANAGE') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public CategoryResponse create(@Valid @RequestBody CategoryRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return service.create(request, actor.id()); }
    @PutMapping("/admin/categories/{id}") @PreAuthorize("hasAuthority('CATEGORY_MANAGE') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return service.update(id, request, actor.id()); }
    @DeleteMapping("/admin/categories/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('CATEGORY_MANAGE') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { service.delete(id, actor.id()); }
}
