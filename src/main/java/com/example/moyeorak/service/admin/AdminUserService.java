package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.*;
import com.example.moyeorak.entity.Enrollment;
import com.example.moyeorak.entity.Program;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.exception.BusinessException;
import com.example.moyeorak.exception.ErrorCode;
import com.example.moyeorak.repository.EnrollmentRepository;
import com.example.moyeorak.repository.UserRepository;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.security.AdminAuthHelper;
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
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnrollmentRepository enrollmentRepository;
    private final AdminAuthHelper adminAuthHelper;


    // 토큰 기반으로 관리자 식별 + 담당 지역 유저 조회 (키워드 필터링 포함)
    public List<AdminUserListResponseDto> getUsersByRegionAndKeyword(HttpServletRequest request, Long regionId, String keyword) {
        Region targetRegion = (regionId == null)
                ? adminAuthHelper.getAdminRegion(request)
                : regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));

        List<User> users = (keyword == null || keyword.trim().isEmpty())
                ? userRepository.findByRegionAndRole(targetRegion, User.Role.USER)
                : userRepository.findByRegionAndRoleAndNameContainingIgnoreCase(targetRegion, User.Role.USER, keyword.trim());

        return users.stream()
                .map(AdminUserListResponseDto::from)
                .toList();
    }


    // 관리자 지역 기반으로 해당 지역 유저 생성
    @Transactional
    public void createUser(AdminUserCreateRequestDto dto, HttpServletRequest request) {
        Region region = adminAuthHelper.getAdminRegion(request);

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_EMAIL);
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

        userRepository.save(newUser);
    }

    private User.Gender parseGender(String gender) {
        if (gender == null) {
            throw new BusinessException(ErrorCode.NULL_GENDER);
        }
        return switch (gender.trim()) {
            case "남" -> User.Gender.MALE;
            case "여" -> User.Gender.FEMALE;
            default -> throw new BusinessException(ErrorCode.INVALID_GENDER);
        };
    }

    private LocalDate parseBirthDate(String birth) {
        try {
            return LocalDate.parse(birth);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_BIRTH_FORMAT);
        }
    }


    // 회원 상세정보 응답
    public AdminUserDetailResponseDto getUserDetail(Long userId, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        // 응답 DTO 구성
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
    public void updateUserInfo(Long userId, AdminUserUpdateRequestDto dto, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        // 지역 검증
        adminAuthHelper.validateAdminRegionAccess(admin, user.getRegion());

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getRegionId() != null) {
            Region region = regionRepository.findById(dto.getRegionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));
            user.setRegion(region);
        }
    }

    // 비밀번호 변경
    @Transactional
    public void updateUserPassword(Long userId, AdminPasswordUpdateRequestDto dto, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        adminAuthHelper.validateAdminRegionAccess(admin, user.getRegion());

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    // 수강 이력 조회
    public List<AdminUserEnrollmentDto> getUserEnrollments(Long userId, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));


        List<Enrollment> enrollments = enrollmentRepository.findAllWithProgramAndRegionByUserId(userId);

        return enrollments.stream()
                .map(enrollment -> AdminUserEnrollmentDto.builder()
                        .enrollmentId(enrollment.getId())
                        .programTitle(enrollment.getProgram().getTitle())
                        .appliedDate(enrollment.getEnrolledAt().toLocalDate().toString())
                        .regionName(enrollment.getRegion().getName())
                        .status(enrollment.getStatus().name())
                        .canCancel(
                                enrollment.getStatus() == Enrollment.Status.ENROLLED &&
                                        enrollment.getProgram().getStatus() == Program.Status.OPEN
                        )
                        .build())
                .toList();
    }

    // 수강 취소
    @Transactional
    public void cancelEnrollment(Long enrollmentId, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ENROLLMENT));

        if (!enrollment.getRegion().getId().equals(admin.getRegion().getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_REGION_ACCESS);
        }

        if (enrollment.getStatus() != Enrollment.Status.ENROLLED) {
            throw new BusinessException(ErrorCode.INVALID_ENROLLMENT_STATUS);
        }

        if (enrollment.getProgram().getStatus() == Program.Status.CLOSED) {
            throw new BusinessException(ErrorCode.PROGRAM_CLOSED);
        }

        enrollment.setStatus(Enrollment.Status.CANCELLED);
    }
}