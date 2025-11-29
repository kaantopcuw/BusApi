package com.busapi.modules.fleet.dto;

import com.busapi.modules.fleet.enums.BusType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateBusRequest {

    @NotBlank(message = "Plaka zorunludur")
    @Pattern(regexp = "^(0[1-9]|[1-7][0-9]|8[01]) [A-Z]{1,3} \\d{2,4}$", message = "Geçersiz plaka formatı (Örn: 34 ABC 123)")
    private String plateNumber;

    @NotNull(message = "Otobüs tipi seçilmelidir")
    private BusType busType;

    @Min(value = 10, message = "Kapasite en az 10 olmalıdır")
    private int seatCapacity;
}