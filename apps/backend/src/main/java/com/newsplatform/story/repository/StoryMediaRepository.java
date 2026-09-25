package com.newsplatform.story.repository;

import com.newsplatform.story.entity.StoryMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface StoryMediaRepository extends JpaRepository<StoryMedia, UUID> {
    List<StoryMedia> findAllByIdIn(Collection<UUID> ids);
}
