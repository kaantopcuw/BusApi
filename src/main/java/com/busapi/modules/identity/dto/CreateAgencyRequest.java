package com.busapi.modules.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAgencyRequest {
    @NotBlank
    private String name;

    @NotNull
    private Long districtId;

    @NotBlank
    private String address;

    @NotBlank
    private String contactPhone;
}
