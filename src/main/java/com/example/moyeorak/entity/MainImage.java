package com.example.moyeorak.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "main_images",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_main_images_region_display_order", columnNames = {"region_id", "display_order"})
        },
        indexes = {
                @Index(name = "idx_main_images_region", columnList = "region_id"),
                @Index(name = "idx_main_images_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MainImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(name = "image_url", length = 1024, nullable = false)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false, foreignKey = @ForeignKey(name = "fk_main_image_region"))
    private Region region;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ==== 도메인 메소드 ====

    public void update(String title, String imageUrl, int displayOrder, boolean isActive) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }

    // 순서만 바꾸기
    public void changeDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    // 활성 상태 토글
    public void changeActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }
}