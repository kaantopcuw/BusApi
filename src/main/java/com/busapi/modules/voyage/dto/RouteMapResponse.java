package com.busapi.modules.voyage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteMapResponse {
    // Kalkış Noktası Bilgileri
    private UUID originId;
    private String originLabel; // "İstanbul - Esenler"

    // Bu noktadan gidilebilecek yerlerin listesi
    private List<DestinationDTO> destinations;
}