package com.example.moyeorak.security;

import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.exception.BusinessException;
import com.example.moyeorak.exception.ErrorCode;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAuthHelper {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public User getAdminFromRequest(HttpServletRequest request) {
        String token = jwtProvider.resolveToken(request);
        String email = jwtProvider.getEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (user.getRole() != User.Role.ADMIN) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_REGION_ACCESS);
        }

        return user;
    }

    // 관리자 지역 가져오기
    public Region getAdminRegion(HttpServletRequest request) {
        User admin = getAdminFromRequest(request);
        Region region = admin.getRegion();
        if (region == null) {
            throw new BusinessException(ErrorCode.NO_ADMIN_REGION);
        }
        return region;
    }

    // 관리자 담당 유저인지
    public void validateAdminRegionAccess(User admin, Region targetRegion) {
        if (!targetRegion.getId().equals(admin.getRegion().getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_REGION_ACCESS);
        }
    }
}