package com.busapi.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "E-posta zorunludur")
    @Email(message = "E-posta formatı hatalı")
    private String email;

    @NotBlank(message = "Şifre zorunludur")
    private String password;
}