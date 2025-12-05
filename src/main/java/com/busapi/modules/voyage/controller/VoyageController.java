package com.busapi.modules.voyage.controller;

import com.busapi.core.result.ApiResponse;
import com.busapi.modules.voyage.dto.*;
import com.busapi.modules.voyage.service.VoyageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/voyages")
@RequiredArgsConstructor
public class VoyageController {

    private final VoyageService voyageService;

    // --- ROUTE ---
    @PostMapping("/routes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<UUID> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        return ApiResponse.success(voyageService.createRoute(request), "Güzergah oluşturuldu.");
    }

    // --- VOYAGE DEFINITION ---
    @PostMapping("/definitions")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<UUID> createVoyageDefinition(@Valid @RequestBody CreateVoyageRequest request) {
        return ApiResponse.success(voyageService.createVoyageDefinition(request), "Sefer şablonu oluşturuldu.");
    }

    // --- TRIP GENERATION & ASSIGNMENT ---
    @PostMapping("/trips/generate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<Void> generateTrips(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        voyageService.generateTripsForDate(date);
        return ApiResponse.success("Seferler oluşturuldu.");
    }

    @PutMapping("/trips/{tripId}/assign-bus/{busId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<Void> assignBus(@PathVariable UUID tripId, @PathVariable UUID busId) {
        voyageService.assignBusToTrip(tripId, busId);
        return ApiResponse.success("Otobüs sefere atandı.");
    }

    // --- PUBLIC SEARCH ---
    @GetMapping("/trips/search")
    public ApiResponse<List<TripResponse>> searchTrips(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam UUID fromId,
            @RequestParam UUID toId) {
        return ApiResponse.success(voyageService.searchTrips(date, fromId, toId));
    }

    @PutMapping("/trips/{tripId}/assign-crew")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<Void> assignCrew(
            @PathVariable UUID tripId,
            @Valid @RequestBody AssignCrewRequest request) {

        voyageService.assignCrewToTrip(tripId, request.getDriverId(), request.getHostId());
        return ApiResponse.success("Sefer personeli başarıyla atandı.");
    }

    @GetMapping("/trips/{tripId}/manifest")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AGENCY_STAFF', 'ROLE_DRIVER', 'ROLE_HOST')")
    public ApiResponse<ManifestResponse> getManifest(@PathVariable UUID tripId) {
        return ApiResponse.success(voyageService.getTripManifest(tripId));
    }

//    @GetMapping("/search/origins")
//    public ApiResponse<List<SearchLocationResponse>> getOrigins() {
//        return ApiResponse.success(voyageService.getAvailableOrigins());
//    }
//
//    @GetMapping("/search/destinations")
//    public ApiResponse<List<SearchLocationResponse>> getDestinations(@RequestParam Long fromId) {
//        return ApiResponse.success(voyageService.getAvailableDestinations(fromId));
//    }

    @GetMapping("/search/route-map")
    public ApiResponse<List<RouteMapResponse>> getRouteMap() {
        return ApiResponse.success(voyageService.getFullRouteMap());
    }
}