package com.busapi.modules.identity.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AgencyResponse {
    private UUID id;
    private String name;
    private String districtName;
    private String cityName;
    private String address;
    private String contactPhone;
    private boolean isActive;
}