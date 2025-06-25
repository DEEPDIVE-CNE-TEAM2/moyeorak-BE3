package com.example.moyeorak.controller;

import com.example.moyeorak.dto.UserSignupRequestDto;
import com.example.moyeorak.dto.UserSignupResponseDto;
import com.example.moyeorak.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.moyeorak.dto.UserLoginRequestDto;
import com.example.moyeorak.entity.User;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(@Valid @RequestBody UserSignupRequestDto dto) {
        UserSignupResponseDto response = userService.signup(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDto dto) {
        User user = userService.login(dto);
        return ResponseEntity.ok("로그인 성공: " + user.getName());
    }
}
