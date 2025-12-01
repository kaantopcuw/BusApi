package com.busapi.modules.location.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class DistrictResponse {
    private UUID id;
    private String name;
    private UUID cityId;
}