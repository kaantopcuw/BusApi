package com.busapi.modules.sales.dto;

import com.busapi.modules.sales.enums.TicketStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketResponse {
    private Long id;
    private String pnrCode;
    private String tripDescription; // "Istanbul - Ankara / 14:00"
    private int seatNumber;
    private String passengerName;
    private BigDecimal price;
    private TicketStatus status;
}