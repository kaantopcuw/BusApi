package com.busapi.modules.voyage.service;

import com.busapi.core.entity.types.UserRole;
import com.busapi.core.exception.BusinessException;
import com.busapi.modules.identity.entity.User;
import com.busapi.modules.identity.service.UserService;
import com.busapi.modules.voyage.entity.Trip;
import com.busapi.modules.voyage.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoyageServiceCrewTest {

    @Mock private TripRepository tripRepository;
    @Mock private UserService userService; // User entity dönen mock

    // Diğer gerekli mocklar (RouteRepository vs.) null kalabilir çünkü sadece assignCrew test ediyoruz
    @InjectMocks private VoyageService voyageService;

    @Test
    @DisplayName("Atanan kişi Şoför rolünde değilse hata fırlatmalı")
    void assignCrew_InvalidDriverRole_ThrowsException() {
        // Given
        UUID tripId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID driverId = UUID.fromString("00000000-0000-0000-0000-000000000100");
        UUID hostId = UUID.fromString("00000000-0000-0000-0000-000000000200");

        Trip trip = new Trip();

        User fakeDriver = new User();
        fakeDriver.setRole(UserRole.ROLE_CUSTOMER); // Yanlış Rol!

        User host = new User();
        host.setRole(UserRole.ROLE_HOST);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userService.findUserEntityById(driverId)).thenReturn(fakeDriver);
        when(userService.findUserEntityById(hostId)).thenReturn(host);

        // When & Then
        assertThatThrownBy(() -> voyageService.assignCrewToTrip(tripId, driverId, hostId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Şoför değil");

        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doğru rollerle atama başarılı olmalı")
    void assignCrew_Success() {
        // Given
        UUID tripId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID driverId = UUID.fromString("00000000-0000-0000-0000-000000000100");
        UUID hostId = UUID.fromString("00000000-0000-0000-0000-000000000200");

        Trip trip = new Trip();
        User driver = new User(); driver.setRole(UserRole.ROLE_DRIVER);
        User host = new User(); host.setRole(UserRole.ROLE_HOST);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userService.findUserEntityById(driverId)).thenReturn(driver);
        when(userService.findUserEntityById(hostId)).thenReturn(host);

        // When
        voyageService.assignCrewToTrip(tripId, driverId, hostId);

        // Then
        verify(tripRepository).save(trip);
    }
}