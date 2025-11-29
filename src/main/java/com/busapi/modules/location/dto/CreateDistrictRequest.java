package com.busapi.modules.location.dto;

import lombok.Data;

@Data
public class CreateDistrictRequest {
    private String name;
    private Long cityId;
}
