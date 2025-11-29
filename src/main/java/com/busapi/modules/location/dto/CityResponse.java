package com.busapi.modules.location.dto;

import lombok.Data;

@Data
public class CityResponse {
    private Long id;
    private String name;
    private int plateCode;
}
