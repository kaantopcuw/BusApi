package com.busapi.modules.voyage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
public class CreateRouteRequest {
    @NotBlank
    private String name;

    @NotNull
    private Long departureDistrictId;

    @NotNull
    private Long arrivalDistrictId;

    @Valid
    private List<CreateStopRequest> stops;
}
