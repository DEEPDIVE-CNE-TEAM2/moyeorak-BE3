package com.example.moyeorak.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private String detail;

    public static ErrorResponse of(int status, String message, String detail) {
        return new ErrorResponse(status, message, detail);
    }
}
