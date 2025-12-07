package com.busapi.modules.sales.service;

import com.busapi.core.exception.ResourceNotFoundException;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.sales.dto.SeatStatusResponse;
import com.busapi.modules.sales.entity.Ticket;
import com.busapi.modules.sales.repository.TicketRepository;
import com.busapi.modules.voyage.entity.Trip;
import com.busapi.modules.voyage.entity.Voyage;
import com.busapi.modules.voyage.repository.TripRepository;
import com.busapi.modules.voyage.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final VoyageRepository voyageRepository;

    // Seferdeki koltuk durumlarını getirir (Dolu/Boş)
    public List<SeatStatusResponse> getSeatStatus(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        // O sefer için satılmış (aktif) biletleri çek
        List<Ticket> soldTickets = ticketRepository.findActiveTicketsByTripId(tripId);

        // Biletleri koltuk numarasına göre haritala (Hızlı erişim için)
        Map<Integer, Ticket> ticketMap = soldTickets.stream()
                .collect(Collectors.toMap(Ticket::getSeatNumber, t -> t));

        List<SeatStatusResponse> statusList = new ArrayList<>();
        int capacity = trip.getBus().getSeatCapacity();

        // 1'den kapasiteye kadar dön ve durum oluştur
        for (int i = 1; i <= capacity; i++) {
            Ticket t = ticketMap.get(i);
            statusList.add(SeatStatusResponse.builder()
                    .seatNumber(i)
                    .isOccupied(t != null) // Bilet varsa doludur
                    .occupantGender(t != null ? t.getPassengerGender() : null) // Cinsiyet kuralı kontrolü için gerekli
                    .build());
        }
        return statusList;
    }

    public List<SeatStatusResponse> getVoyageEmptySeats(java.util.UUID voyageId) {
        Voyage voyage = voyageRepository.findById(voyageId)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", "id", voyageId));

        List<SeatStatusResponse> statusList = new ArrayList<>();
        int capacity = voyage.getBusType() == BusType.SUITE_2_1 ? 30 : 46; // Basit mantık veya BusTypeEnum'dan al

        for (int i = 1; i <= capacity; i++) {
            statusList.add(SeatStatusResponse.builder()
                    .seatNumber(i)
                    .isOccupied(false) // Sanal olduğu için hepsi boş
                    .occupantGender(null)
                    .build());
        }
        return statusList;
    }

    // NOT: sellTicket metodu buradan SİLİNDİ.
    // Artık satış işlemi OrderService.createOrder() üzerinden yapılıyor.
}