package com.busapi.modules.auth.controller;

import com.busapi.config.SecurityConfig;
import com.busapi.core.security.JwtAuthenticationFilter;
import com.busapi.core.security.JwtService;
import com.busapi.modules.auth.dto.AuthResponse;
import com.busapi.modules.auth.dto.LoginRequest;
import com.busapi.modules.auth.dto.RegisterRequest;
import com.busapi.modules.auth.service.AuthService;
import com.busapi.modules.identity.service.UserService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- MOCKITO BEANS ---

    @MockitoBean
    private AuthService authService;

    // SecurityConfig Bağımlılıkları
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private UserService userService; // SecurityConfig -> UserDetailsService için

    @BeforeEach
    void setup() throws Exception {
        // Filtreyi pass-through yap
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Başarılı Login testi")
    void login_ShouldReturnToken() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("test@mail.com", "123456");

        AuthResponse mockResponse = AuthResponse.builder()
                .token("dummy-jwt-token")
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .role("ROLE_CUSTOMER")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("dummy-jwt-token"));
    }

    @Test
    @DisplayName("Başarılı Register testi")
    void register_ShouldReturnToken() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Veli")
                .email("ali@mail.com")
                .password("123456")
                .phoneNumber("555")
                .build();

        AuthResponse mockResponse = AuthResponse.builder()
                .token("dummy-jwt-token")
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                .role("ROLE_CUSTOMER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("00000000-0000-0000-0000-000000000002"));
    }

    @Test
    @DisplayName("Eksik veri ile Register 400 dönmeli")
    void register_InvalidData_ShouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(); // Boş veri

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}