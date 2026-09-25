package com.newsplatform.story.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.story.dto.AdminStoryResponse;
import com.newsplatform.story.dto.FeedResponse;
import com.newsplatform.story.dto.StoryRequest;
import com.newsplatform.story.dto.StoryResponse;
import com.newsplatform.story.service.StoryService;
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
@Tag(name = "Stories")
public class StoryController {
    private final StoryService service;
    public StoryController(StoryService service) { this.service = service; }
    @GetMapping("/feed") public FeedResponse feed(@RequestParam(required = false) String category, @RequestParam(required = false) String tag, @RequestParam(defaultValue = "20") int limit, @RequestParam(required = false) String cursor) { return service.publicFeed(category, tag, limit, cursor); }
    @GetMapping("/feed/{slug}") public StoryResponse publicStory(@PathVariable String slug) { return service.publicStory(slug); }
    @GetMapping("/admin/stories") @PreAuthorize("hasAuthority('STORY_VIEW_ADMIN') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public List<AdminStoryResponse> list(@RequestParam(required = false) String search, @RequestParam(required = false) String status, @RequestParam(required = false) UUID categoryId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int limit) { return service.listAdmin(search, status, categoryId, page, limit); }
    @GetMapping("/admin/stories/{id}") @PreAuthorize("hasAuthority('STORY_VIEW_ADMIN') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public AdminStoryResponse get(@PathVariable UUID id) { return service.getAdmin(id); }
    @PostMapping("/admin/stories") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('STORY_CREATE') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public AdminStoryResponse create(@Valid @RequestBody StoryRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return service.create(request, actor.id()); }
    @PutMapping("/admin/stories/{id}") @PreAuthorize("hasAuthority('STORY_EDIT') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public AdminStoryResponse update(@PathVariable UUID id, @Valid @RequestBody StoryRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return service.update(id, request, actor.id()); }
    @DeleteMapping("/admin/stories/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('STORY_DELETE') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { service.delete(id, actor.id()); }
    @PostMapping("/admin/stories/{id}/publish") @PreAuthorize("hasAuthority('STORY_PUBLISH') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public AdminStoryResponse publish(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { return service.publish(id, actor.id()); }
    @PostMapping("/admin/stories/{id}/unpublish") @PreAuthorize("hasAuthority('STORY_PUBLISH') or hasAuthority('SUPER_ADMIN')") @SecurityRequirement(name = "bearerAuth") public AdminStoryResponse unpublish(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { return service.unpublish(id, actor.id()); }
}
