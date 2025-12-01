package com.busapi.modules.identity.dto;

import lombok.Data;

@Data
public class AgencyResponse {
    private Long id;
    private String name;
    private String districtName;
    private String cityName;
    private String address;
    private String contactPhone;
    private boolean isActive;
}