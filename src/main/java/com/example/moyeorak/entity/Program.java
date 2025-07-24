package com.example.moyeorak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "programs")
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    // ✅ Rental → Facility 로 변경
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(length = 50, nullable = false)
    private String target;

    @Column(length = 100, nullable = false)
    private String instructorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.OPEN;

    @Column(nullable = false)
    private LocalDate usageStartDate;

    @Column(nullable = false)
    private LocalDate usageEndDate;

    @Column(nullable = false)
    private LocalTime classStartTime;

    @Column(nullable = false)
    private LocalTime classEndTime;

    @Column(nullable = false)
    private LocalDate registrationStartDate;

    @Column(nullable = false)
    private LocalDate registrationEndDate;

    @Column(nullable = false)
    private LocalDate cancelEndDate;

    @Column(nullable = false)
    private Integer inPrice;

    @Column(nullable = false)
    private Integer outPrice;

    @Column(nullable = false)
    private Integer capacity;

    @Column(length = 50, nullable = false)
    private String contact;

    @Column(length = 255)
    private String imageUrl;

    @Lob
    private String description;

    public enum Status {
        OPEN, CLOSED
    }
}
