package com.busapi.modules.sales.controller;

import com.busapi.config.SecurityConfig;
import com.busapi.core.security.JwtAuthenticationFilter;
import com.busapi.core.security.JwtService;
import com.busapi.core.security.UserSecurity;
import com.busapi.modules.identity.service.UserService;
import com.busapi.modules.sales.dto.CreateOrderRequest;
import com.busapi.modules.sales.dto.OrderResponse;
import com.busapi.modules.sales.dto.PaymentInfoRequest;
import com.busapi.modules.sales.dto.TicketRequestItem;
import com.busapi.modules.sales.enums.Gender;
import com.busapi.modules.sales.enums.PaymentType;
import com.busapi.modules.sales.service.OrderService;
import com.busapi.modules.sales.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private OrderService orderService;
    @MockitoBean private TicketService ticketService; // Controller dependency

    // Security Mocks
    @MockitoBean private UserService userService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private AuthenticationProvider authenticationProvider;
    @MockitoBean(name = "userSecurity") private UserSecurity userSecurity;

    @BeforeEach
    void setup() throws Exception {
        doAnswer(i -> {
            ((FilterChain) i.getArgument(2)).doFilter(i.getArgument(0), i.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Geçerli sipariş isteği 200 dönmeli")
    @WithMockUser
    void createOrder_ShouldReturnSuccess() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setTripId(10L);
        request.setContactEmail("a@b.com");
        request.setContactPhone("555");

        // --- EKSİK OLAN KISIM ---
        PaymentInfoRequest paymentInfo = new PaymentInfoRequest();
        paymentInfo.setPaymentType(PaymentType.CREDIT_CARD);
        paymentInfo.setTransactionId("iyzico-123");
        request.setPaymentInfo(paymentInfo);
        // -------------------------

        TicketRequestItem item = new TicketRequestItem();
        item.setSeatNumber(1);
        item.setPassengerName("Ali");
        item.setPassengerSurname("Veli");
        item.setPassengerTc("111");
        item.setPassengerGender(Gender.MALE);
        request.setTickets(List.of(item));

        OrderResponse response = new OrderResponse();
        response.setOrderPnr("PNR123");
        response.setTotalPrice("100.00");

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/sales/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderPnr").value("PNR123"));
    }
}