package com.newsplatform.story.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.story.dto.MediaResponse;
import com.newsplatform.story.service.MediaService;
import com.newsplatform.story.service.MediaStorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@Tag(name = "Media")
public class MediaController {
    private final MediaService service; private final MediaStorageService storage;
    public MediaController(MediaService service, MediaStorageService storage) { this.service = service; this.storage = storage; }
    @PostMapping(value = "/api/v1/admin/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('STORY_CREATE') or hasAuthority('STORY_EDIT') or hasAuthority('AD_CREATE') or hasAuthority('AD_EDIT') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public MediaResponse upload(@RequestPart("file") MultipartFile file, @RequestParam(required = false) UUID storyId, @AuthenticationPrincipal AuthenticatedUser actor) { return service.upload(file, storyId); }
    @GetMapping("/api/v1/media/{*storageKey}")
    public ResponseEntity<Resource> get(@PathVariable String storageKey) {
        MediaStorageService.RetrievedMedia media = storage.retrieve(storageKey);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(media.mimeType())).body(media.resource());
    }
}
