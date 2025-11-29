package com.busapi.modules.identity.dto;

import com.busapi.core.entity.types.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Ad zorunludur")
    private String firstName;

    @NotBlank(message = "Soyad zorunludur")
    private String lastName;

    @Email(message = "Geçerli bir e-posta giriniz")
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String phoneNumber;

    private String tcNo;

    @NotNull
    private UserRole role;

    // Eğer personel ise acenta ID'si
    private Long agencyId;
}




