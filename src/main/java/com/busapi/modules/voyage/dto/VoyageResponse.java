package com.busapi.modules.voyage.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VoyageResponse {
    private String voyageId; // UUID
    private String routeName;
    private String departureTime;
    private String date;
    private String busType;
    private BigDecimal price;
}