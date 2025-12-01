package com.busapi.modules.voyage.repository;

import com.busapi.modules.voyage.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    // Belirli bir tarih ve güzergahtaki seferleri bul (Bilet arama için temel sorgu)
    @Query("SELECT t FROM Trip t WHERE t.date = :date AND t.voyage.route.departurePoint.id = :fromId AND t.voyage.route.arrivalPoint.id = :toId")
    List<Trip> searchTrips(LocalDate date, UUID fromId, UUID toId);

    // Tarih bazlı listeleme
    List<Trip> findByDate(LocalDate date);
}
