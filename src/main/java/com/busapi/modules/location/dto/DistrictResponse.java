package com.busapi.modules.location.dto;

import lombok.Data;

@Data
public class DistrictResponse {
    private Long id;
    private String name;
    private Long cityId;
}