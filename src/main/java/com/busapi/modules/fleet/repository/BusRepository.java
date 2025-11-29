package com.busapi.modules.fleet.repository;

import com.busapi.core.repository.BaseRepository; // Senin BaseRepository'in
import com.busapi.modules.fleet.entity.Bus;
import org.springframework.stereotype.Repository;

@Repository
public interface BusRepository extends BaseRepository<Bus> {
    boolean existsByPlateNumber(String plateNumber);
}