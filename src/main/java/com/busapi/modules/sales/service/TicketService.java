package com.busapi.modules.sales.service;

import com.busapi.core.exception.BusinessException;
import com.busapi.core.exception.ResourceNotFoundException;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.identity.entity.User;
import com.busapi.modules.identity.service.UserService;
import com.busapi.modules.sales.dto.SeatStatusResponse;
import com.busapi.modules.sales.dto.TicketPurchaseRequest;
import com.busapi.modules.sales.dto.TicketResponse;
import com.busapi.modules.sales.entity.Ticket;
import com.busapi.modules.sales.enums.Gender;
import com.busapi.modules.sales.enums.TicketStatus;
import com.busapi.modules.sales.repository.TicketRepository;
import com.busapi.modules.voyage.entity.Trip;
import com.busapi.modules.voyage.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final UserService userService;

    // Seferdeki koltuk durumlarını getirir
    public List<SeatStatusResponse> getSeatStatus(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        // Satılan biletleri çek
        List<Ticket> soldTickets = ticketRepository.findActiveTicketsByTripId(tripId);
        Map<Integer, Ticket> ticketMap = soldTickets.stream()
                .collect(Collectors.toMap(Ticket::getSeatNumber, t -> t));

        List<SeatStatusResponse> statusList = new ArrayList<>();
        int capacity = trip.getBus().getSeatCapacity();

        for (int i = 1; i <= capacity; i++) {
            Ticket t = ticketMap.get(i);
            statusList.add(SeatStatusResponse.builder()
                    .seatNumber(i)
                    .isOccupied(t != null)
                    .occupantGender(t != null ? t.getPassengerGender() : null)
                    .build());
        }
        return statusList;
    }

    @Transactional
    public TicketResponse sellTicket(TicketPurchaseRequest request) {
        // 1. Sefer kontrolü
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", request.getTripId()));

        // 2. Tarih kontrolü (Geçmişe bilet satılmaz)
        if (trip.getDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Geçmiş tarihli sefere bilet satılamaz.");
        }

        // 3. Koltuk dolu mu?
        if (ticketRepository.isSeatOccupied(trip.getId(), request.getSeatNumber())) {
            throw new BusinessException("Seçilen koltuk (" + request.getSeatNumber() + ") zaten dolu.");
        }

        // 4. Kapasite kontrolü
        if (request.getSeatNumber() > trip.getBus().getSeatCapacity()) {
            throw new BusinessException("Geçersiz koltuk numarası.");
        }

        // 5. Cinsiyet Kontrolü (Bay yanı Bay, Bayan yanı Bayan)
        validateGenderRestriction(trip, request.getSeatNumber(), request.getPassengerGender());

        // 6. Kullanıcıyı bul (Login ise)
        User currentUser = getCurrentUser();

        // 7. Bileti Oluştur
        Ticket ticket = new Ticket();
        ticket.setTrip(trip);
        ticket.setUser(currentUser);
        ticket.setPassengerName(request.getPassengerName());
        ticket.setPassengerSurname(request.getPassengerSurname());
        ticket.setPassengerTc(request.getPassengerTc());
        ticket.setPassengerPhone(request.getPassengerPhone());
        ticket.setPassengerGender(request.getPassengerGender());
        ticket.setSeatNumber(request.getSeatNumber());
        ticket.setPrice(trip.getVoyage().getBasePrice()); // Fiyatlama politikası buraya eklenebilir
        ticket.setStatus(TicketStatus.SOLD);
        ticket.setPnrCode(generatePNR());

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToResponse(savedTicket);
    }

    // --- YARDIMCI METODLAR ---

    private void validateGenderRestriction(Trip trip, int seatNumber, Gender requestGender) {
        // Sadece 2+2 Standart otobüslerde yan yana kısıtlaması vardır.
        // 2+1 Suit araçlarda genellikle tekli koltuk serbesttir, çiftlilerde kural vardır.
        // Basitleştirilmiş mantık (2+2 varsayımı): (1-2), (3-4)...

        if (trip.getBus().getBusType() == BusType.STANDARD_2_2) {
            int neighborSeat = (seatNumber % 2 == 0) ? seatNumber - 1 : seatNumber + 1;

            // Yan koltuktaki bileti bul
            // Bu sorgu verimsiz olabilir, cache veya map kullanılabilir ama şimdilik yeterli.
            List<Ticket> tickets = ticketRepository.findActiveTicketsByTripId(trip.getId());
            Optional<Ticket> neighborTicket = tickets.stream()
                    .filter(t -> t.getSeatNumber() == neighborSeat)
                    .findFirst();

            if (neighborTicket.isPresent()) {
                if (neighborTicket.get().getPassengerGender() != requestGender) {
                    throw new BusinessException("Farklı cinsiyetteki yolcunun yanına bilet alınamaz.");
                }
            }
        }
    }

    private String generatePNR() {
        // Basit 6 haneli random PNR
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userService.getByEmail(auth.getName());
        }
        return null; // Guest user
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setPnrCode(ticket.getPnrCode());
        response.setPassengerName(ticket.getPassengerName() + " " + ticket.getPassengerSurname());
        response.setSeatNumber(ticket.getSeatNumber());
        response.setPrice(ticket.getPrice());
        response.setStatus(ticket.getStatus());
        response.setTripDescription(ticket.getTrip().getVoyage().getRoute().getName() + " / " + ticket.getTrip().getDepartureDateTime());
        return response;
    }
}