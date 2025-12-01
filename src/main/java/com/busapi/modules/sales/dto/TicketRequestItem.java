package com.busapi.modules.sales.dto;

import com.busapi.modules.sales.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketRequestItem {
    @NotNull
    @Min(1) @Max(100)
    private int seatNumber;

    @NotBlank
    private String passengerName;

    @NotBlank
    private String passengerSurname;

    @NotBlank
    private String passengerTc; // TCKN validasyonu eklenebilir

    @NotNull
    private Gender passengerGender;
}
