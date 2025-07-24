package com.example.moyeorak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "facilities")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column(length = 100, nullable = false)
    private String location;

    @Column(length = 255, nullable = false)
    private String address;

    @Column(length = 50)
    private String contact;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private Integer capacity;

    @Lob
    private String description;

    @Column
    private Integer area;

    @Column(name = "usage_start_time", nullable = false)
    private LocalTime usageStartTime;

    @Column(name = "usage_end_time", nullable = false)
    private LocalTime usageEndTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
}