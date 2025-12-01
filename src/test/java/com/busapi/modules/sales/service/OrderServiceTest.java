package com.busapi.modules.sales.service;

import com.busapi.core.exception.BusinessException;
import com.busapi.modules.fleet.entity.Bus;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.identity.service.UserService;
import com.busapi.modules.sales.dto.CreateOrderRequest;
import com.busapi.modules.sales.dto.OrderResponse;
import com.busapi.modules.sales.dto.TicketRequestItem;
import com.busapi.modules.sales.entity.Ticket;
import com.busapi.modules.sales.entity.TicketOrder;
import com.busapi.modules.sales.enums.Gender;
import com.busapi.modules.sales.repository.TicketOrderRepository;
import com.busapi.modules.sales.repository.TicketRepository;
import com.busapi.modules.voyage.entity.Route;
import com.busapi.modules.voyage.entity.Trip;
import com.busapi.modules.voyage.entity.Voyage;
import com.busapi.modules.voyage.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private TicketOrderRepository orderRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserService userService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private OrderService orderService;

    private Trip trip;

    @BeforeEach
    void setup() {
        // Security Context Mocking (Null user sorunu yaşamamak için)
        SecurityContextHolder.setContext(securityContext);

        // Ortak Trip Verisi
        Bus bus = new Bus();
        bus.setBusType(BusType.STANDARD_2_2); // Cinsiyet kuralı için önemli
        bus.setSeatCapacity(40);

        Voyage voyage = new Voyage();
        voyage.setBasePrice(BigDecimal.valueOf(100));
        voyage.setRoute(new Route()); // Null pointer yememek için

        trip = new Trip();
        trip.setId(1L);
        trip.setBus(bus);
        trip.setVoyage(voyage);
        trip.setDate(LocalDate.now().plusDays(1));
    }

    @Test
    @DisplayName("Başarılı Sipariş: Koltuklar boş ve cinsiyet uygunsa sipariş oluşmalı")
    void createOrder_Success() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setTripId(1L);
        request.setContactEmail("test@mail.com");
        request.setContactPhone("555");

        TicketRequestItem item = new TicketRequestItem();
        item.setSeatNumber(1);
        item.setPassengerGender(Gender.MALE);
        item.setPassengerName("Ali");
        item.setPassengerSurname("Veli");
        item.setPassengerTc("111");
        request.setTickets(List.of(item));

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(ticketRepository.findActiveTicketsByTripId(1L)).thenReturn(Collections.emptyList()); // Hiç bilet yok
        when(orderRepository.save(any(TicketOrder.class))).thenAnswer(i -> i.getArguments()[0]); // Kaydedileni dön

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        assertThat(response.getOrderPnr()).isNotNull();
        assertThat(response.getTotalPrice()).isEqualTo("100"); // 1 bilet * 100 TL
        verify(orderRepository).save(any(TicketOrder.class));
    }

    @Test
    @DisplayName("Cinsiyet Kuralı: 2+2 otobüste Bayan yanına Bay oturamaz")
    void createOrder_GenderMismatch_ShouldThrowException() {
        // Given
        // Mevcut durumda Koltuk 1'de KADIN oturuyor olsun
        Ticket existingTicket = new Ticket();
        existingTicket.setSeatNumber(1);
        existingTicket.setPassengerGender(Gender.FEMALE);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        // Repository mock: Zaten satılmış biletleri döndür
        when(ticketRepository.findActiveTicketsByTripId(1L)).thenReturn(List.of(existingTicket));

        // Request: Koltuk 2'ye ERKEK almaya çalışıyor
        CreateOrderRequest request = new CreateOrderRequest();
        request.setTripId(1L);
        TicketRequestItem item = new TicketRequestItem();
        item.setSeatNumber(2); // 1'in yanı
        item.setPassengerGender(Gender.MALE); // HATA!
        item.setPassengerName("Ahmet");
        item.setPassengerSurname("Can");
        item.setPassengerTc("222");
        request.setTickets(List.of(item));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cinsiyet kuralı hatası");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Stok Kontrolü: Dolu koltuğa tekrar bilet alınamaz")
    void createOrder_SeatOccupied_ShouldThrowException() {
        // Given
        Ticket existingTicket = new Ticket();
        existingTicket.setSeatNumber(5);
        // FIX: NullPointerException yememek için cinsiyet set ediyoruz
        existingTicket.setPassengerGender(Gender.MALE);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        // Mock response
        when(ticketRepository.findActiveTicketsByTripId(1L)).thenReturn(List.of(existingTicket));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setTripId(1L);

        TicketRequestItem item = new TicketRequestItem();
        item.setSeatNumber(5); // Zaten dolu
        item.setPassengerGender(Gender.MALE);
        item.setPassengerName("Test");
        item.setPassengerSurname("User");
        item.setPassengerTc("123");

        request.setTickets(List.of(item));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten dolu");
    }
}