package com.busapi.modules.identity.controller;

import com.busapi.core.result.ApiResponse;
import com.busapi.modules.identity.dto.CreateUserRequest;
import com.busapi.modules.identity.dto.UserHistoryResponse;
import com.busapi.modules.identity.dto.UserResponse;
import com.busapi.modules.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Sadece Adminler yeni personel/kullanıcı ekleyebilir (Backoffice ekranı için)
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(userService.createUser(request), "Kullanıcı başarıyla oluşturuldu.");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ApiResponse<UserResponse> getUserById(@PathVariable UUID id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("@userSecurity.isCurrentUser(#id) or hasRole('ROLE_ADMIN')")
    public ApiResponse<UserHistoryResponse> getUserHistory(@PathVariable UUID id) {
        return ApiResponse.success(userService.getUserHistory(id));
    }
}