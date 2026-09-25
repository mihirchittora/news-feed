package com.newsplatform.tag.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.tag.dto.TagRequest;
import com.newsplatform.tag.dto.TagResponse;
import com.newsplatform.tag.service.TagService;
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
@RequestMapping("/api/v1/admin/tags")
@Tag(name = "Tags")
@SecurityRequirement(name = "bearerAuth")
public class TagController {
    private final TagService service;
    public TagController(TagService service) { this.service = service; }
    @GetMapping @PreAuthorize("hasAuthority('TAG_MANAGE') or hasAuthority('SUPER_ADMIN')") public List<TagResponse> list(@RequestParam(required = false) String query) { return service.list(query); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('TAG_MANAGE') or hasAuthority('SUPER_ADMIN')") public TagResponse create(@Valid @RequestBody TagRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return service.create(request, actor.id()); }
    @PutMapping("/{id}") @PreAuthorize("hasAuthority('TAG_MANAGE') or hasAuthority('SUPER_ADMIN')") public TagResponse update(@PathVariable UUID id, @Valid @RequestBody TagRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return service.update(id, request, actor.id()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('TAG_MANAGE') or hasAuthority('SUPER_ADMIN')") public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { service.delete(id, actor.id()); }
}
