package com.example.moyeorak.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "main_images", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"region_id", "display_order"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MainImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void update(String title, String imageUrl, int displayOrder, boolean isActive) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }

    // 순서만 바꾸는 메소드
    public void changeDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    // 표시여부만 바꾸는 메소드
    public void changeActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }

}
