package com.newsplatform.advertisement.repository;

import com.newsplatform.advertisement.entity.Advertisement;
import com.newsplatform.advertisement.entity.AdvertisementPlacementType;
import com.newsplatform.advertisement.entity.AdvertisementStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {
    @EntityGraph(attributePaths = {"placements", "placements.category", "placements.category.parent", "createdBy"})
    @Query("select distinct a from Advertisement a join a.placements p where " +
            "(:search = '' or lower(a.title) like concat('%', :search, '%') or lower(a.advertiserName) like concat('%', :search, '%') or lower(coalesce(a.description, '')) like concat('%', :search, '%')) " +
            "and (:status is null or a.status = :status) " +
            "and (:placement is null or p.placementType = :placement) " +
            "and (:categoryId is null or p.category.id = :categoryId) order by a.updatedAt desc, a.id desc")
    List<Advertisement> findAdmin(@Param("search") String search, @Param("status") AdvertisementStatus status,
                                  @Param("placement") AdvertisementPlacementType placement, @Param("categoryId") UUID categoryId,
                                  Pageable pageable);

    @EntityGraph(attributePaths = {"placements", "placements.category", "placements.category.parent", "createdBy"})
    Optional<Advertisement> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"placements", "placements.category", "placements.category.parent"})
    @Query("select distinct a from Advertisement a join a.placements p where p.placementType = :placement " +
            "and a.status in ('ACTIVE', 'SCHEDULED') and a.startAt <= :now and a.endAt > :now " +
            "order by a.updatedAt asc, a.id asc")
    List<Advertisement> findEligible(@Param("placement") AdvertisementPlacementType placement, @Param("now") Instant now);
}
