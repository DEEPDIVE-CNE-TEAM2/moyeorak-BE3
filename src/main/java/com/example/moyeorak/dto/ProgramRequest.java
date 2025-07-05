package com.example.moyeorak.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramRequest {

    @NotBlank
    private String title;

    @NotNull
    private Long regionId;

    @NotNull
    private Long facilityId;

    @NotBlank
    private String category;

    @NotBlank
    private String target;

    @NotBlank
    private String instructorName;

    private String status; // "open" or "closed" (optional)

    @NotNull
    private LocalDate usageStartDate;
    @NotNull
    private LocalDate usageEndDate;
    @NotNull
    private LocalTime classStartTime;
    @NotNull
    private LocalTime classEndTime;

    @NotNull
    private LocalDate registrationStartDate;
    @NotNull
    private LocalDate registrationEndDate;
    @NotNull
    private LocalDate cancelEndDate;

    @NotNull
    @Min(0)
    private Integer fee;

    @NotNull
    @Min(1)
    private Integer capacity;

    @NotBlank
    private String contact;

    private String imageUrl;
    private String description;
}

