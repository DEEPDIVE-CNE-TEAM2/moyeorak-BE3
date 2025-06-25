package com.example.moyeorak.service;

import com.example.moyeorak.dto.UserSignupRequestDto;
import com.example.moyeorak.dto.UserSignupResponseDto;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.moyeorak.dto.UserLoginRequestDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSignupResponseDto signup(UserSignupRequestDto dto) {
        // 중복 확인
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 전화번호입니다.");
        }

        // 비밀번호 암호화 및 사용자 저장
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .role(dto.getRole())
                .address(dto.getAddress())
                .build();

        User savedUser = userRepository.save(user);

        // 응답 DTO 생성 후 반환
        return UserSignupResponseDto.builder()
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .phone(savedUser.getPhone())
                .address(savedUser.getAddress())
                .build();
    }

    public User login(UserLoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user; // JWT 사용 시 여기서 토큰 발급
    }
}
