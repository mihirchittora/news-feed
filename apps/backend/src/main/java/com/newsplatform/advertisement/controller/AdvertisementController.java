package com.newsplatform.advertisement.controller;

import com.newsplatform.advertisement.dto.AdminAdvertisementListResponse;
import com.newsplatform.advertisement.dto.AdminAdvertisementResponse;
import com.newsplatform.advertisement.dto.AdvertisementRequest;
import com.newsplatform.advertisement.dto.PublicAdvertisementResponse;
import com.newsplatform.advertisement.service.AdvertisementService;
import com.newsplatform.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Advertisements")
public class AdvertisementController {
    private final AdvertisementService service;

    public AdvertisementController(AdvertisementService service) { this.service = service; }

    @GetMapping("/ads")
    public List<PublicAdvertisementResponse> publicAds(@RequestParam String placement, @RequestParam(required = false) String category) {
        return service.publicAds(placement, category);
    }

    @GetMapping("/admin/ads")
    @PreAuthorize("hasAuthority('AD_VIEW_ADMIN') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminAdvertisementListResponse list(@RequestParam(required = false) String search, @RequestParam(required = false) String status,
                                               @RequestParam(required = false) String placement, @RequestParam(required = false) UUID categoryId,
                                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int limit) {
        return service.listAdmin(search, status, placement, categoryId, page, limit);
    }

    @GetMapping("/admin/ads/{id}")
    @PreAuthorize("hasAuthority('AD_VIEW_ADMIN') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminAdvertisementResponse get(@PathVariable UUID id) { return service.getAdmin(id); }

    @PostMapping("/admin/ads")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('AD_CREATE') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminAdvertisementResponse create(@Valid @org.springframework.web.bind.annotation.RequestBody AdvertisementRequest request,
                                             @AuthenticationPrincipal AuthenticatedUser actor) { return service.create(request, actor.id()); }

    @PutMapping("/admin/ads/{id}")
    @PreAuthorize("hasAuthority('AD_EDIT') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminAdvertisementResponse update(@PathVariable UUID id, @Valid @org.springframework.web.bind.annotation.RequestBody AdvertisementRequest request,
                                             @AuthenticationPrincipal AuthenticatedUser actor) { return service.update(id, request, actor.id()); }

    @PostMapping("/admin/ads/{id}/publish")
    @PreAuthorize("hasAuthority('AD_PUBLISH') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminAdvertisementResponse publish(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { return service.publish(id, actor.id()); }

    @PostMapping("/admin/ads/{id}/pause")
    @PreAuthorize("hasAuthority('AD_PAUSE') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminAdvertisementResponse pause(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { return service.pause(id, actor.id()); }

    @PostMapping("/admin/ads/{id}/resume")
    @PreAuthorize("hasAuthority('AD_PAUSE') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminAdvertisementResponse resume(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { return service.resume(id, actor.id()); }

    @DeleteMapping("/admin/ads/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('AD_DELETE') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { service.delete(id, actor.id()); }
}
