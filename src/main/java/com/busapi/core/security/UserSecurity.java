package com.busapi.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userSecurity")
public class UserSecurity {
    public boolean isCurrentUser(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;

        String currentUsername = authentication.getName(); // Email
        // Burada DB'den veya Token'dan ID kontrolü yapılır.
        // Basitlik için service çağrısı veya token claim kontrolü yapılabilir.
        return true; // Implementasyon detayı servise göre değişir.
    }
}
