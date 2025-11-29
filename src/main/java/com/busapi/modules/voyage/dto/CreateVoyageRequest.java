package com.busapi.modules.voyage.dto;

import com.busapi.modules.fleet.enums.BusType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class CreateVoyageRequest {
    @NotNull
    private Long routeId;

    @NotNull
    private LocalTime departureTime;

    @NotNull
    private BusType busType;

    @NotNull
    @Min(0)
    private BigDecimal basePrice;
}
