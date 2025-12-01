package com.busapi.modules.fleet.dto;

import com.busapi.modules.fleet.enums.BusType;
import lombok.Data;

import java.util.UUID;

@Data
public class BusResponse {
    private UUID id;
    private String plateNumber;
    private BusType busType;
    private int seatCapacity;
    private boolean isActive;
}