package com.busapi.modules.location.repository;

import com.busapi.modules.location.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Long> {
    // Bir şehrin ilçelerini getir
    List<District> findByCityIdOrderByNameAsc(Long cityId);
}