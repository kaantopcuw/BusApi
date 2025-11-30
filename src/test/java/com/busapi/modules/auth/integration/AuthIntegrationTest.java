package com.busapi.modules.auth.integration;

import com.busapi.core.entity.types.UserRole;
import com.busapi.modules.auth.dto.LoginRequest;
import com.busapi.modules.auth.dto.RegisterRequest;
import com.busapi.modules.identity.entity.User;
import com.busapi.modules.identity.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // DEĞİŞİKLİK: Autowired yerine manuel oluşturuyoruz.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Tam Akış: Yeni kullanıcı kayıt olabilmeli ve token almalı")
    void register_ShouldSaveUserAndReturnToken() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Yeni");
        request.setLastName("Müşteri");
        request.setEmail("newcustomer@mail.com");
        request.setPassword("123456");
        request.setPhoneNumber("5551234567");
        request.setTcNo("11111111111");

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.role", is("ROLE_CUSTOMER")));

        // DB Kontrol
        User savedUser = userRepository.findByEmail("newcustomer@mail.com").orElseThrow();
        assertThat(savedUser.getFirstName()).isEqualTo("Yeni");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(passwordEncoder.matches("123456", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Tam Akış: Kayıtlı kullanıcı doğru şifreyle giriş yapabilmeli")
    void login_WithCorrectCredentials_ShouldReturnToken() throws Exception {
        // Given (DB Hazırlığı)
        User user = new User();
        user.setFirstName("Login");
        user.setLastName("Test");
        user.setEmail("login@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setPhoneNumber("555");
        user.setRole(UserRole.ROLE_AGENCY_STAFF);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("login@test.com", "password123");

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.role", is("ROLE_AGENCY_STAFF")));
    }

    @Test
    @DisplayName("Hata Senaryosu: Yanlış şifre ile giriş yapılamamalı")
    void login_WithWrongPassword_ShouldReturnUnauthorized() throws Exception {
        // Given
        User user = new User();
        user.setFirstName("Wrong");
        user.setLastName("Pass");
        user.setEmail("wrongpass@test.com");
        user.setPassword(passwordEncoder.encode("correctPassword"));
        user.setPhoneNumber("555");
        user.setRole(UserRole.ROLE_CUSTOMER);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("wrongpass@test.com", "WRONG_PASSWORD");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Validasyon: Hatalı email formatı ile kayıt engellenmeli")
    void register_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Bad");
        request.setLastName("Email");
        request.setEmail("bu-bir-email-degil");
        request.setPassword("123456");
        request.setPhoneNumber("555");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }
}