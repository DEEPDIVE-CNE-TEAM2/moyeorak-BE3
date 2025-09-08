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

    /* ===========================
       회원가입
       =========================== */
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

        if (dto.getRegionId() != null) {
            region = regionRepository.findById(dto.getRegionId())
                    .orElseThrow(() -> new IllegalArgumentException("선택한 지역이 존재하지 않습니다."));
        }

        if (role == User.Role.USER && region == null) {
            throw new IllegalArgumentException("일반 사용자는 지역을 반드시 선택해야 합니다.");
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

    /* ===========================
       로그인 (refreshToken 저장 포함)
       - Region 안전 로딩 & 트랜잭션 내부 처리
       =========================== */
    @Transactional
    public LoginResponseDto login(UserLoginRequestDto dto) {
        User user = userRepository.findByEmailWithRegion(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 트랜잭션 안에서 필요한 값 접근 (이미 region이 로딩됨)
        String role = user.getRole().name();

        String accessToken = jwtProvider.generateToken(user.getEmail(), role);
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponseDto("로그인 완료", "Bearer " + accessToken, refreshToken);
    }

    /* ===========================
       RefreshToken 업데이트
       =========================== */
    @Transactional
    public void updateRefreshToken(String email, String refreshToken) {
        User user = getUserByEmail(email); // WithRegion 사용함
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
    }

    /* ===========================
       내 정보 조회
       - DTO 변환을 트랜잭션 안에서 끝내기
       =========================== */
    @Transactional(Transactional.TxType.SUPPORTS)
    public UserResponseDto getMyInfo(String email) {
        User user = getUserByEmail(email); // 이미 WithRegion
        return UserResponseDto.fromEntity(user);
    }

    /* ===========================
       내 정보 수정
       =========================== */
    @Transactional
    public UserResponseDto updateUserInfo(String email, UserUpdateRequestDto dto) {
        log.info("[UserService] 사용자 정보 수정 요청: {}", email);

        User user = getUserByEmail(email); // WithRegion → region 접근 안전

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

        updateIfChanged(dto.getRegionId(), user.getRegion() != null ? user.getRegion().getId() : null, newRegionId -> {
            Region region = regionRepository.findById(newRegionId)
                    .orElseThrow(() -> new IllegalArgumentException("선택한 지역이 존재하지 않습니다."));
            user.setRegion(region);
        });

        return UserResponseDto.fromEntity(user);
    }

    /* ===========================
       비밀번호 변경
       =========================== */
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

    /* ===========================
       회원 탈퇴
       =========================== */
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

    /* ===========================
       전체 사용자 조회 (관리자)
       - Region 페치 + 트랜잭션 안에서 DTO 변환
       =========================== */
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAllByOrderByIdDesc().stream()
                .map(UserResponseDto::fromEntity)
                .toList();
    }

    /* ===========================
       비밀번호 검증
       =========================== */
    public boolean verifyPassword(String email, String password) {
        User user = getUserByEmail(email);
        return passwordEncoder.matches(password, user.getPassword());
    }

    /* ===========================
       중복 체크
       =========================== */
    public boolean isEmailDuplicate(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase()).isPresent();
    }

    public boolean isPhoneDuplicate(String phone) {
        return userRepository.findByPhone(phone.trim()).isPresent();
    }

    /* ===========================
       사용자 단건 조회 (공용 헬퍼)
       - WithRegion 사용으로 이후 접근 안전
       =========================== */
    public User getUserByEmail(String email) {
        return userRepository.findByEmailWithRegion(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /* ===========================
       유틸
       =========================== */
    private void updateIfChanged(String newValue, String currentValue, Consumer<String> updater) {
        if (newValue != null && !newValue.equals(currentValue)) {
            updater.accept(newValue);
        }
    }

    private void updateIfChanged(Long newValue, Long currentValue, Consumer<Long> updater) {
        if (newValue != null && !newValue.equals(currentValue)) {
            updater.accept(newValue);
        }
    }

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
