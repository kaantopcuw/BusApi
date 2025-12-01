package com.busapi.modules.fleet.controller;

import com.busapi.core.result.ApiResponse;
import com.busapi.modules.fleet.dto.BusResponse;
import com.busapi.modules.fleet.dto.CreateBusRequest;
import com.busapi.modules.fleet.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fleet/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AGENCY_MANAGER')")
    public ApiResponse<List<BusResponse>> getAllBuses() {
        return ApiResponse.success(busService.getAllBuses());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<BusResponse> createBus(@Valid @RequestBody CreateBusRequest request) {
        return ApiResponse.success(busService.createBus(request), "Otobüs filoya eklendi.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<Void> deleteBus(@PathVariable UUID id) {
        busService.deleteBus(id);
        return ApiResponse.success("Otobüs başarıyla silindi (Arşivlendi).");
    }
}