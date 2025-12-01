package com.busapi.modules.identity.dto;

import com.busapi.core.entity.types.UserRole;
import lombok.Data;

import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private String agencyName; // Detaylı obje yerine sadece isim yeterli olabilir
}