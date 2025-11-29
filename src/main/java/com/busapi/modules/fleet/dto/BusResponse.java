package com.busapi.modules.fleet.dto;

import com.busapi.modules.fleet.enums.BusType;
import lombok.Data;

@Data
public class BusResponse {
    private Long id;
    private String plateNumber;
    private BusType busType;
    private int seatCapacity;
    private boolean isActive;
}