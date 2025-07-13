package com.example.moyeorak.controller;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.service.EnrollmentService;
import com.example.moyeorak.service.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramDisplayResponse> createProgram(@RequestBody @Valid ProgramRequest request) {
        return ResponseEntity.ok(programService.createProgram(request));
    }

    @GetMapping
    public ResponseEntity<List<ProgramDisplayResponse>> getPrograms(
            @RequestParam(value = "regionId", required = false) Long regionId
    ) {
        if (regionId != null) {
            return ResponseEntity.ok(programService.getProgramsByRegion(regionId));
        } else {
            return ResponseEntity.ok(programService.getAllPrograms());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramDisplayResponse> getProgramById(@PathVariable Long id) {
        return ResponseEntity.ok(programService.getProgramById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramDisplayResponse> patchProgram(@PathVariable Long id,
                                                               @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(programService.partialUpdateProgram(id, updates));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id);
        return ResponseEntity.ok(new MessageResponse("프로그램이 삭제되었습니다."));
    }

    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<ProgramDisplayResponse>> getProgramsByRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(programService.getProgramsByRegion(regionId));
    }
}