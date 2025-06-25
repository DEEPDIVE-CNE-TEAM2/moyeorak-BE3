package com.example.moyeorak.service;

import com.example.moyeorak.dto.UserSignupRequestDto;
import com.example.moyeorak.dto.UserSignupResponseDto;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.moyeorak.dto.UserLoginRequestDto;
import com.example.moyeorak.dto.UserResponseDto;
import com.example.moyeorak.dto.UserUpdateRequestDto;
import org.springframework.transaction.annotation.Transactional;
import com.example.moyeorak.dto.UserPasswordChangeRequestDto;
import com.example.moyeorak.dto.UserDeleteRequestDto;
import com.example.moyeorak.dto.LoginResponseDto;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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

    public LoginResponseDto login(UserLoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.generateToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        // refreshToken 저장
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponseDto("로그인 완료", "Bearer " + accessToken, refreshToken);
    }

    public UserResponseDto getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    public UserResponseDto updateUserInfo(String emailFromToken, UserUpdateRequestDto dto) {
        System.out.println("🔧 [UserService] 사용자 정보 수정 요청 받음");
        User user = userRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getPhone() != null && !dto.getPhone().equals(user.getPhone())) {
            if (userRepository.findByPhone(dto.getPhone()).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
            }
            user.setPhone(dto.getPhone());
        }

        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }

        userRepository.save(user);
        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    public void changePassword(String email, UserPasswordChangeRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String email, UserDeleteRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }

    public boolean isEmailDuplicate(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean isPhoneDuplicate(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

}
