package com.example.moyeorak.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegionRequest {
    @NotBlank
    private String name;
    private Long  managerId;

}
