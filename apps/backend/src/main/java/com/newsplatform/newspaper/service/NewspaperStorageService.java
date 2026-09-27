package com.newsplatform.newspaper.service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.time.LocalDate;

public interface NewspaperStorageService {
    StoredFile storePdf(MultipartFile file, LocalDate editionDate);
    StoredFile storeCover(MultipartFile file, LocalDate editionDate);
    default Path resolve(String storageKey) { throw new UnsupportedOperationException("This storage adapter does not expose local paths"); }
    Resource open(String storageKey);
    void delete(String storageKey);
    record StoredFile(String storageKey, String mimeType, long fileSize) { }
}
