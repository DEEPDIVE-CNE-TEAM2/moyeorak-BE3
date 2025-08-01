package com.example.moyeorak.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMainImageUpdateRequest {
    private Long id;
    private Integer displayOrder;
    private Boolean isActive;
}