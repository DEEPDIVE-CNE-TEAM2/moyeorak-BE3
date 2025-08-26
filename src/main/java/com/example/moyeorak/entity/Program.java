package com.example.moyeorak.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "programs",
        indexes = {
                @Index(name = "idx_programs_region", columnList = "region_id"),
                @Index(name = "idx_programs_facility", columnList = "facility_id"),
                @Index(name = "idx_programs_status", columnList = "status"),
                @Index(name = "idx_programs_reg_period", columnList = "registration_start_date,registration_end_date"),
                @Index(name = "idx_programs_use_period", columnList = "usage_start_date,usage_end_date")
        }
)
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(length = 50, nullable = false)
    private String target;

    @Column(name = "instructor_name", length = 100, nullable = false)
    private String instructorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.OPEN;

    @Column(name = "usage_start_date", nullable = false)
    private LocalDate usageStartDate;

    @Column(name = "usage_end_date", nullable = false)
    private LocalDate usageEndDate;

    @Column(name = "class_start_time", nullable = false)
    private LocalTime classStartTime;

    @Column(name = "class_end_time", nullable = false)
    private LocalTime classEndTime;

    @Column(name = "registration_start_date", nullable = false)
    private LocalDate registrationStartDate;

    @Column(name = "registration_end_date", nullable = false)
    private LocalDate registrationEndDate;

    @Column(name = "cancel_end_date", nullable = false)
    private LocalDate cancelEndDate;

    @Column(name = "in_price", nullable = false)
    private Integer inPrice;

    @Column(name = "out_price", nullable = false)
    private Integer outPrice;

    @Column(nullable = false)
    private Integer capacity;

    @Column(length = 50, nullable = false)
    private String contact;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Lob
    private String description;

    @Builder.Default
    @Column(name = "reserved_count", nullable = false)
    private Integer reservedCount = 0;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public enum Status {
        OPEN, CLOSED
    }
}
