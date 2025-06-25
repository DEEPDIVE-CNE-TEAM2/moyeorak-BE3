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
        String token = userService.login(dto);
        return ResponseEntity.ok(new LoginResponseDto("로그인 완료", "Bearer " + token));
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
}
