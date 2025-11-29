package com.busapi.modules.voyage.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TripResponse {
    private Long id;
    private String routeName;
    private String departureTime; // "14:00"
    private String date; // "2023-12-01"
    private String busPlateNumber;
    private String status;
    private BigDecimal price;
}
