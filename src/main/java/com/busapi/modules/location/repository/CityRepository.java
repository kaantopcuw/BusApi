package com.busapi.modules.location.repository;

import com.busapi.modules.location.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
    // İsime göre sıralı getir
    List<City> findAllByOrderByNameAsc();
    boolean existsByPlateCode(int plateCode);
}