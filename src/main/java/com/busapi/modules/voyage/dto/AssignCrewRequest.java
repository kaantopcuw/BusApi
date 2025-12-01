package com.busapi.modules.voyage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignCrewRequest {
    @NotNull(message = "Şoför seçimi zorunludur")
    private Long driverId;

    @NotNull(message = "Muavin seçimi zorunludur")
    private Long hostId;
}