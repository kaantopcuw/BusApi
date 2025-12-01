package com.busapi.modules.identity.service;

import com.busapi.core.exception.BusinessException;
import com.busapi.core.exception.ResourceNotFoundException;
import com.busapi.modules.identity.dto.AgencyResponse;
import com.busapi.modules.identity.dto.CreateAgencyRequest;
import com.busapi.modules.identity.entity.Agency;
import com.busapi.modules.identity.mapper.AgencyMapper;
import com.busapi.modules.identity.repository.AgencyRepository;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.location.repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final DistrictRepository districtRepository;
    private final AgencyMapper agencyMapper;

    @Transactional
    public AgencyResponse createAgency(CreateAgencyRequest request) {
        // 1. İsim Kontrolü (Unique)
        if (agencyRepository.existsByName(request.getName())) {
            throw new BusinessException("Bu isimde bir acenta zaten mevcut: " + request.getName());
        }

        // 2. Lokasyon (İlçe) Kontrolü
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", request.getDistrictId()));

        // 3. Entity Dönüşümü ve Set İşlemleri
        Agency agency = agencyMapper.toEntity(request);
        agency.setLocation(district);
        agency.setActive(true); // Varsayılan olarak aktif başlar

        // 4. Kayıt
        Agency savedAgency = agencyRepository.save(agency);

        // 5. Response Dönüşü
        return agencyMapper.toResponse(savedAgency);
    }

    // AgencyService içine
    public List<AgencyResponse> getAllAgencies() {
        return agencyRepository.findAll().stream()
                .map(agencyMapper::toResponse)
                .toList();
    }
}
