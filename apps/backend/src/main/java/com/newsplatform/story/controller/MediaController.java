package com.newsplatform.story.controller;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.story.dto.MediaResponse;
import com.newsplatform.story.service.MediaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@Tag(name = "Media")
public class MediaController {
    private final MediaService service; private final Path storagePath;
    public MediaController(MediaService service, @Value("${app.media.storage-path:./data/media}") String storagePath) { this.service = service; this.storagePath = Path.of(storagePath).toAbsolutePath().normalize(); }
    @PostMapping(value = "/api/v1/admin/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('STORY_CREATE') or hasAuthority('STORY_EDIT') or hasAuthority('AD_CREATE') or hasAuthority('AD_EDIT') or hasAuthority('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public MediaResponse upload(@RequestPart("file") MultipartFile file, @RequestParam(required = false) UUID storyId, @AuthenticationPrincipal AuthenticatedUser actor) { return service.upload(file, storyId); }
    @GetMapping("/api/v1/media/{storageKey:.+}")
    public ResponseEntity<Resource> get(@PathVariable String storageKey) {
        if (storageKey.contains("/") || storageKey.contains("\\") || storageKey.contains("..")) throw new RbacException(org.springframework.http.HttpStatus.NOT_FOUND, "MEDIA_NOT_FOUND", "Media not found");
        Path path = storagePath.resolve(storageKey).normalize(); if (!path.getParent().equals(storagePath) || !Files.exists(path)) throw new RbacException(org.springframework.http.HttpStatus.NOT_FOUND, "MEDIA_NOT_FOUND", "Media not found");
        try { String type = Files.probeContentType(path); return ResponseEntity.ok().contentType(type == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(type)).body(new FileSystemResource(path)); }
        catch (Exception ex) { throw new RbacException(org.springframework.http.HttpStatus.NOT_FOUND, "MEDIA_NOT_FOUND", "Media not found"); }
    }
}
