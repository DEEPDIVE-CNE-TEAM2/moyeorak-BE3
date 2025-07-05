package com.example.moyeorak.service;

import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.dto.ProgramRequest;
import com.example.moyeorak.dto.ProgramResponse;
import com.example.moyeorak.entity.Program;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.Rental;
import com.example.moyeorak.repository.ProgramRepository;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramService {

    private final ProgramRepository programRepository;
    private final RegionRepository regionRepository;
    private final RentalRepository rentalRepository;

    @Transactional
    public ProgramResponse createProgram(ProgramRequest dto) {
        log.info("[CREATE] 프로그램 등록 요청: {}", dto);

        validateDuplicateTitle(dto.getTitle());  // 예시: 중복 검사

        Program program = Program.builder()
                .title(dto.getTitle())
                .region(getRegion(dto.getRegionId()))
                .facility(getFacility(dto.getFacilityId()))
                .category(dto.getCategory())
                .target(dto.getTarget())
                .instructorName(dto.getInstructorName())
                .status("closed".equalsIgnoreCase(dto.getStatus()) ? Program.Status.CLOSED : Program.Status.OPEN)
                .usageStartDate(dto.getUsageStartDate())
                .usageEndDate(dto.getUsageEndDate())
                .classStartTime(dto.getClassStartTime())
                .classEndTime(dto.getClassEndTime())
                .registrationStartDate(dto.getRegistrationStartDate())
                .registrationEndDate(dto.getRegistrationEndDate())
                .cancelEndDate(dto.getCancelEndDate())
                .fee(dto.getFee())
                .capacity(dto.getCapacity())
                .contact(dto.getContact())
                .imageUrl(dto.getImageUrl())
                .description(dto.getDescription())
                .build();

        return toResponse(programRepository.save(program));
    }

    public List<ProgramResponse> getAllPrograms() {
        log.info("[GET] 전체 프로그램 목록 조회");
        return programRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProgramResponse getProgramById(Long id) {
        log.info("[GET] 프로그램 상세 조회 - ID: {}", id);
        return toResponse(getProgram(id));
    }

    @Transactional
    public ProgramResponse updateProgram(Long id, ProgramRequest dto) {
        log.info("[PUT] 프로그램 전체 수정 - ID: {}", id);

        Program program = getProgram(id);

        program.setTitle(dto.getTitle());
        program.setRegion(getRegion(dto.getRegionId()));
        program.setFacility(getFacility(dto.getFacilityId()));
        program.setCategory(dto.getCategory());
        program.setTarget(dto.getTarget());
        program.setInstructorName(dto.getInstructorName());
        program.setStatus("closed".equalsIgnoreCase(dto.getStatus()) ? Program.Status.CLOSED : Program.Status.OPEN);
        program.setUsageStartDate(dto.getUsageStartDate());
        program.setUsageEndDate(dto.getUsageEndDate());
        program.setClassStartTime(dto.getClassStartTime());
        program.setClassEndTime(dto.getClassEndTime());
        program.setRegistrationStartDate(dto.getRegistrationStartDate());
        program.setRegistrationEndDate(dto.getRegistrationEndDate());
        program.setCancelEndDate(dto.getCancelEndDate());
        program.setFee(dto.getFee());
        program.setCapacity(dto.getCapacity());
        program.setContact(dto.getContact());
        program.setImageUrl(dto.getImageUrl());
        program.setDescription(dto.getDescription());

        return toResponse(program);
    }

    @Transactional
    public ProgramResponse partialUpdateProgram(Long id, Map<String, Object> updates) {
        log.info("[PATCH] 프로그램 부분 수정 - ID: {}", id);

        Program program = getProgram(id);

        Set<String> allowedFields = Set.of(
                "title", "regionId", "facilityId", "category", "target", "instructorName", "status",
                "usageStartDate", "usageEndDate", "classStartTime", "classEndTime",
                "registrationStartDate", "registrationEndDate", "cancelEndDate",
                "fee", "capacity", "contact", "imageUrl", "description"
        );

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            if (!allowedFields.contains(fieldName)) continue;

            try {
                switch (fieldName) {
                    case "regionId" -> program.setRegion(getRegion(Long.parseLong(value.toString())));
                    case "facilityId" -> program.setFacility(getFacility(Long.parseLong(value.toString())));
                    case "status" -> program.setStatus("closed".equalsIgnoreCase(value.toString()) ? Program.Status.CLOSED : Program.Status.OPEN);
                    case "usageStartDate", "usageEndDate", "registrationStartDate", "registrationEndDate", "cancelEndDate" -> {
                        LocalDate date = LocalDate.parse(value.toString());
                        Field field = Program.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(program, date);
                    }
                    case "classStartTime", "classEndTime" -> {
                        LocalTime time = LocalTime.parse(value.toString());
                        Field field = Program.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(program, time);
                    }
                    default -> {
                        Field field = Program.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        if (field.getType().equals(Integer.class)) {
                            field.set(program, Integer.parseInt(value.toString()));
                        } else {
                            field.set(program, value);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("필드 업데이트 실패: " + fieldName + " - " + e.getMessage(), e);
            }
        }

        return toResponse(program);
    }

    @Transactional
    public MessageResponse deleteProgram(Long id) {
        log.info("[DELETE] 프로그램 삭제 - ID: {}", id);
        if (!programRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 프로그램이 존재하지 않습니다.");
        }
        programRepository.deleteById(id);
        return new MessageResponse("프로그램이 삭제되었습니다.");
    }

    // ===== 내부 유틸 =====

    private Program getProgram(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("프로그램(ID: " + id + ")이 존재하지 않습니다."));
    }

    private Region getRegion(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("지역(ID: " + regionId + ")이 존재하지 않습니다."));
    }

    private Rental getFacility(Long facilityId) {
        return rentalRepository.findById(Math.toIntExact(facilityId))
                .orElseThrow(() -> new IllegalArgumentException("시설(ID: " + facilityId + ")이 존재하지 않습니다."));
    }

    private void validateDuplicateTitle(String title) {
        // 예시: title 중복 검사 필요 시 사용
    }

    private ProgramResponse toResponse(Program program) {
        return ProgramResponse.builder()
                .id(program.getId())
                .title(program.getTitle())
                .category(program.getCategory())
                .target(program.getTarget())
                .instructorName(program.getInstructorName())
                .status(program.getStatus().name().toLowerCase())
                .usageStartDate(program.getUsageStartDate())
                .usageEndDate(program.getUsageEndDate())
                .classStartTime(program.getClassStartTime())
                .classEndTime(program.getClassEndTime())
                .registrationStartDate(program.getRegistrationStartDate())
                .registrationEndDate(program.getRegistrationEndDate())
                .cancelEndDate(program.getCancelEndDate())
                .fee(program.getFee())
                .capacity(program.getCapacity())
                .contact(program.getContact())
                .imageUrl(program.getImageUrl())
                .description(program.getDescription())
                .build();
    }
}
