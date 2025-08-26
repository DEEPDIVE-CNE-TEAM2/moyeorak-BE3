package com.example.moyeorak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(
        name = "facilities",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_facilities_region_name", columnNames = {"region_id", "name"})
        },
        indexes = {
                @Index(name = "idx_facilities_region_id", columnList = "region_id"),
                @Index(name = "idx_facilities_name", columnList = "name")
        }
)
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String name;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String location;

    @NotBlank
    @Size(max = 255)
    @Column(length = 255, nullable = false)
    private String address;

    @Size(max = 50)
    @Column(length = 50)
    private String contact;

    @Size(max = 255)
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer capacity;

    @Lob
    private String description;

    @Min(0)
    @Column
    private Integer area;

    @NotNull
    @Column(name = "usage_start_time", nullable = false)
    private LocalTime usageStartTime;

    @NotNull
    @Column(name = "usage_end_time", nullable = false)
    private LocalTime usageEndTime;

    // ✅ MSA: Region 엔티티 직접 참조 제거 → 단순 FK 값만 보관
    @NotNull
    @Column(name = "region_id", nullable = false)
    private Long regionId;

    // 감사 컬럼
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void validateTimes() {
        if (usageStartTime != null && usageEndTime != null &&
                !usageStartTime.isBefore(usageEndTime)) {
            throw new IllegalArgumentException("usageStartTime은 usageEndTime보다 이전이어야 합니다.");
        }
        if (capacity != null && capacity < 1) {
            throw new IllegalArgumentException("capacity는 1 이상이어야 합니다.");
        }
        if (area != null && area < 0) {
            throw new IllegalArgumentException("area는 0 이상이어야 합니다.");
        }
    }
}
