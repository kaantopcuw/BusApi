package com.busapi.modules.voyage.dto;

import com.busapi.modules.fleet.enums.BusType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateVoyageRequest {
    @NotNull
    private UUID routeId;

    @NotNull
    private LocalTime departureTime;

    @NotNull
    private BusType busType;

    @NotNull
    @Min(0)
    private BigDecimal basePrice;
}
