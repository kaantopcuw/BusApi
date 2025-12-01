package com.busapi.modules.fleet.service;

import com.busapi.core.exception.BusinessException;
import com.busapi.modules.fleet.dto.BusResponse;
import com.busapi.modules.fleet.dto.CreateBusRequest;
import com.busapi.modules.fleet.entity.Bus;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.fleet.mapper.BusMapper;
import com.busapi.modules.fleet.repository.BusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusServiceTest {

    @Mock
    private BusRepository busRepository;

    @Mock
    private BusMapper busMapper;

    @InjectMocks
    private BusService busService;

    @Test
    @DisplayName("Benzersiz plaka ile otobüs başarıyla oluşturulmalı")
    void createBus_WhenUniquePlate_ShouldCreate() {
        // Given
        CreateBusRequest request = new CreateBusRequest();
        request.setPlateNumber("34 ABC 123");
        request.setBusType(BusType.SUITE_2_1);
        request.setSeatCapacity(30);

        Bus busEntity = new Bus();
        busEntity.setPlateNumber("34 ABC 123");

        BusResponse response = new BusResponse();
        response.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        response.setPlateNumber("34 ABC 123");

        when(busRepository.existsByPlateNumber(request.getPlateNumber())).thenReturn(false);
        when(busMapper.toEntity(request)).thenReturn(busEntity);
        when(busRepository.save(busEntity)).thenReturn(busEntity);
        when(busMapper.toResponse(busEntity)).thenReturn(response);

        // When
        BusResponse result = busService.createBus(request);

        // Then
        assertThat(result.getPlateNumber()).isEqualTo("34 ABC 123");
        verify(busRepository).save(busEntity);
    }

    @Test
    @DisplayName("Var olan plaka ile kayıt yapılmaya çalışılırsa hata fırlatmalı")
    void createBus_WhenDuplicatePlate_ShouldThrowException() {
        // Given
        CreateBusRequest request = new CreateBusRequest();
        request.setPlateNumber("34 ABC 123");

        when(busRepository.existsByPlateNumber(request.getPlateNumber())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> busService.createBus(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten mevcut");

        verify(busRepository, never()).save(any());
    }
}