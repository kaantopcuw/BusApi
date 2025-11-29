package com.busapi.modules.location.controller;

import com.busapi.core.result.ApiResponse;
import com.busapi.modules.location.dto.CityResponse;
import com.busapi.modules.location.dto.CreateCityRequest;
import com.busapi.modules.location.dto.CreateDistrictRequest;
import com.busapi.modules.location.dto.DistrictResponse;
import com.busapi.modules.location.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // Public: Herkes şehirleri listeleyebilir
    @GetMapping("/cities")
    public ApiResponse<List<CityResponse>> getAllCities() {
        return ApiResponse.success(locationService.getAllCities());
    }

    // Public: Şehre göre ilçe listeleme
    @GetMapping("/cities/{cityId}/districts")
    public ApiResponse<List<DistrictResponse>> getDistrictsByCity(@PathVariable Long cityId) {
        return ApiResponse.success(locationService.getDistrictsByCity(cityId));
    }

    // Admin: Yeni Şehir Ekleme
    @PostMapping("/cities")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<CityResponse> createCity(@Valid @RequestBody CreateCityRequest request) {
        return ApiResponse.success(locationService.createCity(request));
    }

    // Admin: Yeni İlçe Ekleme
    @PostMapping("/districts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<DistrictResponse> createDistrict(@Valid @RequestBody CreateDistrictRequest request) {
        return ApiResponse.success(locationService.createDistrict(request));
    }
}