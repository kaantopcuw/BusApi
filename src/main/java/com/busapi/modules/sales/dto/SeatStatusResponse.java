package com.busapi.modules.sales.dto;

import com.busapi.modules.sales.enums.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatStatusResponse {
    private int seatNumber;
    private boolean isOccupied;
    private Gender occupantGender; // Doluysa cinsiyeti (Kural kontrolü için front-end'e lazım)
}