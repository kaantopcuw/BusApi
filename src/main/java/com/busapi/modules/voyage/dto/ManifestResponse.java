package com.busapi.modules.voyage.dto;

import com.busapi.modules.sales.enums.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ManifestResponse {
    private UUID tripId;
    private String plateNumber;
    private String routeName;
    private String date;
    private String time;
    private List<PassengerInfo> passengers;

    @Data
    @Builder
    public static class PassengerInfo {
        private int seatNumber;
        private String fullName;
        private String tcNo;      // Maskelenmiş veya tam
        private String phone;
        private TicketStatus status;
    }
}