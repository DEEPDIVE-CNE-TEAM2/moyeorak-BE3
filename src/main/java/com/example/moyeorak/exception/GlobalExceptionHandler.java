package com.example.moyeorak.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice(basePackages = "com.example.moyeorak.controller")
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode code = ex.getErrorCode();

        log.warn("[{}] {}", code.name(), code.getMessage());

        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.of(code));
    }
}
