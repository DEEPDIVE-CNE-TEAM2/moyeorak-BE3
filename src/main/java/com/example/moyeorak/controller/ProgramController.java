package com.example.moyeorak.controller;

import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.dto.ProgramRequest;
import com.example.moyeorak.dto.ProgramResponse;
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
    public ResponseEntity<ProgramResponse> createProgram(@RequestBody @Valid ProgramRequest request) {
        ProgramResponse response = programService.createProgram(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProgramResponse>> getAllPrograms() {
        List<ProgramResponse> programs = programService.getAllPrograms();
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramResponse> getProgramById(@PathVariable Long id) {
        ProgramResponse program = programService.getProgramById(id);
        return ResponseEntity.ok(program);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramResponse> updateProgram(@PathVariable Long id,
                                                         @RequestBody @Valid ProgramRequest request) {
        ProgramResponse updated = programService.updateProgram(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramResponse> patchProgram(@PathVariable Long id,
                                                        @RequestBody Map<String, Object> updates) {
        ProgramResponse updated = programService.partialUpdateProgram(id, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id);
        return ResponseEntity.ok(new MessageResponse("프로그램이 삭제되었습니다."));
    }

}