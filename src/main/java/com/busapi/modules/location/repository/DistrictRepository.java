package com.busapi.modules.location.repository;

import com.busapi.modules.location.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DistrictRepository extends JpaRepository<District, UUID> {
    // Bir şehrin ilçelerini getir
    List<District> findByCityIdOrderByNameAsc(UUID cityId);
}