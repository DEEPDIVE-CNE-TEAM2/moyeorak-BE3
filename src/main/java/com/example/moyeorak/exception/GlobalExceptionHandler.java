package com.example.moyeorak.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(HttpServletRequest request, Exception ex) {
        String uri = request.getRequestURI();

        // Swagger 요청은 전역 예외 핸들링 무시
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger")) {
            log.warn("Swagger 관련 요청에서 예외 발생 (무시): {}", ex.getMessage());
            throw new RuntimeException(ex);
        }

        log.error("예외 발생 - URI: {}, Message: {}", uri, ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "서버 내부 오류가 발생했습니다.",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
