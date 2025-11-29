package com.busapi.modules.sales.controller;

import com.busapi.config.SecurityConfig;
import com.busapi.core.security.JwtAuthenticationFilter;
import com.busapi.core.security.JwtService;
import com.busapi.core.security.UserSecurity;
import com.busapi.modules.identity.service.UserService;
import com.busapi.modules.sales.dto.SeatStatusResponse;
import com.busapi.modules.sales.dto.TicketPurchaseRequest;
import com.busapi.modules.sales.dto.TicketResponse;
import com.busapi.modules.sales.enums.Gender;
import com.busapi.modules.sales.enums.TicketStatus;
import com.busapi.modules.sales.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SalesController.class)
@Import(SecurityConfig.class)
class SalesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- SERVICE MOCK ---
    @MockitoBean
    private TicketService ticketService;

    // --- SECURITY MOCKS ---
    @MockitoBean private UserService userService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private AuthenticationProvider authenticationProvider;
    @MockitoBean(name = "userSecurity") private UserSecurity userSecurity;

    @BeforeEach
    void setup() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Koltuk durumları listelenmeli (GET /trip/{id}/seats)")
    @WithMockUser
    void getSeatStatus_ShouldReturnList() throws Exception {
        // Given
        Long tripId = 1L;
        // DTO oluştururken Lombok Builder kullanıyoruz
        SeatStatusResponse seat1 = SeatStatusResponse.builder().seatNumber(1).isOccupied(true).occupantGender(Gender.MALE).build();
        SeatStatusResponse seat2 = SeatStatusResponse.builder().seatNumber(2).isOccupied(false).build();

        when(ticketService.getSeatStatus(tripId)).thenReturn(List.of(seat1, seat2));

        // When & Then
        mockMvc.perform(get("/api/v1/sales/trip/{tripId}/seats", tripId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].seatNumber").value(1))
                // DÜZELTME: isOccupied yerine occupied yazıyoruz
                .andExpect(jsonPath("$.data[0].occupied").value(true))
                .andExpect(jsonPath("$.data[1].occupied").value(false));
    }

    @Test
    @DisplayName("Bilet satın alma başarılı olmalı")
    @WithMockUser(username = "customer@mail.com", roles = "CUSTOMER")
    void buyTicket_ShouldReturnTicketInfo() throws Exception {
        // Given
        TicketPurchaseRequest request = new TicketPurchaseRequest();
        request.setTripId(10L);
        request.setSeatNumber(5);
        request.setPassengerName("Veli");
        request.setPassengerSurname("Kısa");
        request.setPassengerTc("12345678901");
        request.setPassengerGender(Gender.MALE);

        TicketResponse response = new TicketResponse();
        response.setPnrCode("PNR123");
        response.setSeatNumber(5);
        response.setPrice(BigDecimal.valueOf(200));
        response.setStatus(TicketStatus.SOLD);

        when(ticketService.sellTicket(any(TicketPurchaseRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/sales/ticket")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pnrCode").value("PNR123"))
                .andExpect(jsonPath("$.data.status").value("SOLD"));
    }

    @Test
    @DisplayName("Geçersiz veri ile bilet alımı 400 dönmeli")
    @WithMockUser
    void buyTicket_InvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - TC No ve İsim eksik
        TicketPurchaseRequest request = new TicketPurchaseRequest();
        request.setTripId(10L);
        request.setSeatNumber(0); // Hatalı koltuk (Min 1)

        // When & Then
        mockMvc.perform(post("/api/v1/sales/ticket")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}