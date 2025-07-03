package com.example.moyeorak.controller;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.service.UserService;
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
        String jwt = token.substring(7).trim();
        return jwtProvider.getEmail(jwt);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(@Valid @RequestBody UserSignupRequestDto dto) {
        UserSignupResponseDto response = userService.signup(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody UserLoginRequestDto dto) {
        LoginResponseDto response = userService.login(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        UserResponseDto responseDto = userService.getMyInfo(email);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(
            @Valid @RequestBody UserUpdateRequestDto dto,
            HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        UserResponseDto updatedUser = userService.updateUserInfo(email, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody UserPasswordChangeRequestDto dto) {
        String email = extractEmailFromRequest(request);
        userService.changePassword(email, dto);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUser(
            HttpServletRequest request,
            @Valid @RequestBody UserDeleteRequestDto dto) {
        String email = extractEmailFromRequest(request);
        userService.deleteUser(email, dto);
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 성공적으로 처리되었습니다."));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email.trim().toLowerCase());
        return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
    }

    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Boolean>> checkPhoneDuplicate(@RequestParam String phone) {
        boolean isDuplicate = userService.isPhoneDuplicate(phone.trim());
        return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
    }

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

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
