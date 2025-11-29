package com.busapi.modules.sales.dto;

import com.busapi.modules.sales.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketPurchaseRequest {
    @NotNull
    private Long tripId;

    @NotNull
    @Min(1)
    @Max(100) // Otobüs kapasitesine göre değişir ama hard limit
    private int seatNumber;

    @NotBlank
    private String passengerName;

    @NotBlank
    private String passengerSurname;

    @NotBlank
    private String passengerTc;

    private String passengerPhone;

    @NotNull
    private Gender passengerGender;
}