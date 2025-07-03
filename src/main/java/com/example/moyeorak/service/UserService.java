package com.example.moyeorak.service;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserSignupResponseDto signup(UserSignupRequestDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        String phone = dto.getPhone().trim();

        validateDuplicateEmail(email);
        validateDuplicatePhone(phone);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .gender(dto.getGender())
                .phone(phone)
                .role(dto.getRole())
                .address(dto.getAddress())
                .birth(LocalDate.parse(dto.getBirth()))
                .build();

        User savedUser = userRepository.save(user);

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

        String accessToken = jwtProvider.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);

        return new LoginResponseDto("로그인 완료", "Bearer " + accessToken, refreshToken);
    }

    public UserResponseDto getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    public UserResponseDto updateUserInfo(String emailFromToken, UserUpdateRequestDto dto) {
        log.info("[UserService] 사용자 정보 수정 요청: {}", emailFromToken);

        User user = userRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        updateIfChanged(dto.getEmail(), user.getEmail(), newEmail -> {
            validateDuplicateEmail(newEmail);
            user.setEmail(newEmail.trim().toLowerCase());
        });

        updateIfChanged(dto.getPhone(), user.getPhone(), newPhone -> {
            validateDuplicatePhone(newPhone);
            user.setPhone(newPhone.trim());
        });

        updateIfChanged(dto.getAddress(), user.getAddress(), user::setAddress);
        updateIfChanged(dto.getName(), user.getName(), user::setName);

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
        return userRepository.findByEmail(email.trim().toLowerCase()).isPresent();
    }

    public boolean isPhoneDuplicate(String phone) {
        return userRepository.findByPhone(phone.trim()).isPresent();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll(Sort.by("id").descending()).stream()
                .map(UserResponseDto::fromEntity)
                .toList();
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
    }

    private void validateDuplicatePhone(String phone) {
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 전화번호입니다.");
        }
    }

    private void updateIfChanged(String newValue, String currentValue, Consumer<String> updater) {
        if (newValue != null && !newValue.equals(currentValue)) {
            updater.accept(newValue);
        }
    }
}
