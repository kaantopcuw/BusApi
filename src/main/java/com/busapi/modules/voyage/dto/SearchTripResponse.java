package com.busapi.modules.voyage.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SearchTripResponse {
    private List<TripResponse> realTrips;    // Oluşmuş, kesinleşmiş seferler
    private List<VoyageResponse> virtualTrips; // Henüz oluşmamış planlı seferler
}