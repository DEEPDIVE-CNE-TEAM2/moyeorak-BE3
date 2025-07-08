package com.example.moyeorak.service;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.Rental;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.repository.RentalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RegionRepository regionRepository;

    /**
     * ✅ 대관 등록
     */
    public RentalCreateResponse createRental(RentalRequest request) {
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        Rental rental = Rental.builder()
                .region(region)
                .category(request.getCategory())
                .location(request.getLocation())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .target(request.getTarget())
                .usageStartDate(request.getUsageStartDate())
                .usageEndDate(request.getUsageEndDate())
                .usageStartTime(request.getUsageStartTime())
                .usageEndTime(request.getUsageEndTime())
                .registrationStartDate(request.getRegistrationStartDate())
                .registrationEndDate(request.getRegistrationEndDate())
                .cancelEndDate(request.getCancelEndDate())
                .fee(request.getFee())
                .capacity(request.getCapacity())
                .contact(request.getContact())
                .address(request.getAddress())
                .build();

        return mapToCreateResponse(rentalRepository.save(rental));
    }

    /**
     * ✅ 전체 대관 목록 조회
     */
    public List<RentalListResponse> getAllRentals() {
        return rentalRepository.findAll().stream()
                .map(this::mapToListResponse)
                .toList();
    }

    /**
     * ✅ 대관 상세 조회
     */
    public RentalDetailResponse getRentalById(Long id) {
        Rental rental = rentalRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 대관 정보가 없습니다."));
        return mapToDetailResponse(rental);
    }

    /**
     * ✅ 대관 부분 수정
     */
    @Transactional
    public RentalCreateResponse partialUpdateRental(Long id, Map<String, Object> updates) {
        Rental rental = rentalRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new IllegalArgumentException("대관 정보를 찾을 수 없습니다."));

        Set<String> allowedFields = Set.of(
                "regionId", "category", "location", "imageUrl", "description", "target",
                "usageStartDate", "usageEndDate", "usageStartTime", "usageEndTime",
                "registrationStartDate", "registrationEndDate", "cancelEndDate",
                "fee", "capacity", "contact", "address"
        );

        updates.forEach((fieldName, value) -> {
            if (!allowedFields.contains(fieldName)) return;

            try {
                switch (fieldName) {
                    case "regionId" -> {
                        Region region = regionRepository.findById(Long.parseLong(value.toString()))
                                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));
                        rental.setRegion(region);
                    }
                    case "usageStartDate", "usageEndDate",
                         "registrationStartDate", "registrationEndDate", "cancelEndDate" -> {
                        Field field = Rental.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(rental, LocalDate.parse(value.toString()));
                    }
                    case "usageStartTime", "usageEndTime" -> {
                        Field field = Rental.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(rental, LocalTime.parse(value.toString()));
                    }
                    default -> {
                        Field field = Rental.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object parsed = field.getType().equals(Integer.class)
                                ? Integer.parseInt(value.toString())
                                : value;
                        field.set(rental, parsed);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("[" + fieldName + "] 필드 업데이트 실패: " + e.getMessage(), e);
            }
        });

        return mapToCreateResponse(rental);
    }

    /**
     * ✅ 대관 삭제
     */
    public void deleteRental(Long id) {
        if (!rentalRepository.existsById(Math.toIntExact(id))) {
            throw new IllegalArgumentException("해당 대관 정보가 없습니다.");
        }
        rentalRepository.deleteById(Math.toIntExact(id));
    }

    // ===================== 응답 변환 =====================

    private RentalCreateResponse mapToCreateResponse(Rental rental) {
        User manager = rental.getRegion().getManager();

        return RentalCreateResponse.builder()
                .id(Math.toIntExact(rental.getId()))
                .regionName(rental.getRegion().getName())
                .managerEmail(manager != null ? manager.getEmail() : null)
                .category(rental.getCategory())
                .location(rental.getLocation())
                .imageUrl(rental.getImageUrl())
                .description(rental.getDescription())
                .target(rental.getTarget())
                .usageStartDate(rental.getUsageStartDate())
                .usageEndDate(rental.getUsageEndDate())
                .usageStartTime(rental.getUsageStartTime())
                .usageEndTime(rental.getUsageEndTime())
                .registrationStartDate(rental.getRegistrationStartDate())
                .registrationEndDate(rental.getRegistrationEndDate())
                .cancelEndDate(rental.getCancelEndDate())
                .fee(rental.getFee())
                .capacity(rental.getCapacity())
                .contact(rental.getContact())
                .address(rental.getAddress())
                .build();
    }

    private RentalListResponse mapToListResponse(Rental rental) {
        return RentalListResponse.builder()
                .id(Math.toIntExact(rental.getId()))
                .location(rental.getLocation())
                .imageUrl(rental.getImageUrl())
                .address(rental.getAddress())
                .usageTime(formatTimeRange(rental.getUsageStartTime(), rental.getUsageEndTime()))
                .capacity(rental.getCapacity())
                .build();
    }

    private RentalDetailResponse mapToDetailResponse(Rental rental) {
        return RentalDetailResponse.builder()
                .id(Math.toIntExact(rental.getId()))
                .category(rental.getCategory())
                .location(rental.getLocation())
                .address(rental.getAddress())
                .usageTime(formatTimeRange(rental.getUsageStartTime(), rental.getUsageEndTime()))
                .registrationPeriod(formatDateRange(rental.getRegistrationStartDate(), rental.getRegistrationEndDate()))
                .cancelEndDate(rental.getCancelEndDate().toString())
                .capacity(rental.getCapacity())
                .contact(rental.getContact())
                .build();
    }

    private String formatTimeRange(LocalTime start, LocalTime end) {
        return start + " ~ " + end;
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        return start + " ~ " + end;
    }
}
