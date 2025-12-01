package com.busapi.modules.identity.controller;

import com.busapi.core.result.ApiResponse;
import com.busapi.modules.identity.dto.AgencyResponse;
import com.busapi.modules.identity.dto.CreateAgencyRequest;
import com.busapi.modules.identity.service.AgencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;

    // Sadece Admin yeni acenta açabilir
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<AgencyResponse> createAgency(@Valid @RequestBody CreateAgencyRequest request) {
        return ApiResponse.success(agencyService.createAgency(request), "Acenta başarıyla oluşturuldu.");
    }

    @GetMapping
    public ApiResponse<List<AgencyResponse>> getAllAgencies() {
        return ApiResponse.success(agencyService.getAllAgencies());
    }
}