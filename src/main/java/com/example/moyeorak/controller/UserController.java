package com.example.moyeorak.controller;

import com.example.moyeorak.dto.UserSignupRequestDto;
import com.example.moyeorak.dto.UserSignupResponseDto;
import com.example.moyeorak.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.moyeorak.dto.UserLoginRequestDto;
import com.example.moyeorak.dto.LoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.dto.UserResponseDto;
import com.example.moyeorak.dto.UserUpdateRequestDto;
import com.example.moyeorak.dto.UserPasswordChangeRequestDto;
import com.example.moyeorak.dto.UserDeleteRequestDto;
import java.util.Map;
import com.example.moyeorak.entity.User;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(@Valid @RequestBody UserSignupRequestDto dto) {
        UserSignupResponseDto response = userService.signup(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDto dto) {
        LoginResponseDto response = userService.login(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("토큰이 없습니다.");
        }

        String jwt = token.substring(7); // "Bearer " 제거
        String email = jwtProvider.getEmail(jwt);

        UserResponseDto responseDto = userService.getMyInfo(email);
        return ResponseEntity.ok(responseDto);
    }
    @PutMapping("/me")
    public ResponseEntity<?> updateMyInfo(
            @RequestBody UserUpdateRequestDto dto,
            HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("토큰이 없습니다.");
        }

        String jwt = token.substring(7).trim();
        String email = jwtProvider.getEmail(jwt);

        UserResponseDto updatedUser = userService.updateUserInfo(email, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody UserPasswordChangeRequestDto dto) {
        String jwt = authHeader.replace("Bearer ", "");
        String email = jwtProvider.getEmail(jwt);

        userService.changePassword(email, dto);
        return ResponseEntity.ok().body(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody UserDeleteRequestDto dto) {
        String jwt = authHeader.replace("Bearer ", "");
        String email = jwtProvider.getEmail(jwt);

        userService.deleteUser(email, dto);
        return ResponseEntity.ok().body(Map.of("message", "회원 탈퇴가 성공적으로 처리되었습니다."));
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam String email) {
        boolean exists = userService.isEmailDuplicate(email);
        return ResponseEntity.ok().body(Map.of("isDuplicate", exists));
    }

    @GetMapping("/check-phone")
    public ResponseEntity<?> checkPhoneDuplicate(@RequestParam String phone) {
        boolean exists = userService.isPhoneDuplicate(phone);
        return ResponseEntity.ok().body(Map.of("isDuplicate", exists));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> tokenMap) {
        String refreshToken = tokenMap.get("refreshToken");

        if (!jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body("Refresh Token이 유효하지 않습니다.");
        }

        String email = jwtProvider.getEmail(refreshToken);

        User user = userService.getUserByEmail(email);

        if (!refreshToken.equals(user.getRefreshToken())) {
            return ResponseEntity.status(401).body("Refresh Token이 일치하지 않습니다.");
        }

        String newAccessToken = jwtProvider.generateToken(email);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

}
