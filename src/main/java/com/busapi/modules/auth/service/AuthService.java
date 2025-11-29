package com.busapi.modules.auth.service;

import com.busapi.core.entity.types.UserRole;
import com.busapi.core.exception.BusinessException;
import com.busapi.core.security.JwtService;
import com.busapi.modules.auth.dto.AuthResponse;
import com.busapi.modules.auth.dto.LoginRequest;
import com.busapi.modules.auth.dto.RegisterRequest;
import com.busapi.modules.identity.dto.CreateUserRequest;
import com.busapi.modules.identity.entity.User;
import com.busapi.modules.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Gelen isteği UserService'in anlayacağı formata çevir (Role = CUSTOMER)
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setFirstName(request.getFirstName());
        createUserRequest.setLastName(request.getLastName());
        createUserRequest.setEmail(request.getEmail());
        createUserRequest.setPassword(request.getPassword()); // UserService bunu hashleyecek
        createUserRequest.setPhoneNumber(request.getPhoneNumber());
        createUserRequest.setTcNo(request.getTcNo());
        createUserRequest.setRole(UserRole.ROLE_CUSTOMER); // Public kayıtlar müşteri olur

        // 2. Kullanıcıyı oluştur (UserService email kontrolünü vs. yapar)
        // UserResponse dönüyor ama token üretmek için User entity'sine ihtiyacımız var.
        // Bu yüzden UserService'e entity dönen bir metod eklemek veya email ile tekrar sorgulamak gerekir.
        // Pratiklik açısından email ile user'ı tekrar çekelim veya UserService'i revize edelim.
        // Şimdilik kayıttan sonra email ile çekiyoruz (Entity dönen metodumuz UserService'de var: getByEmail)

        userService.createUser(createUserRequest);
        User user = userService.getByEmail(request.getEmail());

        // 3. Token üret
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // 1. Spring Security ile kimlik doğrulama (Email/Şifre kontrolü)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new BusinessException("E-posta veya şifre hatalı.", HttpStatus.UNAUTHORIZED);
        }

        // 2. Başarılıysa kullanıcıyı bul
        User user = userService.getByEmail(request.getEmail());

        // 3. Token üret
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .role(user.getRole().name())
                .build();
    }
}