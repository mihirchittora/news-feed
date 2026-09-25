package com.newsplatform.story.service;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {
    StoredMedia store(MultipartFile file);
    void delete(String storageKey);
    record StoredMedia(String storageKey, String mimeType, long fileSize, String publicUrl) { }
}
