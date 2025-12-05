package com.busapi.modules.voyage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchLocationResponse {
    private UUID id; // İlçe ID'si (Kalkış/Varış noktası aslında ilçedir)
    private String label; // "İstanbul - Esenler" formatında
    private String cityName;
    private String districtName;
}