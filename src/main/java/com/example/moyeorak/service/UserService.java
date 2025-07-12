package com.example.moyeorak.service;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // ✅ 회원가입
    public UserSignupResponseDto signup(UserSignupRequestDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        String phone = dto.getPhone().trim();

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        validateDuplicateEmail(email);
        validateDuplicatePhone(phone);

        User.Role role = dto.getRoleOrDefault();

        Region region = null;
        if (role == User.Role.USER) {
            if (dto.getRegionId() == null) {
                throw new IllegalArgumentException("일반 사용자는 지역을 반드시 선택해야 합니다.");
            }

            region = regionRepository.findById(dto.getRegionId())
                    .orElseThrow(() -> new IllegalArgumentException("선택한 지역이 존재하지 않습니다."));
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .gender(dto.getGender())
                .phone(phone)
                .birth(dto.getBirth())
                .role(role)
                .region(region)
                .build();

        User savedUser = userRepository.save(user);

        return UserSignupResponseDto.builder()
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .phone(savedUser.getPhone())
                .regionName(region != null ? region.getName() : null)
                .build();
    }

    // ✅ 로그인
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

    // ✅ 내 정보 조회
    public UserResponseDto getMyInfo(String email) {
        User user = getUserByEmail(email);
        return UserResponseDto.fromEntity(user);
    }

    // ✅ 내 정보 수정
    @Transactional
    public UserResponseDto updateUserInfo(String emailFromToken, UserUpdateRequestDto dto) {
        log.info("[UserService] 사용자 정보 수정 요청: {}", emailFromToken);

        User user = getUserByEmail(emailFromToken);

        updateIfChanged(dto.getEmail(), user.getEmail(), newEmail -> {
            validateDuplicateEmail(newEmail);
            user.setEmail(newEmail.trim().toLowerCase());
        });

        updateIfChanged(dto.getPhone(), user.getPhone(), newPhone -> {
            validateDuplicatePhone(newPhone);
            user.setPhone(newPhone.trim());
        });

        updateIfChanged(dto.getName(), user.getName(), user::setName);

        if (dto.getGender() != null && !dto.getGender().equals(user.getGender())) {
            user.setGender(dto.getGender());
        }

        return UserResponseDto.fromEntity(user);
    }

    // ✅ 비밀번호 변경
    @Transactional
    public void changePassword(String email, UserPasswordChangeRequestDto dto) {
        User user = getUserByEmail(email);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인 값이 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    // ✅ 회원 탈퇴
    @Transactional
    public void deleteUser(String email, UserDeleteRequestDto dto) {
        User user = getUserByEmail(email);

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }

    // ✅ 전체 사용자 조회 (관리자용)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll(Sort.by("id").descending()).stream()
                .map(UserResponseDto::fromEntity)
                .toList();
    }

    // ✅ 비밀번호 검증 (verify-password용)
    public boolean verifyPassword(String email, String password) {
        User user = getUserByEmail(email);
        return passwordEncoder.matches(password, user.getPassword());
    }

    // ✅ 중복 확인
    public boolean isEmailDuplicate(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase()).isPresent();
    }

    public boolean isPhoneDuplicate(String phone) {
        return userRepository.findByPhone(phone.trim()).isPresent();
    }

    // ✅ 사용자 단건 조회
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // ✅ 수정 시 필드 변경 감지
    private void updateIfChanged(String newValue, String currentValue, Consumer<String> updater) {
        if (newValue != null && !newValue.equals(currentValue)) {
            updater.accept(newValue);
        }
    }

    // ✅ 중복 체크 유틸
    private void validateDuplicateEmail(String email) {
        if (isEmailDuplicate(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
    }

    private void validateDuplicatePhone(String phone) {
        if (isPhoneDuplicate(phone)) {
            throw new IllegalArgumentException("이미 존재하는 전화번호입니다.");
        }
    }
}
