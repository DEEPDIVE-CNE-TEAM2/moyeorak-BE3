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
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProgramDisplayResponse createProgram(ProgramRequest dto) {
        log.info("[CREATE] 추가 등록: {}", dto);
        validateDuplicateTitle(dto.getTitle());
        Program program = buildProgramFromDto(dto);
        return toDisplayResponse(programRepository.save(program), null);
    }

    public List<ProgramDisplayResponse> getAllPrograms(Long userId) {
        User user = getUser(userId);
        log.info("[GET] 전체 공정 목록 - userId: {}", userId);
        return programRepository.findAll().stream()
                .map(p -> toDisplayResponse(p, user))
                .toList();
    }

    public List<ProgramDisplayResponse> getProgramsByRegion(Long regionId, Long userId) {
        User user = getUser(userId);
        log.info("[GET] 지역 별 공정 목록 - regionId: {}, userId: {}", regionId, userId);
        return programRepository.findByRegion_Id(regionId).stream()
                .map(p -> toDisplayResponse(p, user))
                .toList();
    }

    public ProgramDisplayResponse getProgramById(Long id, Long userId) {
        User user = getUser(userId);
        log.info("[GET] 공정 상세 조회 - ID: {}, userId: {}", id, userId);
        return toDisplayResponse(getProgram(id), user);
    }

    @Transactional
    public ProgramDisplayResponse partialUpdateProgram(Long id, Map<String, Object> updates) {
        log.info("[PATCH] 공정 부분 수정 - ID: {}", id);
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
                    case "regionId" -> program.setRegion(getRegion(Long.parseLong(value.toString())));
                    case "facilityId" -> program.setFacility(getFacility(Long.parseLong(value.toString())));
                    case "status" -> program.setStatus("closed".equalsIgnoreCase(value.toString()) ? Program.Status.CLOSED : Program.Status.OPEN);
                    case "usageStartDate", "usageEndDate", "registrationStartDate", "registrationEndDate", "cancelEndDate" -> {
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

        return toDisplayResponse(program, null);
    }

    @Transactional
    public MessageResponse deleteProgram(Long id) {
        log.info("[DELETE] 공정 삭제 - ID: {}", id);

        if (!programRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 공정이 없습니다.");
        }

        programRepository.deleteById(id);
        return new MessageResponse("공정이 삭제되었습니다.");
    }

    private void validateDuplicateTitle(String title) {
        boolean exists = programRepository.findAll().stream()
                .anyMatch(program -> program.getTitle().equalsIgnoreCase(title));
        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 공정 제목입니다.");
        }
    }

    private Program getProgram(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공정(ID: " + id + ")이 존재하지 않습니다."));
    }

    private Region getRegion(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("지역(ID: " + regionId + ")이 존재하지 않습니다."));
    }

    private Facility getFacility(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("시설(ID: " + facilityId + ")이 존재하지 않습니다."));
    }

    private User getUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
    }

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
                .inPrice(dto.getInPrice())
                .outPrice(dto.getOutPrice())
                .capacity(dto.getCapacity())
                .contact(dto.getContact())
                .imageUrl(dto.getImageUrl())
                .description(dto.getDescription())
                .build();
    }

    private ProgramDisplayResponse toDisplayResponse(Program program, User user) {
        boolean inRegion = false;
        Long userRegionId = null;
        Long programRegionId = null;

        if (user != null) {
            userRegionId = Optional.ofNullable(user.getRegion()).map(Region::getId).orElse(null);
        }
        if (program.getRegion() != null) {
            programRegionId = program.getRegion().getId();
        }

        inRegion = Objects.equals(userRegionId, programRegionId);

        // ✅ 불일치 여부 로그로 확인
        if (program.getFacility() != null && program.getFacility().getRegion() != null && program.getRegion() != null) {
            Long facilityRegionId = program.getFacility().getRegion().getId();
            if (!facilityRegionId.equals(programRegionId)) {
                log.warn("[불일치] 프로그램과 시설의 지역ID 다름 - programId: {}, program.regionId: {}, facility.regionId: {}",
                        program.getId(), programRegionId, facilityRegionId);
            }
        }

        return ProgramDisplayResponse.builder()
                .id(program.getId())
                .title(program.getTitle())
                .location(program.getFacility().getName())
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
                .regionId(program.getRegion().getId())
                .instructorName(program.getInstructorName())
                .build();
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        return start + " ~ " + end;
    }

    private String formatTimeRange(LocalTime start, LocalTime end) {
        return start + " ~ " + end;
    }
}
