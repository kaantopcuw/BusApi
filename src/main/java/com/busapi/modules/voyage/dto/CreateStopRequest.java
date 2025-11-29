package com.busapi.modules.voyage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStopRequest {
    @NotNull
    private Long districtId;

    @Min(0)
    private int stopOrder;

    @Min(0)
    private int kmFromStart;

    @Min(0)
    private int durationMinutesFromStart;
}
