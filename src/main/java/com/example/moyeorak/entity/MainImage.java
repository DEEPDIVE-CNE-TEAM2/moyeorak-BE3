package com.example.moyeorak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Builder(toBuilder = true)
public class MainImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 제목은 선택값 */
    @Builder.Default
    @Column(length = 100)
    private String title = "";

    @NotBlank
    @Column(name = "image_url", length = 1024, nullable = false)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** MAS: Region 엔티티 참조 대신 FK 값만 보관 */
    @NotNull
    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ==== 도메인 메소드 ====

    /** 전체 필드 업데이트 */
    public void update(String title, String imageUrl, Integer displayOrder, Boolean isActive) {
        if (title != null) this.title = title;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (displayOrder != null) this.displayOrder = displayOrder;
        if (isActive != null) this.isActive = isActive;
    }

    /** 순서만 변경 */
    public void changeDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    /** 활성 상태 변경 */
    public void changeActiveStatus(Boolean isActive) {
        this.isActive = isActive;
    }

    // ==== 보정 ====
    @PrePersist
    private void prePersistDefaults() {
        if (this.displayOrder == null) this.displayOrder = 1;
        if (this.isActive == null) this.isActive = true;
        if (this.title == null) this.title = "";
    }
}
