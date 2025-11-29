package com.busapi.modules.voyage.controller;

import com.busapi.config.SecurityConfig;
import com.busapi.core.security.JwtAuthenticationFilter;
import com.busapi.core.security.JwtService;
import com.busapi.core.security.UserSecurity;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.identity.service.UserService;
import com.busapi.modules.voyage.dto.CreateRouteRequest;
import com.busapi.modules.voyage.dto.CreateVoyageRequest;
import com.busapi.modules.voyage.dto.TripResponse;
import com.busapi.modules.voyage.service.VoyageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoyageController.class)
@Import(SecurityConfig.class)
class VoyageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // LocalDate/Time serileştirme hatası almamak için JavaTimeModule ekliyoruz
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // --- SERVICE MOCK ---
    @MockitoBean
    private VoyageService voyageService;

    // --- SECURITY MOCKS ---
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private AuthenticationProvider authenticationProvider;
    @MockitoBean(name = "userSecurity")
    private UserSecurity userSecurity;

    @BeforeEach
    void setup() throws Exception {
        // Filtre zincirini kırmamak için pass-through ayarı
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Admin yeni bir güzergah (Route) oluşturabilmeli")
    @WithMockUser(roles = "ADMIN")
    void createRoute_WhenAdmin_ShouldReturnSuccess() throws Exception {
        // Given
        CreateRouteRequest request = new CreateRouteRequest();
        request.setName("Istanbul - Ankara");
        request.setDepartureDistrictId(1L);
        request.setArrivalDistrictId(2L);
        // Stops opsiyonel, null bırakabiliriz veya boş liste verebiliriz

        when(voyageService.createRoute(any(CreateRouteRequest.class))).thenReturn(10L);

        // When & Then
        mockMvc.perform(post("/api/v1/voyages/routes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    @DisplayName("Müşteri güzergah oluşturmaya çalışırsa 403 almalı")
    @WithMockUser(roles = "CUSTOMER")
    void createRoute_WhenCustomer_ShouldReturnForbidden() throws Exception {
        // Given - Validasyon hatası almamak için tüm zorunlu alanları dolduruyoruz
        CreateRouteRequest request = new CreateRouteRequest();
        request.setName("Test Route");
        request.setDepartureDistrictId(1L); // EKLENDİ
        request.setArrivalDistrictId(2L);   // EKLENDİ

        // When & Then
        mockMvc.perform(post("/api/v1/voyages/routes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // Artık validasyonu geçip yetkiye takılacak
    }

    @Test
    @DisplayName("Admin yeni bir sefer şablonu (Voyage Definition) oluşturabilmeli")
    @WithMockUser(roles = "ADMIN")
    void createVoyageDefinition_ShouldReturnSuccess() throws Exception {
        // Given
        CreateVoyageRequest request = new CreateVoyageRequest();
        request.setRouteId(10L);
        request.setDepartureTime(LocalTime.of(14, 0));
        request.setBusType(BusType.SUITE_2_1);
        request.setBasePrice(BigDecimal.valueOf(500));

        when(voyageService.createVoyageDefinition(any(CreateVoyageRequest.class))).thenReturn(55L);

        // When & Then
        mockMvc.perform(post("/api/v1/voyages/definitions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(55));
    }

    @Test
    @DisplayName("Admin belirli bir tarih için seferleri generate edebilmeli")
    @WithMockUser(roles = "ADMIN")
    void generateTrips_ShouldReturnSuccess() throws Exception {
        LocalDate date = LocalDate.of(2025, 12, 1);

        mockMvc.perform(post("/api/v1/voyages/trips/generate")
                        .with(csrf())
                        .param("date", date.toString())) // 2025-12-01 formatında gider
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Seferler oluşturuldu."));
    }

    @Test
    @DisplayName("Herkes (Public) sefer araması yapabilmeli")
    @WithMockUser // Role fark etmez, hatta @WithMockUser olmasa da (permitAll) çalışmalı ama test context için güvenli
    void searchTrips_ShouldReturnList() throws Exception {
        // Given
        LocalDate searchDate = LocalDate.of(2025, 12, 1);
        Long fromId = 1L;
        Long toId = 2L;

        TripResponse trip = new TripResponse();
        trip.setId(100L);
        trip.setRouteName("Ist - Ank");
        trip.setPrice(BigDecimal.valueOf(450));

        when(voyageService.searchTrips(eq(searchDate), eq(fromId), eq(toId)))
                .thenReturn(List.of(trip));

        // When & Then
        mockMvc.perform(get("/api/v1/voyages/trips/search")
                        .param("date", searchDate.toString())
                        .param("fromId", fromId.toString())
                        .param("toId", toId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(100))
                .andExpect(jsonPath("$.data[0].routeName").value("Ist - Ank"));
    }
}