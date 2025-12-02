package com.busapi.core.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class XssProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldSanitizeJsonBody() throws Exception {
        String xssPayload = "{\"input\": \"<script>alert(1)</script>SafeText\"}";

        mockMvc.perform(post("/api/xss-test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(xssPayload))
                .andExpect(status().isOk())
                // Beklenen: Script tagleri silinmiş, sadece "SafeText" kalmış olmalı
                .andExpect(jsonPath("$.message").value("Sunucunun Aldığı Veri: SafeText"));
    }
}

