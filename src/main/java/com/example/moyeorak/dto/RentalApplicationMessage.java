package com.example.moyeorak.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalApplicationMessage implements Serializable {
    private RentalApplicationRequest request;
    private String email;
}

