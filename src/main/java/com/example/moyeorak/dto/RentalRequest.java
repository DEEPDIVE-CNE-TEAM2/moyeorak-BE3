package com.example.moyeorak.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalRequest {

    @NotNull
    private Long regionId;

    @NotBlank
    @Size(max = 50)
    private String category;

    @NotBlank
    @Size(max = 255)
    private String location;

    private String imageUrl;

    private String description;

    @NotBlank
    @Size(max = 50)
    private String target;

    @NotNull
    private LocalDate usageStartDate;

    @NotNull
    private LocalDate usageEndDate;

    @NotNull
    private LocalTime usageStartTime;

    @NotNull
    private LocalTime usageEndTime;

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
    @Size(max = 50)
    private String contact;

    @NotBlank
    @Size(max = 255)
    private String address;
}
