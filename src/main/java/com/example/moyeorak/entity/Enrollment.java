package com.example.moyeorak.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "enrollments",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "program_id"})})
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @CreationTimestamp
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private String cancelReason;

    @Column(name = "paid_amount")
    private Integer paidAmount;

    // ✅ 수업 시간 추가
    @Column(name = "class_start_time", nullable = false)
    private LocalTime classStartTime;

    @Column(name = "class_end_time", nullable = false)
    private LocalTime classEndTime;

    public enum Status {
        ENROLLED, CANCELLED
    }
}