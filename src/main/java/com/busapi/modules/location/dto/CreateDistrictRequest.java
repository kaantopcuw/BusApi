package com.busapi.modules.location.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateDistrictRequest {
    private String name;
    private UUID cityId;
}
