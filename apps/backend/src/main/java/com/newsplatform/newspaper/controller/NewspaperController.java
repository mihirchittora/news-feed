package com.newsplatform.newspaper.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.newspaper.dto.AdminNewspaperResponse;
import com.newsplatform.newspaper.dto.NewspaperRequest;
import com.newsplatform.newspaper.dto.PublicNewspaperListResponse;
import com.newsplatform.newspaper.dto.PublicNewspaperResponse;
import com.newsplatform.newspaper.service.NewspaperService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Newspapers")
public class NewspaperController {
    private final NewspaperService service;

    public NewspaperController(NewspaperService service) { this.service = service; }

    @GetMapping("/newspapers")
    public PublicNewspaperListResponse listPublic() { return service.listPublic(); }

    @GetMapping("/newspapers/{id}")
    public PublicNewspaperResponse getPublic(@PathVariable UUID id) { return service.getPublic(id); }

    @GetMapping("/newspapers/{id}/cover")
    public ResponseEntity<Resource> cover(@PathVariable UUID id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType(service.coverContentType(id))).body(service.cover(id));
    }

    @GetMapping("/newspapers/{id}/document")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Resource> document(@PathVariable UUID id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"newspaper-" + id + ".pdf\"").body(service.document(id));
    }

    @GetMapping("/admin/newspapers")
    @PreAuthorize("hasAuthority('NEWSPAPER_VIEW_ADMIN') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public List<AdminNewspaperResponse> listAdmin() { return service.listAdmin(); }

    @GetMapping("/admin/newspapers/{id}")
    @PreAuthorize("hasAuthority('NEWSPAPER_VIEW_ADMIN') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminNewspaperResponse getAdmin(@PathVariable UUID id) { return service.getAdmin(id); }

    @PostMapping("/admin/newspapers")
    @PreAuthorize("hasAuthority('NEWSPAPER_UPLOAD') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminNewspaperResponse create(@Valid @RequestBody NewspaperRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return service.create(request, actor.id()); }

    @PutMapping("/admin/newspapers/{id}")
    @PreAuthorize("hasAuthority('NEWSPAPER_EDIT') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminNewspaperResponse update(@PathVariable UUID id, @Valid @RequestBody NewspaperRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return service.update(id, request, actor.id()); }

    @PostMapping(value = "/admin/newspapers/{id}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('NEWSPAPER_UPLOAD') or hasAuthority('NEWSPAPER_EDIT') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminNewspaperResponse uploadDocument(@PathVariable UUID id, @RequestPart("file") MultipartFile file, @AuthenticationPrincipal AuthenticatedUser actor) { return service.uploadDocument(id, file, actor.id()); }

    @PostMapping(value = "/admin/newspapers/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('NEWSPAPER_UPLOAD') or hasAuthority('NEWSPAPER_EDIT') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminNewspaperResponse uploadCover(@PathVariable UUID id, @RequestPart("file") MultipartFile file, @AuthenticationPrincipal AuthenticatedUser actor) { return service.uploadCover(id, file, actor.id()); }

    @PostMapping("/admin/newspapers/{id}/publish")
    @PreAuthorize("hasAuthority('NEWSPAPER_PUBLISH') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminNewspaperResponse publish(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { return service.publish(id, actor.id()); }

    @PostMapping("/admin/newspapers/{id}/unpublish")
    @PreAuthorize("hasAuthority('NEWSPAPER_PUBLISH') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminNewspaperResponse unpublish(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { return service.unpublish(id, actor.id()); }

    @DeleteMapping("/admin/newspapers/{id}")
    @PreAuthorize("hasAuthority('NEWSPAPER_DELETE') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { service.delete(id, actor.id()); }
}
