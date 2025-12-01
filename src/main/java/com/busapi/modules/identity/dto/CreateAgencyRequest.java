package com.busapi.modules.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAgencyRequest {
    @NotBlank(message = "Acenta adı boş olamaz")
    private String name;

    @NotNull(message = "İlçe seçimi zorunludur")
    private Long districtId;

    @NotBlank(message = "Adres boş olamaz")
    private String address;

    @NotBlank(message = "Telefon boş olamaz")
    private String contactPhone;

    private String taxNumber;
}
