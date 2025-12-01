package com.busapi.modules.voyage.repository;

import com.busapi.modules.voyage.entity.Voyage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VoyageRepository extends JpaRepository<Voyage, UUID> {
    List<Voyage> findByRouteId(UUID routeId);
}
