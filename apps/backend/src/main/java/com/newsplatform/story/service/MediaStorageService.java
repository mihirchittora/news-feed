package com.newsplatform.story.service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

public interface MediaStorageService {
    StoredMedia store(MultipartFile file);
    void delete(String storageKey);
    RetrievedMedia retrieve(String storageKey);
    record StoredMedia(String storageKey, String mimeType, long fileSize, String publicUrl) { }
    record RetrievedMedia(Resource resource, String mimeType) { }
}
