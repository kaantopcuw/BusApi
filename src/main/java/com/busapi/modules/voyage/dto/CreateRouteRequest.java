package com.busapi.modules.voyage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Data
public class CreateRouteRequest {
    @NotBlank
    private String name;

    @NotNull
    private UUID departureDistrictId;

    @NotNull
    private UUID arrivalDistrictId;

    @Valid
    private List<CreateStopRequest> stops;
}
