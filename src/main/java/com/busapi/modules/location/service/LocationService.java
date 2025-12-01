package com.busapi.modules.location.service;

import com.busapi.core.exception.BusinessException;
import com.busapi.core.exception.ResourceNotFoundException;
import com.busapi.modules.location.dto.CityResponse;
import com.busapi.modules.location.dto.CreateCityRequest;
import com.busapi.modules.location.dto.CreateDistrictRequest;
import com.busapi.modules.location.dto.DistrictResponse;
import com.busapi.modules.location.entity.City;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.location.mapper.LocationMapper;
import com.busapi.modules.location.repository.CityRepository;
import com.busapi.modules.location.repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final LocationMapper locationMapper;

    public List<CityResponse> getAllCities() {
        return locationMapper.toCityResponseList(cityRepository.findAllByOrderByNameAsc());
    }

    public List<DistrictResponse> getDistrictsByCity(UUID cityId) {
        // Şehir var mı kontrolü (isteğe bağlı ama iyi pratik)
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("City", "id", cityId);
        }
        return locationMapper.toDistrictResponseList(districtRepository.findByCityIdOrderByNameAsc(cityId));
    }

    @Transactional
    public CityResponse createCity(CreateCityRequest request) {
        if (cityRepository.existsByPlateCode(request.getPlateCode())) {
            throw new BusinessException("Bu plaka kodu zaten kayıtlı: " + request.getPlateCode());
        }
        City city = locationMapper.toCity(request);
        return locationMapper.toCityResponse(cityRepository.save(city));
    }

    @Transactional
    public DistrictResponse createDistrict(CreateDistrictRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));

        District district = locationMapper.toDistrict(request);
        district.setCity(city);

        return locationMapper.toDistrictResponse(districtRepository.save(district));
    }
}