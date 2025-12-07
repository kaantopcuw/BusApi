package com.busapi.modules.voyage.repository;

import com.busapi.modules.voyage.entity.Voyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VoyageRepository extends JpaRepository<Voyage, UUID> {
    List<Voyage> findByRouteId(UUID routeId);


    @Query(value = "SELECT v.* FROM voyages v " +
            "JOIN routes r ON v.route_id = r.id " +
            "WHERE r.departure_district_id = :fromId " +
            "AND r.arrival_district_id = :toId " +
            "AND v.is_active = true " +
            "AND v.is_deleted = false " + // Native olduğu için soft-delete'i elle ekledik
            "AND v.valid_from <= :date AND v.valid_to >= :date " +
            "AND v.days_of_week LIKE %:dayName%",
            nativeQuery = true)
    List<Voyage> findCandidateVoyages(LocalDate date, String dayName, UUID fromId, UUID toId);

}
