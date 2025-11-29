package com.busapi.modules.voyage.repository;

import com.busapi.modules.voyage.entity.Voyage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoyageRepository extends JpaRepository<Voyage, Long> {
    List<Voyage> findByRouteId(Long routeId);
}
