package com.test.busapi.core.exception;

// core/exception/GlobalExceptionHandler.java

import com.test.busapi.core.result.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Bizim fırlattığımız iş mantığı hataları
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business Exception: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), ex.getHttpStatus());
    }

    // Bulunamadı hataları
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Not Found: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    // @Valid anotasyonu ile gelen DTO validasyon hataları
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation Error: {}", errors);
        return new ResponseEntity<>(
                ApiResponse.error("Validasyon hatası", errors),
                HttpStatus.BAD_REQUEST
        );
    }

    // Authentication Hataları (Security)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return new ResponseEntity<>(ApiResponse.error("Kimlik doğrulama başarısız: " + ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    // Access Denied (Yetkisiz Erişim)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(ApiResponse.error("Bu işlem için yetkiniz yok."), HttpStatus.FORBIDDEN);
    }

    // Beklenmeyen diğer tüm hatalar (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        log.error("Unexpected Error: ", ex);
        return new ResponseEntity<>(ApiResponse.error("Sunucu tarafında beklenmeyen bir hata oluştu."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}