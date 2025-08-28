package com.example.moyeorak.service;

import com.example.moyeorak.dto.ProgramDisplayResponse;
import com.example.moyeorak.dto.ProgramRequest;
import com.example.moyeorak.entity.Program;
import com.example.moyeorak.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramServiceImpl implements ProgramService {

    private final ProgramRepository programRepository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // ======================== 생성 ========================
    @Override
    @Transactional
    public ProgramDisplayResponse createProgram(ProgramRequest dto) {
        log.info("[CREATE] 프로그램 등록: {}", dto);

        validateDuplicate(dto);

        Program program = Program.builder()
                .title(dto.getTitle())
                .regionId(dto.getRegionId())
                .facilityId(dto.getFacilityId())
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
                .inPrice(dto.getInPrice())
                .outPrice(dto.getOutPrice())
                .capacity(dto.getCapacity())
                .contact(dto.getContact())
                .imageUrl(dto.getImageUrl())
                .description(dto.getDescription())
                .build();

        return toDisplayResponse(programRepository.save(program), null);
    }

    // ======================== 조회 ========================
    @Override
    public List<ProgramDisplayResponse> getAllPrograms(Long userRegionId) {
        log.info("[GET] 전체 프로그램 조회 - userRegionId: {}", userRegionId);
        return programRepository.findAll().stream()
                .map(p -> toDisplayResponse(p, userRegionId))
                .toList();
    }

    @Override
    public List<ProgramDisplayResponse> getProgramsByRegion(Long regionId, Long userRegionId) {
        log.info("[GET] 지역별 프로그램 조회 - regionId: {}, userRegionId: {}", regionId, userRegionId);
        return programRepository.findByRegionId(regionId).stream()
                .map(p -> toDisplayResponse(p, userRegionId))
                .toList();
    }

    @Override
    public ProgramDisplayResponse getProgramById(Long id, Long userRegionId) {
        log.info("[GET] 프로그램 상세 조회 - ID: {}, userRegionId: {}", id, userRegionId);
        return toDisplayResponse(getProgram(id), userRegionId);
    }

    // ======================== 부분 수정 ========================
    @Override
    @Transactional
    public ProgramDisplayResponse partialUpdateProgram(Long id, Map<String, Object> updates) {
        log.info("[PATCH] 프로그램 부분 수정 - ID: {}", id);
        Program program = getProgram(id);

        Set<String> allowedFields = Set.of(
                "title", "regionId", "facilityId", "category", "target", "instructorName", "status",
                "usageStartDate", "usageEndDate", "classStartTime", "classEndTime",
                "registrationStartDate", "registrationEndDate", "cancelEndDate",
                "inPrice", "outPrice", "capacity", "contact", "imageUrl", "description"
        );

        updates.forEach((fieldName, value) -> {
            if (!allowedFields.contains(fieldName)) return;
            try {
                switch (fieldName) {
                    case "status" -> program.setStatus("closed".equalsIgnoreCase(value.toString())
                            ? Program.Status.CLOSED : Program.Status.OPEN);
                    case "usageStartDate", "usageEndDate", "registrationStartDate",
                         "registrationEndDate", "cancelEndDate" -> {
                        Field field = Program.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(program, LocalDate.parse(value.toString()));
                    }
                    case "classStartTime", "classEndTime" -> {
                        Field field = Program.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(program, LocalTime.parse(value.toString()));
                    }
                    case "regionId", "facilityId" -> {
                        Field field = Program.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(program, Long.parseLong(value.toString()));
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

        return toDisplayResponse(program, null);
    }

    // ======================== 삭제 ========================
    @Override
    @Transactional
    public void deleteProgram(Long id) {
        log.info("[DELETE] 프로그램 삭제 - ID: {}", id);
        if (!programRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 프로그램이 없습니다.");
        }
        programRepository.deleteById(id);
    }

    // ======================== 기타 ========================
    @Override
    public boolean isAsyncPeriod(Long programId) {
        Program program = getProgram(programId);

        LocalDate registrationDate = program.getRegistrationStartDate();
        if (registrationDate == null) {
            log.warn("[isAsyncPeriod] registration_start_date가 null입니다. programId={}", programId);
            return false;
        }

        LocalDateTime now = LocalDateTime.now(KST);
        LocalDateTime start = registrationDate.atTime(9, 0);
        LocalDateTime end = registrationDate.atTime(10, 0);

        boolean result = now.isAfter(start) && now.isBefore(end);
        log.info("[isAsyncPeriod] programId={}, now={}, start={}, end={}, isAsync={}",
                programId, now, start, end, result);

        return result;
    }

    // ======================== 내부 유틸 ========================
    private void validateDuplicate(ProgramRequest dto) {
        programRepository.findByTitleAndFacilityIdAndUsageStartDateAndUsageEndDateAndClassStartTimeAndClassEndTime(
                dto.getTitle(),
                dto.getFacilityId(),
                dto.getUsageStartDate(),
                dto.getUsageEndDate(),
                dto.getClassStartTime(),
                dto.getClassEndTime()
        ).ifPresent(p -> {
            throw new IllegalArgumentException("이미 동일한 조건의 프로그램이 존재합니다.");
        });
    }

    private Program getProgram(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("프로그램(ID: " + id + ")이 존재하지 않습니다."));
    }

    private ProgramDisplayResponse toDisplayResponse(Program program, Long userRegionId) {
        boolean inRegion = Objects.equals(userRegionId, program.getRegionId());

        return ProgramDisplayResponse.builder()
                .id(program.getId())
                .title(program.getTitle())
                .target(program.getTarget())
                .usagePeriod(formatDateRange(program.getUsageStartDate(), program.getUsageEndDate()))
                .classTime(formatTimeRange(program.getClassStartTime(), program.getClassEndTime()))
                .registrationPeriod(formatDateRange(program.getRegistrationStartDate(), program.getRegistrationEndDate()))
                .cancelEndDate(program.getCancelEndDate().toString())
                .inPrice(program.getInPrice())
                .outPrice(program.getOutPrice())
                .appliedPrice(inRegion ? program.getInPrice() : program.getOutPrice())
                .inRegion(inRegion)
                .capacity(program.getCapacity())
                .contact(program.getContact())
                .description(program.getDescription())
                .imageUrl(program.getImageUrl())
                .regionId(program.getRegionId())
                .facilityId(program.getFacilityId())
                .instructorName(program.getInstructorName())
                .status(program.getStatus().name())
                .build();
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        return start + " ~ " + end;
    }

    private String formatTimeRange(LocalTime start, LocalTime end) {
        return start + " ~ " + end;
    }
}
