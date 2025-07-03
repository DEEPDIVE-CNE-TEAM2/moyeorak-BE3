package com.example.moyeorak.service;

import com.example.moyeorak.dto.RentalRequest;
import com.example.moyeorak.dto.RentalResponse;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.Rental;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.repository.RentalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RegionRepository regionRepository;

    public RentalResponse createRental(RentalRequest request) {
        log.info("[CREATE] 대관 등록 요청: {}", request);

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

        Rental saved = rentalRepository.save(rental);
        return mapToResponse(saved);
    }

    public List<RentalResponse> getAllRentals() {
        log.info("[GET] 전체 대관 목록 조회");
        return rentalRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RentalResponse getRentalById(Long id) {
        log.info("[GET] 대관 상세 조회 요청 - ID: {}", id);
        Rental rental = rentalRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new IllegalArgumentException("ID " + id + "에 해당하는 대관 공간을 찾을 수 없습니다."));
        return mapToResponse(rental);
    }

    @Transactional
    public RentalResponse updateRental(Long id, RentalRequest request) {
        log.info("[PUT] 대관 전체 수정 요청 - ID: {}", id);

        Rental rental = rentalRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new IllegalArgumentException("ID " + id + "에 해당하는 대관 공간을 찾을 수 없습니다."));

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        rental.setRegion(region);
        rental.setCategory(request.getCategory());
        rental.setLocation(request.getLocation());
        rental.setImageUrl(request.getImageUrl());
        rental.setDescription(request.getDescription());
        rental.setTarget(request.getTarget());
        rental.setUsageStartDate(request.getUsageStartDate());
        rental.setUsageEndDate(request.getUsageEndDate());
        rental.setUsageStartTime(request.getUsageStartTime());
        rental.setUsageEndTime(request.getUsageEndTime());
        rental.setRegistrationStartDate(request.getRegistrationStartDate());
        rental.setRegistrationEndDate(request.getRegistrationEndDate());
        rental.setCancelEndDate(request.getCancelEndDate());
        rental.setFee(request.getFee());
        rental.setCapacity(request.getCapacity());
        rental.setContact(request.getContact());
        rental.setAddress(request.getAddress());

        return mapToResponse(rental);
    }

    @Transactional
    public RentalResponse partialUpdateRental(Long id, Map<String, Object> updates) {
        log.info("[PATCH] 대관 부분 수정 요청 - ID: {}, updates: {}", id, updates);

        Rental rental = rentalRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new IllegalArgumentException("대관 정보를 찾을 수 없습니다."));

        Set<String> allowedFields = Set.of(
                "regionId", "category", "location", "imageUrl", "description", "target",
                "usageStartDate", "usageEndDate", "usageStartTime", "usageEndTime",
                "registrationStartDate", "registrationEndDate", "cancelEndDate",
                "fee", "capacity", "contact", "address"
        );

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            if (!allowedFields.contains(fieldName)) continue;

            try {
                switch (fieldName) {
                    case "regionId" -> {
                        Long regionId = Long.parseLong(value.toString());
                        Region region = regionRepository.findById(regionId)
                                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));
                        rental.setRegion(region);
                    }
                    case "usageStartDate", "usageEndDate",
                         "registrationStartDate", "registrationEndDate", "cancelEndDate" -> {
                        LocalDate date = LocalDate.parse(value.toString());
                        Field field = Rental.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(rental, date);
                    }
                    case "usageStartTime", "usageEndTime" -> {
                        LocalTime time = LocalTime.parse(value.toString());
                        Field field = Rental.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(rental, time);
                    }
                    default -> {
                        Field field = Rental.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        if (field.getType().equals(Integer.class)) {
                            field.set(rental, Integer.parseInt(value.toString()));
                        } else {
                            field.set(rental, value);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("[" + fieldName + "] 필드 업데이트 실패: " + e.getMessage(), e);
            }
        }

        return mapToResponse(rental);
    }

    public void deleteRental(Long id) {
        log.info("[DELETE] 대관 삭제 요청 - ID: {}", id);
        if (!rentalRepository.existsById(Math.toIntExact(id))) {
            throw new IllegalArgumentException("ID " + id + "에 해당하는 대관 공간을 찾을 수 없습니다.");
        }
        rentalRepository.deleteById(Math.toIntExact(id));
    }

    private RentalResponse mapToResponse(Rental rental) {
        return RentalResponse.builder()
                .id(Math.toIntExact(rental.getId()))
                .regionId(rental.getRegion().getId())
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
}
