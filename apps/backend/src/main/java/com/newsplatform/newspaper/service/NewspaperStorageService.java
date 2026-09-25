package com.newsplatform.newspaper.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDate;

public interface NewspaperStorageService {
    StoredFile storePdf(MultipartFile file, LocalDate editionDate);
    StoredFile storeCover(MultipartFile file, LocalDate editionDate);
    Path resolve(String storageKey);
    void delete(String storageKey);
    record StoredFile(String storageKey, String mimeType, long fileSize) { }
}
