package com.busapi.modules.voyage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignCrewRequest {
    @NotNull(message = "Şoför seçimi zorunludur")
    private UUID driverId;

    @NotNull(message = "Muavin seçimi zorunludur")
    private UUID hostId;
}