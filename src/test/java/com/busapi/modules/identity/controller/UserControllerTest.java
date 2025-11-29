package com.busapi.modules.identity.controller;

import com.busapi.config.SecurityConfig;
import com.busapi.core.entity.types.UserRole;
import com.busapi.core.security.JwtAuthenticationFilter;
import com.busapi.core.security.JwtService;
import com.busapi.core.security.UserSecurity;
import com.busapi.modules.identity.dto.CreateUserRequest;
import com.busapi.modules.identity.dto.UserResponse;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper manuel oluşturuluyor (Dependency hatasını önlemek için)
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- MOCKITO BEANS ---

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

    // KRİTİK NOKTA: Mock filtrenin zinciri kırmamasını sağlıyoruz
    @BeforeEach
    void setup() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response); // Zinciri devam ettir!
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Admin geçerli veriyle kullanıcı oluşturduğunda 200 dönmeli")
    @WithMockUser(roles = "ADMIN")
    void createUser_WhenAdminAndValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Ali");
        request.setLastName("Veli");
        request.setEmail("ali@test.com");
        request.setPassword("123456");
        request.setPhoneNumber("5554443322");
        request.setRole(UserRole.ROLE_AGENCY_STAFF);

        UserResponse mockResponse = new UserResponse();
        mockResponse.setId(1L);
        mockResponse.setEmail("ali@test.com");
        mockResponse.setRole(UserRole.ROLE_AGENCY_STAFF);

        // When
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(mockResponse);

        // Then
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("ali@test.com"));
    }

    @Test
    @DisplayName("Yetkisiz kullanıcı (Müşteri) personel eklemeye çalışırsa 403 Forbidden dönmeli")
    @WithMockUser(roles = "CUSTOMER")
    void createUser_WhenCustomer_ShouldReturnForbidden() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Hacker");
        request.setLastName("Can");
        request.setEmail("hacker@test.com");
        request.setPassword("123");
        request.setPhoneNumber("123");
        request.setRole(UserRole.ROLE_ADMIN);

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Eksik veri gönderilirse 400 Bad Request dönmeli")
    @WithMockUser(roles = "ADMIN")
    void createUser_WhenInvalidRequest_ShouldReturnBadRequest() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setLastName("Soyad");

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("Kullanıcı kendi profilini görüntüleyebilmeli")
    @WithMockUser(username = "kendi@mail.com", roles = "CUSTOMER")
    void getUserById_WhenOwnProfile_ShouldReturnSuccess() throws Exception {
        Long userId = 100L;
        UserResponse response = new UserResponse();
        response.setId(userId);

        when(userSecurity.isCurrentUser(userId)).thenReturn(true);
        when(userService.getUserById(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId));
    }

    @Test
    @DisplayName("Kullanıcı başkasının profilini görüntülemeye çalışırsa 403 almalı")
    @WithMockUser(username = "hacker@mail.com", roles = "CUSTOMER")
    void getUserById_WhenOtherProfile_ShouldReturnForbidden() throws Exception {
        Long targetUserId = 200L;

        when(userSecurity.isCurrentUser(targetUserId)).thenReturn(false);

        mockMvc.perform(get("/api/v1/users/{id}", targetUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}