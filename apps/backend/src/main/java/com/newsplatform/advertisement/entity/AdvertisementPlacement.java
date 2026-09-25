package com.newsplatform.advertisement.entity;

import com.newsplatform.category.entity.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "advertisement_placements")
public class AdvertisementPlacement {
    @Id @UuidGenerator @Column(nullable = false, updatable = false) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "advertisement_id", nullable = false) private Advertisement advertisement;
    @Enumerated(EnumType.STRING) @Column(name = "placement_type", nullable = false, length = 30) private AdvertisementPlacementType placementType;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") private Category category;

    protected AdvertisementPlacement() { }

    public AdvertisementPlacement(AdvertisementPlacementType placementType, Category category) {
        this.placementType = placementType;
        this.category = category;
    }

    public UUID getId() { return id; }
    public Advertisement getAdvertisement() { return advertisement; }
    public AdvertisementPlacementType getPlacementType() { return placementType; }
    public Category getCategory() { return category; }
    public void setAdvertisement(Advertisement advertisement) { this.advertisement = advertisement; }
}
