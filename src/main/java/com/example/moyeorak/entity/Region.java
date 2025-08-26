// content-service/src/main/java/com/example/content/entity/Region.java
package com.example.moyeorak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "regions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_regions_name", columnNames = {"name"})
        },
        indexes = {
                @Index(name = "idx_regions_name", columnList = "name"),
                @Index(name = "idx_regions_manager_id", columnList = "manager_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Region {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    // 필요 시 "시" 허용: "^([가-힣\\s]+시\\s)?[가-힣\\s]+구$"
    @Pattern(regexp = "^[가-힣\\s]+구$", message = "지역명은 'oo구' 또는 'oo시 oo구' 형식이어야 합니다.")
    @Column(length = 100, nullable = false)
    private String name;

    // ✅ 외부 서비스(User) 참조 없이 값만 저장
    @Column(name = "manager_id")
    private Long managerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
