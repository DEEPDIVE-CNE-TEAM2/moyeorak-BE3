package com.example.moyeorak.controller;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    private String extractEmailFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        return jwtProvider.getEmail(token.substring(7).trim());
    }

    // 🔐 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(@Valid @RequestBody UserSignupRequestDto dto) {
        return ResponseEntity.ok(userService.signup(dto));
    }

    // 🔐 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody UserLoginRequestDto dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    // 🔐 비밀번호 검증 (수정 페이지 접근 전)
    @Operation(summary = "비밀번호 확인", description = "회원 정보 수정 전에 비밀번호를 검증합니다.")
    @PostMapping("/verify-password")
    public ResponseEntity<PasswordVerifyResponseDto> verifyPassword(
            HttpServletRequest request,
            @RequestBody @Valid PasswordVerifyRequestDto dto) {

        String email = extractEmailFromRequest(request);
        boolean matched = userService.verifyPassword(email, dto.getPassword());
        return ResponseEntity.ok(new PasswordVerifyResponseDto(matched));
    }

    // 👤 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        return ResponseEntity.ok(userService.getMyInfo(email));
    }

    // ✏️ 내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(
            HttpServletRequest request,
            @Valid @RequestBody UserUpdateRequestDto dto) {
        String email = extractEmailFromRequest(request);
        return ResponseEntity.ok(userService.updateUserInfo(email, dto));
    }

    // 🔑 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody UserPasswordChangeRequestDto dto) {
        String email = extractEmailFromRequest(request);
        userService.changePassword(email, dto);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    // ❌ 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUser(
            HttpServletRequest request,
            @Valid @RequestBody UserDeleteRequestDto dto) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
            );
        }

        String email = extractEmailFromRequest(request);
        userService.deleteUser(email, dto);
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 성공적으로 처리되었습니다."));
    }

    // 📧 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email.trim().toLowerCase());
        return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
    }

    // 📱 휴대폰 번호 중복 확인
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Boolean>> checkPhoneDuplicate(@RequestParam String phone) {
        boolean isDuplicate = userService.isPhoneDuplicate(phone.trim());
        return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
    }

    // ♻️ 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> tokenMap) {
        String refreshToken = tokenMap.get("refreshToken");

        if (!jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("message", "Refresh Token이 유효하지 않습니다."));
        }

        String email = jwtProvider.getEmail(refreshToken);
        User user = userService.getUserByEmail(email);

        if (!refreshToken.equals(user.getRefreshToken())) {
            return ResponseEntity.status(401).body(Map.of("message", "Refresh Token이 일치하지 않습니다."));
        }

        String newAccessToken = jwtProvider.generateToken(email, user.getRole().name());
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    // 👑 전체 사용자 조회 (관리자용)
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
