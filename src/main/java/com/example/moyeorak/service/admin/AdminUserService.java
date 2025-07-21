package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.*;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.UserRepository;
import com.example.moyeorak.repository.RegionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;

    // 토큰 기반으로 관리자 식별 + 담당 지역 유저 조회 (키워드 필터링 포함)
    public List<AdminUserListResponseDto> getUsersByRegionAndKeyword(HttpServletRequest request, Long regionId, String keyword) {
        // 1. 토큰에서 관리자 이메일 꺼내기
        String token = jwtProvider.resolveToken(request);
        String email = jwtProvider.getEmail(token);

        // 2. 관리자 유저 조회
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보가 없습니다."));

        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }

        // 3. 조회할 지역 결정
        Region targetRegion;
        if (regionId == null) {
            targetRegion = admin.getRegion();
            if (targetRegion == null) {
                throw new IllegalStateException("관리자에게 지역 정보가 설정되어 있지 않습니다.");
            }
        } else {
            targetRegion = regionRepository.findById(regionId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다."));
        }

        // 4. 일반 유저만 조회 (관리자 제외) + 키워드 필터링
        List<User> users;
        if (keyword == null || keyword.trim().isEmpty()) {
            users = userRepository.findByRegionAndRole(targetRegion, User.Role.USER);
        } else {
            users = userRepository.findByRegionAndRoleAndNameContainingIgnoreCase(targetRegion, User.Role.USER, keyword.trim());
        }

        // 5. DTO 변환
        return users.stream()
                .map(user -> new AdminUserListResponseDto(
                        user.getId(),
                        user.getName(),
                        user.getGender().name(),
                        user.getEmail(),
                        user.getRegion().getName(),
                        user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                ))
                .toList();
    }


    // 관리자 지역 기반으로 해당 지역 유저 생성
    @Transactional
    public void createUser(AdminUserCreateRequestDto dto, HttpServletRequest request) {
        // 1. 관리자 인증
        String token = jwtProvider.resolveToken(request);
        String email = jwtProvider.getEmail(token);

        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보가 없습니다."));

        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalAccessError("관리자 권한이 없습니다.");
        }

        // 2. 관리자 지역 가져오기
        Region region = admin.getRegion();
        if (region == null) {
            throw new IllegalStateException("관리자 지역 정보가 없습니다.");
        }

        // 3. 유저 생성

        // null 체크 먼저
        if (dto.getEmail() == null) {
            throw new IllegalArgumentException("Email cannot be null.");
        }

        User newUser = User.builder()
                .email(dto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .gender(parseGender(dto.getGender()))
                .phone(dto.getPhone())
                .birth(parseBirthDate(dto.getBirth()))
                .role(User.Role.USER)
                .region(region)
                .build();

        // 4. 저장
        userRepository.save(newUser);
    }

    private User.Gender parseGender(String gender) {
        return switch (gender.trim()) {
            case "남" -> User.Gender.MALE;
            case "여" -> User.Gender.FEMALE;
            default -> throw new IllegalArgumentException("성별은 '남' 또는 '여'여야 합니다.");
        };
    }

    private LocalDate parseBirthDate(String birth) {
        try {
            return LocalDate.parse(birth);  // yyyy-MM-dd 형식 기대
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("생년월일 형식이 잘못되었습니다: " + birth);
        }
    }

    // 회원 상세정보 응답
    public AdminUserDetailResponseDto getUserDetail(Long userId, HttpServletRequest request) {
        // 1. 관리자 인증
        String token = jwtProvider.resolveToken(request);
        String email = jwtProvider.getEmail(token);

        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보가 없습니다."));
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }

        // 2. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 3. 유저가 관리자 담당 지역 유저인지 검증
        if (!user.getRegion().getId().equals(admin.getRegion().getId())) {
            throw new IllegalArgumentException("관리자 담당 지역 유저가 아닙니다.");
        }

        // 4. 응답 DTO 구성
        return AdminUserDetailResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .gender(user.getGender() == User.Gender.MALE ? "남" : "여")
                .createdAt(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .email(user.getEmail())
                .phone(user.getPhone())
                .regionId(user.getRegion().getId())
                .regionName(user.getRegion().getName())
                .build();
    }


    // 회원정보 수정
    @Transactional
    public void updateUserInfo(Long userId, AdminUserUpdateRequestDto dto) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 2. email 수정
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        // 3. phone 수정
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        // 4. region 수정
        if (dto.getRegionId() != null) {
            Region region = regionRepository.findById(dto.getRegionId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다."));
            user.setRegion(region);
        }
    }

    // 비밀번호 변경
    @Transactional
    public void updateUserPassword(Long userId, AdminPasswordUpdateRequestDto dto) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 2. 새 비밀번호 확인
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 3. 비밀번호 업데이트
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

}