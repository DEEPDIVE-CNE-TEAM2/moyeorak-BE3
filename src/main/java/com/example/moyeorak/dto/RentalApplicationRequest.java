package com.example.moyeorak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalApplicationRequest {

    @NotNull
    private Integer rentalId;

    @NotNull
    private LocalDate requestedDate;

    @NotNull
    private LocalTime requestedStartTime;

    @NotNull
    private LocalTime requestedEndTime;

    @NotNull
    @Size(min = 1, max = 500)
    private String note;
}