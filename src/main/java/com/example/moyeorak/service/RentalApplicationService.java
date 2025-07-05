package com.example.moyeorak.service;

import com.example.moyeorak.dto.RentalApplicationRequest;
import com.example.moyeorak.dto.RentalApplicationResponse;
import com.example.moyeorak.entity.*;
import com.example.moyeorak.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalApplicationService {

    private final RentalApplicationRepository rentalApplicationRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final RegionRepository regionRepository;

    public RentalApplicationResponse createRentalApplication(RentalApplicationRequest request) {
        log.info("[CREATE] 대관 신청 요청: {}", request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        Rental rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new IllegalArgumentException("대관 공간이 존재하지 않습니다."));
        Region region = regionRepository.findById(Long.valueOf(request.getRegionId()))
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        RentalApplication application = RentalApplication.builder()
                .user(user)
                .rental(rental)
                .region(region)
                .requestedDate(request.getRequestedDate())
                .requestedStartTime(request.getRequestedStartTime())
                .requestedEndTime(request.getRequestedEndTime())
                .note(request.getNote())
                .status(RentalApplicationStatus.PENDING)
                .build();

        return mapToResponse(rentalApplicationRepository.save(application));
    }

    public RentalApplicationResponse cancelRentalApplication(Long applicationId, Long userId) {
        RentalApplication application = findUserOwnedApplication(applicationId, userId);
        application.setStatus(RentalApplicationStatus.CANCELLED);
        return mapToResponse(rentalApplicationRepository.save(application));
    }

    public RentalApplicationResponse updateApplicationStatus(Long applicationId, String status) {
        RentalApplicationStatus newStatus;
        try {
            newStatus = RentalApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 상태입니다: " + status);
        }

        RentalApplication application = rentalApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 존재하지 않습니다."));

        application.setStatus(newStatus);
        return mapToResponse(rentalApplicationRepository.save(application));
    }

    public List<RentalApplicationResponse> getUserApplications(Long userId) {
        return rentalApplicationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public String deleteRentalApplication(Long applicationId, Long userId) {
        RentalApplication application = findUserOwnedApplication(applicationId, userId);
        rentalApplicationRepository.delete(application);
        return "대관 신청이 삭제되었습니다.";
    }

    public List<RentalApplicationResponse> getAllApplications() {
        return rentalApplicationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RentalApplicationResponse> getApplicationsByUser(Long userId) {
        return rentalApplicationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RentalApplicationResponse> getApplicationsByRental(Long rentalId) {
        return rentalApplicationRepository.findByRentalId(rentalId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private RentalApplication findUserOwnedApplication(Long appId, Long userId) {
        RentalApplication application = rentalApplicationRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("대관 신청이 존재하지 않습니다."));
        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인만 처리할 수 있습니다.");
        }
        return application;
    }

    private RentalApplicationResponse mapToResponse(RentalApplication app) {
        return RentalApplicationResponse.builder()
                .id(app.getId())
                .userId(Math.toIntExact(app.getUser().getId()))
                .rentalId(Math.toIntExact(app.getRental().getId()))
                .regionId(Math.toIntExact(app.getRegion().getId()))
                .requestedDate(app.getRequestedDate())
                .requestedStartTime(app.getRequestedStartTime())
                .requestedEndTime(app.getRequestedEndTime())
                .status(app.getStatus().name().toLowerCase())
                .note(app.getNote())
                .createdAt(app.getCreatedAt())
                .build();
    }
}
