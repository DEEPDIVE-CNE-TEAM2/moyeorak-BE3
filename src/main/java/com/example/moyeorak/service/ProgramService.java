package com.example.moyeorak.service;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.entity.*;
import com.example.moyeorak.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramService {

    private final ProgramRepository programRepository;
    private final RegionRepository regionRepository;
    private final RentalRepository rentalRepository;

    @Transactional
    public ProgramDisplayResponse createProgram(ProgramRequest dto) {
        log.info("[CREATE] 프로그램 등록 요청: {}", dto);

        validateDuplicateTitle(dto.getTitle());

        Program program = buildProgramFromDto(dto);
        return toDisplayResponse(programRepository.save(program));
    }

    public List<ProgramDisplayResponse> getAllPrograms() {
        log.info("[GET] 전체 프로그램 목록 조회");
        return programRepository.findAll().stream()
                .map(this::toDisplayResponse)
                .toList();
    }

    public ProgramDisplayResponse getProgramById(Long id) {
        log.info("[GET] 프로그램 상세 조회 - ID: {}", id);
        return toDisplayResponse(getProgram(id));
    }

    @Transactional
    public ProgramDisplayResponse partialUpdateProgram(Long id, Map<String, Object> updates) {
        log.info("[PATCH] 프로그램 부분 수정 - ID: {}", id);

        Program program = getProgram(id);

        Set<String> allowedFields = Set.of(
                "title", "regionId", "facilityId", "category", "target", "instructorName", "status",
                "usageStartDate", "usageEndDate", "classStartTime", "classEndTime",
                "registrationStartDate", "registrationEndDate", "cancelEndDate",
                "fee", "capacity", "contact", "imageUrl", "description"
        );

        updates.forEach((fieldName, value) -> {
            if (!allowedFields.contains(fieldName)) return;

            try {
                switch (fieldName) {
                    case "regionId" -> program.setRegion(getRegion(Long.parseLong(value.toString())));
                    case "facilityId" -> program.setFacility(getFacility(Long.parseLong(value.toString())));
                    case "status" ->
                            program.setStatus("closed".equalsIgnoreCase(value.toString()) ? Program.Status.CLOSED : Program.Status.OPEN);
                    case "usageStartDate", "usageEndDate",
                         "registrationStartDate", "registrationEndDate", "cancelEndDate" -> {
                        Field field = Program.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(program, LocalDate.parse(value.toString()));
                    }
                    case "classStartTime", "classEndTime" -> {
                        Field field = Program.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(program, LocalTime.parse(value.toString()));
                    }
                    default -> {
                        Field field = Program.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object parsed = field.getType().equals(Integer.class)
                                ? Integer.parseInt(value.toString())
                                : value;
                        field.set(program, parsed);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("필드 업데이트 실패: " + fieldName + " - " + e.getMessage(), e);
            }
        });

        return toDisplayResponse(program);
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

    private void validateDuplicateTitle(String title) {
        boolean exists = programRepository.findAll().stream()
                .anyMatch(program -> program.getTitle().equalsIgnoreCase(title));
        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 프로그램 제목입니다.");
        }
    }

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

    // ===== 내부 유틸 =====

    private Program buildProgramFromDto(ProgramRequest dto) {
        return Program.builder()
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
    }

    private void updateProgramFromDto(Program program, ProgramRequest dto) {
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
    }
    private ProgramDisplayResponse toDisplayResponse(Program program) {
        return ProgramDisplayResponse.builder()
                .id(program.getId())
                .title(program.getTitle())
                .location(program.getFacility().getLocation())
                .target(program.getTarget())
                .usagePeriod(formatDateRange(program.getUsageStartDate(), program.getUsageEndDate()))
                .classTime(formatTimeRange(program.getClassStartTime(), program.getClassEndTime()))
                .registrationPeriod(formatDateRange(program.getRegistrationStartDate(), program.getRegistrationEndDate()))
                .cancelEndDate(program.getCancelEndDate().toString())
                .fee(program.getFee())
                .capacity(program.getCapacity())
                .contact(program.getContact())
                .description(program.getDescription())
                .imageUrl(program.getImageUrl())
                .build();
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        return start + " ~ " + end;
    }

    private String formatTimeRange(LocalTime start, LocalTime end) {
        return start + " ~ " + end;
    }


}