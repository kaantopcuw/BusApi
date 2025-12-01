package com.busapi.modules.identity.service;

import com.busapi.core.exception.BusinessException;
import com.busapi.modules.identity.dto.AgencyResponse;
import com.busapi.modules.identity.dto.CreateAgencyRequest;
import com.busapi.modules.identity.entity.Agency;
import com.busapi.modules.identity.mapper.AgencyMapper;
import com.busapi.modules.identity.repository.AgencyRepository;
import com.busapi.modules.location.entity.City;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.location.repository.DistrictRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgencyServiceTest {

    @Mock private AgencyRepository agencyRepository;
    @Mock private DistrictRepository districtRepository;
    @Mock private AgencyMapper agencyMapper;

    @InjectMocks private AgencyService agencyService;

    @Test
    @DisplayName("Aynı isimde acenta varsa hata fırlatmalı")
    void createAgency_DuplicateName_ThrowsException() {
        CreateAgencyRequest request = new CreateAgencyRequest();
        request.setName("Var Olan Acenta");

        when(agencyRepository.existsByName("Var Olan Acenta")).thenReturn(true);

        assertThatThrownBy(() -> agencyService.createAgency(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten mevcut");

        verify(agencyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Başarılı acenta oluşturma")
    void createAgency_Success() {
        CreateAgencyRequest request = new CreateAgencyRequest();
        request.setName("Yeni Acenta");
        request.setDistrictId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        District district = new District();
        district.setName("İlçe");
        district.setCity(new City()); // Null check yememek için

        Agency agency = new Agency();
        agency.setName("Yeni Acenta");

        AgencyResponse response = new AgencyResponse();
        response.setName("Yeni Acenta");

        when(agencyRepository.existsByName(any())).thenReturn(false);
        when(districtRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000001"))).thenReturn(Optional.of(district));
        when(agencyMapper.toEntity(request)).thenReturn(agency);
        when(agencyRepository.save(agency)).thenReturn(agency);
        when(agencyMapper.toResponse(agency)).thenReturn(response);

        AgencyResponse result = agencyService.createAgency(request);
        assertThat(result.getName()).isEqualTo("Yeni Acenta");
    }
}