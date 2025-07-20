package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminUserListResponseDto;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public List<AdminUserListResponseDto> getUsersByRegion(Long adminId) {
        // 1. 관리자 유저 정보 가져오기
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보가 없습니다."));

        // 2. 관리자 지역 정보 가져오기
        Region region = admin.getRegion();
        if (region == null) {
            throw new IllegalStateException("관리자에게 지역 정보가 설정되어 있지 않습니다.");
        }

        // 3. 해당 지역 유저 전체 조회
        List<User> users = userRepository.findByRegion(region);

        // 4. DTO로 변환해서 반환
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
}