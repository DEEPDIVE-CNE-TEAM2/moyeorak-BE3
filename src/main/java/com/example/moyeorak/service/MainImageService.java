package com.example.moyeorak.service;

import com.example.moyeorak.dto.MainImageRequest;
import com.example.moyeorak.dto.MainImageResponse;

import java.util.List;

public interface MainImageService {

    MainImageResponse create(MainImageRequest request);

    List<MainImageResponse> getByRegion(Long regionId);

    MainImageResponse update(Long id, MainImageRequest request);

    void delete(Long id);
}
