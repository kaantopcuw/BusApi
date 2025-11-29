package com.busapi.modules.fleet.service;

import com.busapi.core.exception.BusinessException;
import com.busapi.core.exception.ResourceNotFoundException;
import com.busapi.modules.fleet.dto.BusResponse;
import com.busapi.modules.fleet.dto.CreateBusRequest;
import com.busapi.modules.fleet.entity.Bus;
import com.busapi.modules.fleet.mapper.BusMapper;
import com.busapi.modules.fleet.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;
    private final BusMapper busMapper;

    public List<BusResponse> getAllBuses() {
        return busMapper.toResponseList(busRepository.findAll());
    }

    @Transactional
    public BusResponse createBus(CreateBusRequest request) {
        // Plaka unique kontrolü
        if (busRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new BusinessException("Bu plakaya sahip bir araç zaten mevcut: " + request.getPlateNumber());
        }

        Bus bus = busMapper.toEntity(request);
        // Default aktif
        bus.setActive(true);

        return busMapper.toResponse(busRepository.save(bus));
    }

    @Transactional
    public void deleteBus(Long id) {
        if (!busRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bus", "id", id);
        }
        // BaseRepository içindeki softDelete metodunu kullanıyoruz
        busRepository.softDelete(id);
    }
}