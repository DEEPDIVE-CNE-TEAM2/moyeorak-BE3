package com.example.moyeorak.dto;

import java.io.Serializable;
import lombok.*;

@Getter
@AllArgsConstructor
public class EnrollmentMessage implements Serializable {
    private EnrollmentRequest request;
    private String email;

}