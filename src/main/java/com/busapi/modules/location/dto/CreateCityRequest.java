package com.busapi.modules.location.dto;

import lombok.Data;

@Data
public class CreateCityRequest {
    private String name;
    private int plateCode;
}
