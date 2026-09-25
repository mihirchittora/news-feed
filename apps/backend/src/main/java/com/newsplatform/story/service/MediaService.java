package com.newsplatform.story.service;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.story.dto.MediaResponse;
import com.newsplatform.story.entity.Story;
import com.newsplatform.story.entity.StoryMedia;
import com.newsplatform.story.entity.StoryMediaType;
import com.newsplatform.story.repository.StoryMediaRepository;
import com.newsplatform.story.repository.StoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MediaService {
    private final MediaStorageService storage; private final StoryMediaRepository mediaRepository; private final StoryRepository storyRepository;
    public MediaService(MediaStorageService storage, StoryMediaRepository mediaRepository, StoryRepository storyRepository) { this.storage = storage; this.mediaRepository = mediaRepository; this.storyRepository = storyRepository; }
    @Transactional
    public MediaResponse upload(MultipartFile file, UUID storyId) {
        MediaStorageService.StoredMedia stored = storage.store(file); StoryMedia media = new StoryMedia(type(stored.mimeType()), stored.storageKey(), stored.publicUrl(), null, stored.mimeType(), stored.fileSize());
        if (storyId != null) { Story story = storyRepository.findById(storyId).orElseThrow(() -> new RbacException(HttpStatus.NOT_FOUND, "STORY_NOT_FOUND", "Story not found")); media.setStory(story); media.setSortOrder(story.getMedia().size()); }
        try { return MediaResponse.from(mediaRepository.save(media)); } catch (RuntimeException ex) { storage.delete(stored.storageKey()); throw ex; }
    }
    private StoryMediaType type(String mime) { return mime.startsWith("image/") ? StoryMediaType.IMAGE : StoryMediaType.VIDEO; }
}
