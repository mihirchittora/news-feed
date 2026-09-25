package com.newsplatform.newspaper.service;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.newspaper.dto.AdminNewspaperResponse;
import com.newsplatform.newspaper.dto.NewspaperRequest;
import com.newsplatform.newspaper.dto.PublicNewspaperListResponse;
import com.newsplatform.newspaper.dto.PublicNewspaperResponse;
import com.newsplatform.newspaper.entity.NewspaperEdition;
import com.newsplatform.newspaper.entity.NewspaperStatus;
import com.newsplatform.newspaper.repository.NewspaperEditionRepository;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NewspaperService {
    private final NewspaperEditionRepository repository;
    private final UserRepository userRepository;
    private final NewspaperStorageService storage;
    private final AuditService auditService;
    private final String publicUrl;

    public NewspaperService(NewspaperEditionRepository repository, UserRepository userRepository,
                            NewspaperStorageService storage, AuditService auditService,
                            @Value("${app.media.public-url:http://localhost:8080}") String publicUrl) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.storage = storage;
        this.auditService = auditService;
        this.publicUrl = publicUrl.replaceAll("/$", "");
    }

    @Transactional(readOnly = true)
    public List<AdminNewspaperResponse> listAdmin() {
        return repository.findAllByOrderByEditionDateDescEditionAsc().stream().map(this::adminResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminNewspaperResponse getAdmin(UUID id) { return adminResponse(require(id)); }

    @Transactional
    public AdminNewspaperResponse create(NewspaperRequest request, UUID actorId) {
        String title = required(request.title());
        String edition = required(request.edition());
        ensureUnique(request.editionDate(), edition, null);
        NewspaperEdition newspaper = repository.save(new NewspaperEdition(title, edition, request.editionDate(), userRepository.getReferenceById(actorId)));
        auditService.record(actorId, "NEWSPAPER_CREATED", "NEWSPAPER", newspaper.getId(), Map.of("edition", edition, "editionDate", request.editionDate().toString()));
        return adminResponse(newspaper);
    }

    @Transactional
    public AdminNewspaperResponse update(UUID id, NewspaperRequest request, UUID actorId) {
        NewspaperEdition newspaper = require(id);
        String title = required(request.title());
        String edition = required(request.edition());
        ensureUnique(request.editionDate(), edition, id);
        newspaper.update(title, edition, request.editionDate());
        auditService.record(actorId, "NEWSPAPER_UPDATED", "NEWSPAPER", id, Map.of("edition", edition, "editionDate", request.editionDate().toString()));
        return adminResponse(newspaper);
    }

    @Transactional
    public AdminNewspaperResponse uploadDocument(UUID id, MultipartFile file, UUID actorId) {
        NewspaperEdition newspaper = require(id);
        NewspaperStorageService.StoredFile stored = storage.storePdf(file, newspaper.getEditionDate());
        String previous = newspaper.getPdfStorageKey();
        newspaper.setPdfStorageKey(stored.storageKey());
        try {
            repository.save(newspaper);
            if (previous != null) storage.delete(previous);
            auditService.record(actorId, "NEWSPAPER_DOCUMENT_REPLACED", "NEWSPAPER", id, Map.of("fileSize", stored.fileSize()));
            return adminResponse(newspaper);
        } catch (RuntimeException exception) {
            storage.delete(stored.storageKey());
            throw exception;
        }
    }

    @Transactional
    public AdminNewspaperResponse uploadCover(UUID id, MultipartFile file, UUID actorId) {
        NewspaperEdition newspaper = require(id);
        NewspaperStorageService.StoredFile stored = storage.storeCover(file, newspaper.getEditionDate());
        String previous = newspaper.getCoverImageStorageKey();
        newspaper.setCoverImageStorageKey(stored.storageKey());
        try {
            repository.save(newspaper);
            if (previous != null) storage.delete(previous);
            auditService.record(actorId, "NEWSPAPER_UPDATED", "NEWSPAPER", id, Map.of("asset", "cover"));
            return adminResponse(newspaper);
        } catch (RuntimeException exception) {
            storage.delete(stored.storageKey());
            throw exception;
        }
    }

    @Transactional
    public AdminNewspaperResponse publish(UUID id, UUID actorId) {
        NewspaperEdition newspaper = require(id);
        if (newspaper.getTitle().isBlank() || newspaper.getEdition().isBlank() || newspaper.getEditionDate() == null || newspaper.getPdfStorageKey() == null) {
            throw bad("NEWSPAPER_INCOMPLETE", "A newspaper needs a title, edition, date, and PDF before publishing");
        }
        newspaper.publish();
        auditService.record(actorId, "NEWSPAPER_PUBLISHED", "NEWSPAPER", id, Map.of("edition", newspaper.getEdition()));
        return adminResponse(newspaper);
    }

    @Transactional
    public AdminNewspaperResponse unpublish(UUID id, UUID actorId) {
        NewspaperEdition newspaper = require(id);
        newspaper.unpublish();
        auditService.record(actorId, "NEWSPAPER_UNPUBLISHED", "NEWSPAPER", id, Map.of("edition", newspaper.getEdition()));
        return adminResponse(newspaper);
    }

    @Transactional
    public void delete(UUID id, UUID actorId) {
        NewspaperEdition newspaper = require(id);
        repository.delete(newspaper);
        storage.delete(newspaper.getPdfStorageKey());
        storage.delete(newspaper.getCoverImageStorageKey());
        auditService.record(actorId, "NEWSPAPER_DELETED", "NEWSPAPER", id, Map.of("edition", newspaper.getEdition()));
    }

    @Transactional(readOnly = true)
    public PublicNewspaperListResponse listPublic() {
        return new PublicNewspaperListResponse(repository.findByStatusOrderByEditionDateDescEditionAsc(NewspaperStatus.PUBLISHED).stream().map(this::publicResponse).toList());
    }

    @Transactional(readOnly = true)
    public PublicNewspaperResponse getPublic(UUID id) { return publicResponse(requirePublished(id)); }

    @Transactional(readOnly = true)
    public Resource document(UUID id) {
        NewspaperEdition newspaper = requirePublished(id);
        if (newspaper.getPdfStorageKey() == null) throw notFound("NEWSPAPER_DOCUMENT_NOT_FOUND", "Newspaper document not found");
        return new FileSystemResource(storage.resolve(newspaper.getPdfStorageKey()));
    }

    @Transactional(readOnly = true)
    public Resource cover(UUID id) {
        NewspaperEdition newspaper = requirePublished(id);
        if (newspaper.getCoverImageStorageKey() == null) throw notFound("NEWSPAPER_COVER_NOT_FOUND", "Newspaper cover not found");
        return new FileSystemResource(storage.resolve(newspaper.getCoverImageStorageKey()));
    }

    @Transactional(readOnly = true)
    public String coverContentType(UUID id) {
        NewspaperEdition newspaper = requirePublished(id);
        String key = newspaper.getCoverImageStorageKey();
        if (key == null) throw notFound("NEWSPAPER_COVER_NOT_FOUND", "Newspaper cover not found");
        return key.endsWith(".png") ? "image/png" : key.endsWith(".webp") ? "image/webp" : "image/jpeg";
    }

    private NewspaperEdition require(UUID id) { return repository.findById(id).orElseThrow(() -> notFound("NEWSPAPER_NOT_FOUND", "Newspaper edition not found")); }
    private NewspaperEdition requirePublished(UUID id) {
        NewspaperEdition newspaper = require(id);
        if (newspaper.getStatus() != NewspaperStatus.PUBLISHED) throw new RbacException(HttpStatus.FORBIDDEN, "NEWSPAPER_NOT_AVAILABLE", "This newspaper edition is not available");
        return newspaper;
    }
    private void ensureUnique(LocalDate date, String edition, UUID existingId) {
        boolean exists = existingId == null ? repository.existsByEditionDateAndEditionIgnoreCase(date, edition) : repository.existsByEditionDateAndEditionIgnoreCaseAndIdNot(date, edition, existingId);
        if (exists) throw conflict("DUPLICATE_NEWSPAPER_EDITION", "An edition for this date and edition already exists");
    }
    private AdminNewspaperResponse adminResponse(NewspaperEdition newspaper) { return AdminNewspaperResponse.from(newspaper, publicUrl); }
    private PublicNewspaperResponse publicResponse(NewspaperEdition newspaper) { return PublicNewspaperResponse.from(newspaper, publicUrl); }
    private String required(String value) { return value == null || value.isBlank() ? "" : value.trim(); }
    private RbacException bad(String code, String message) { return new RbacException(HttpStatus.BAD_REQUEST, code, message); }
    private RbacException conflict(String code, String message) { return new RbacException(HttpStatus.CONFLICT, code, message); }
    private RbacException notFound(String code, String message) { return new RbacException(HttpStatus.NOT_FOUND, code, message); }
}
