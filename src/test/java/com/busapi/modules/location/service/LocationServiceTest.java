package com.busapi.modules.location.service;

import com.busapi.core.exception.BusinessException;
import com.busapi.modules.location.dto.CityResponse;
import com.busapi.modules.location.dto.CreateCityRequest;
import com.busapi.modules.location.entity.City;
import com.busapi.modules.location.mapper.LocationMapper;
import com.busapi.modules.location.repository.CityRepository;
import com.busapi.modules.location.repository.DistrictRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationService locationService;

    @Test
    @DisplayName("Şehirler listelenirken repository çağrılmalı")
    void getAllCities_ShouldReturnList() {
        // Given
        City city = new City();
        city.setName("İstanbul");

        CityResponse response = new CityResponse();
        response.setName("İstanbul");

        when(cityRepository.findAllByOrderByNameAsc()).thenReturn(List.of(city));
        when(locationMapper.toCityResponseList(any())).thenReturn(List.of(response));

        // When
        List<CityResponse> result = locationService.getAllCities();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("İstanbul");
        verify(cityRepository).findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("Mükerrer plaka kodu ile şehir eklenemez")
    void createCity_WhenDuplicatePlate_ShouldThrowException() {
        // Given
        CreateCityRequest request = new CreateCityRequest();
        request.setPlateCode(34);
        request.setName("İstanbul");

        when(cityRepository.existsByPlateCode(34)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> locationService.createCity(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten kayıtlı");

        verify(cityRepository, never()).save(any());
    }
}