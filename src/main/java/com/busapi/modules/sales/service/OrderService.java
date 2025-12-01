package com.busapi.modules.sales.service;

import com.busapi.core.exception.BusinessException;
import com.busapi.core.exception.ResourceNotFoundException;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.identity.entity.User;
import com.busapi.modules.identity.service.UserService;
import com.busapi.modules.sales.dto.CreateOrderRequest;
import com.busapi.modules.sales.dto.OrderResponse;
import com.busapi.modules.sales.dto.TicketRequestItem;
import com.busapi.modules.sales.dto.TicketResponse;
import com.busapi.modules.sales.entity.BillingAddress;
import com.busapi.modules.sales.entity.Ticket;
import com.busapi.modules.sales.entity.TicketOrder;
import com.busapi.modules.sales.enums.Gender;
import com.busapi.modules.sales.enums.TicketStatus;
import com.busapi.modules.sales.repository.TicketOrderRepository;
import com.busapi.modules.sales.repository.TicketRepository;
import com.busapi.modules.voyage.entity.Trip;
import com.busapi.modules.voyage.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final TicketOrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final UserService userService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Sefer Kontrolü
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", request.getTripId()));

        if (trip.getDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Geçmiş tarihli sefere bilet alınamaz.");
        }

        // 2. Mevcut Dolu Koltukları Çek (Cache mantığıyla)
        List<Ticket> existingTickets = ticketRepository.findActiveTicketsByTripId(trip.getId());
        Set<Integer> occupiedSeats = existingTickets.stream()
                .map(Ticket::getSeatNumber)
                .collect(Collectors.toSet());

        // Map<SeatNumber, Gender> -> Cinsiyet kuralı kontrolü için
        Map<Integer, Gender> seatGenderMap = existingTickets.stream()
                .collect(Collectors.toMap(Ticket::getSeatNumber, Ticket::getPassengerGender));

        // İstekteki koltukları da bu haritaya geçici olarak ekleyeceğiz
        // (Çünkü aynı sipariş içinde yan yana koltuk alınıyor olabilir)
        for (TicketRequestItem item : request.getTickets()) {
            if (seatGenderMap.containsKey(item.getSeatNumber())) {
                throw new BusinessException("Koltuk " + item.getSeatNumber() + " zaten dolu!");
            }
            seatGenderMap.put(item.getSeatNumber(), item.getPassengerGender());
        }

        // 3. Validasyonlar (Doluluk ve Cinsiyet)
        for (TicketRequestItem item : request.getTickets()) {
            // Doluluk
            if (occupiedSeats.contains(item.getSeatNumber())) {
                throw new BusinessException("Koltuk " + item.getSeatNumber() + " zaten dolu.");
            }
            // Cinsiyet
            validateGenderRule(trip, item.getSeatNumber(), item.getPassengerGender(), seatGenderMap);
        }

        // 4. Sipariş (Order) Oluşturma
        User currentUser = getCurrentUser();
        TicketOrder order = new TicketOrder();
        order.setBuyer(currentUser);
        order.setOrderPnr(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setContactEmail(request.getContactEmail());
        order.setContactPhone(request.getContactPhone());

        // Fatura
        if (request.getBillingAddress() != null) {
            BillingAddress bill = new BillingAddress();
            bill.setCity(request.getBillingAddress().getCity());
            bill.setFullAddress(request.getBillingAddress().getFullAddress());
            bill.setTaxNumber(request.getBillingAddress().getTaxNumber());
            bill.setTaxOffice(request.getBillingAddress().getTaxOffice());
            order.setBillingAddress(bill);
        }

        // 5. Biletleri Oluştur ve Siparişe Ekle
        BigDecimal unitPrice = trip.getVoyage().getBasePrice();
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<TicketResponse> ticketResponses = new ArrayList<>();

        for (TicketRequestItem item : request.getTickets()) {
            Ticket ticket = new Ticket();
            ticket.setTrip(trip);
            ticket.setOrder(order); // İlişki
            ticket.setPassengerName(item.getPassengerName());
            ticket.setPassengerSurname(item.getPassengerSurname());
            ticket.setPassengerTc(item.getPassengerTc());
            ticket.setPassengerGender(item.getPassengerGender());
            ticket.setSeatNumber(item.getSeatNumber());
            ticket.setPrice(unitPrice);
            ticket.setStatus(TicketStatus.SOLD);

            order.getTickets().add(ticket); // Cascade Save yapacak

            totalAmount = totalAmount.add(unitPrice);

            // Response için hazırla
            TicketResponse tr = new TicketResponse();
            tr.setSeatNumber(ticket.getSeatNumber());
            tr.setPassengerName(ticket.getPassengerName() + " " + ticket.getPassengerSurname());
            tr.setPrice(unitPrice);
            tr.setStatus(TicketStatus.SOLD);
            ticketResponses.add(tr);
        }

        order.setTotalAmount(totalAmount);

        // 6. Puan Sistemi (Loyalty)
        if (currentUser != null) {
            // %1 Puan kazanımı
            BigDecimal pointsEarned = totalAmount.multiply(new BigDecimal("0.01"));
            currentUser.setCurrentPoints(currentUser.getCurrentPoints().add(pointsEarned));
            userService.updateUser(currentUser); // Puanı kaydet
        }

        // 7. Kaydet (Order + Tickets cascade olarak kaydedilir)
        orderRepository.save(order);

        // 8. Response Dön
        OrderResponse response = new OrderResponse();
        response.setOrderPnr(order.getOrderPnr());
        response.setTotalPrice(totalAmount.toString());
        response.setContactEmail(order.getContactEmail());
        response.setTickets(ticketResponses);

        return response;
    }

    private void validateGenderRule(Trip trip, int seatNumber, Gender gender, Map<Integer, Gender> seatMap) {
        if (trip.getBus().getBusType() == BusType.STANDARD_2_2) {
            // 1-2, 3-4 mantığı
            int neighborSeat = (seatNumber % 2 == 0) ? seatNumber - 1 : seatNumber + 1;

            if (seatMap.containsKey(neighborSeat)) {
                Gender neighborGender = seatMap.get(neighborSeat);
                if (neighborGender != gender) {
                    throw new BusinessException("Koltuk " + seatNumber + " için cinsiyet kuralı hatası. Yan koltuk: " + neighborGender);
                }
            }
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userService.getByEmail(auth.getName());
        }
        return null;
    }
}