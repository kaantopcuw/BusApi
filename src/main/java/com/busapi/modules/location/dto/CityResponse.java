package com.busapi.modules.location.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CityResponse {
    private UUID id;
    private String name;
    private int plateCode;
}
