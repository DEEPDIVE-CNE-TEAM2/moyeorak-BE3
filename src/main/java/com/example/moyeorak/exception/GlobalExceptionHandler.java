package com.example.moyeorak.exception;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice(basePackages = "com.example.moyeorak.controller")
public class GlobalExceptionHandler {

    // 400: 유효성 검사 실패
    @ExceptionHandler({MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handle400(HttpServletRequest req, Exception ex) {
        log.warn("400 Bad Request: {} {} - {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.of(400, "잘못된 요청입니다.", ex.getMessage()));
    }


    // 404: 잘못된 URI
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handle404(HttpServletRequest req, NoResourceFoundException ex) {
        log.warn("404 Not Found: {} {}", req.getMethod(), req.getRequestURI());
        return ResponseEntity.status(404)
                .body(ErrorResponse.of(404, "요청 경로가 존재하지 않습니다.", null));
    }

    // 405: 지원하지 않는 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handle405(HttpServletRequest req, HttpRequestMethodNotSupportedException ex) {
        log.warn("405 Method Not Allowed: {} {} (supported: {})",
                req.getMethod(), req.getRequestURI(), ex.getSupportedHttpMethods());
        return ResponseEntity.status(405)
                .body(ErrorResponse.of(405, "허용되지 않는 HTTP 메서드입니다.", null));
    }

    // 그 외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(HttpServletRequest request, Exception ex) {
        String uri = request.getRequestURI();

        log.error("500 Internal Server Error - URI: {}, Message: {}", uri, ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "서버 내부 오류가 발생했습니다.",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
