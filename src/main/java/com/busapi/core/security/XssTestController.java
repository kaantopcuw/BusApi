package com.busapi.core.security;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/xss-test")
public class XssTestController {

    // 1. JSON Body Testi İçin Endpoint
    @PostMapping("/body")
    public TestResponse testBody(@RequestBody TestRequest request) {
        // Gelen veri temizlenmiş olmalı (global XSS modülü tarafından)
        return new TestResponse("Sunucunun Aldığı Veri: " + request.getInput());
    }

    // 2. Query Param Testi İçin Endpoint (Filter çalışıyor mu?)
    @GetMapping("/param")
    public String testParam(@RequestParam String query) {
        return "Sunucunun Aldığı Parametre: " + query;
    }

    // Basit DTO'lar

    public static class TestRequest {
        private String input;
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }
    }

    public static class TestResponse {
        private String message;
        public TestResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}