package com.newsplatform.engagement.repository;

import com.newsplatform.engagement.entity.StoryLike;
import com.newsplatform.engagement.entity.StoryLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface StoryLikeRepository extends JpaRepository<StoryLike, StoryLikeId> {
    @Modifying
    @Query(value = "INSERT INTO story_likes (user_id, story_id, created_at) VALUES (:userId, :storyId, now()) ON CONFLICT (user_id, story_id) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("userId") UUID userId, @Param("storyId") UUID storyId);

    @Modifying
    @Query("delete from StoryLike like where like.id.userId = :userId and like.id.storyId = :storyId")
    int deleteForUserAndStory(@Param("userId") UUID userId, @Param("storyId") UUID storyId);

    @Query("select like.id.storyId as storyId, count(like.id.storyId) as count from StoryLike like where like.id.storyId in :storyIds group by like.id.storyId")
    List<StoryCountProjection> countByStoryIds(@Param("storyIds") Collection<UUID> storyIds);

    @Query("select like.id.storyId from StoryLike like where like.id.userId = :userId and like.id.storyId in :storyIds")
    Set<UUID> findLikedStoryIds(@Param("userId") UUID userId, @Param("storyIds") Collection<UUID> storyIds);

    interface StoryCountProjection {
        UUID getStoryId();
        long getCount();
    }
}
