package com.example.moyeorak.service;

import com.example.moyeorak.dto.RegionRequest;
import com.example.moyeorak.dto.RegionResponse;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;
    private final UserRepository userRepository;

    public RegionResponse createRegion(RegionRequest request) {
        User manager = null;

        if (request.getManagerId() != null) {
            Long managerId = request.getManagerId().longValue();
            manager = userRepository.findById(Math.toIntExact(managerId))
                    .orElseThrow(() -> new IllegalArgumentException("관리자 ID가 존재하지 않습니다."));
        }

        Region region = Region.builder()
                .name(request.getName())
                .manager(manager)
                .build();

        Region saved = regionRepository.save(region);

        return RegionResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .managerId(saved.getManager() != null ? saved.getManager().getId().intValue() : null)
                .build();
    }

    public List<RegionResponse> getAllRegions() {
        return regionRepository.findAll().stream()
                .map(region -> RegionResponse.builder()
                        .id(region.getId())
                        .name(region.getName())
                        .managerId(region.getManager() != null ? region.getManager().getId().intValue() : null)
                        .build())
                .collect(Collectors.toList());
    }

    public RegionResponse getRegion(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        return RegionResponse.builder()
                .id(region.getId())
                .name(region.getName())
                .managerId(region.getManager() != null ? region.getManager().getId().intValue() : null)
                .build();
    }

    public RegionResponse updateRegion(Long id, RegionRequest request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        region.setName(request.getName());

        if (request.getManagerId() != null) {
            Long managerId = request.getManagerId().longValue();
            User manager = userRepository.findById(Math.toIntExact(managerId))
                    .orElseThrow(() -> new IllegalArgumentException("관리자 ID가 존재하지 않습니다."));
            region.setManager(manager);
        } else {
            region.setManager(null);
        }

        Region updated = regionRepository.save(region);

        return RegionResponse.builder()
                .id(updated.getId())
                .name(updated.getName())
                .managerId(updated.getManager() != null ? updated.getManager().getId().intValue() : null)
                .build();
    }

    public ResponseEntity<String> deleteRegion(Long id) {
        if (!regionRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 지역이 존재하지 않습니다.");
        }
        regionRepository.deleteById(id);
        return ResponseEntity.ok("삭제 되었습니다");
    }
}
