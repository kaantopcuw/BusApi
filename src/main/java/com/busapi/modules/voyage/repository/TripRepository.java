package com.busapi.modules.voyage.repository;

import com.busapi.core.repository.BaseRepository;
import com.busapi.modules.voyage.entity.Trip;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends BaseRepository<Trip> {
    // Belirli bir tarih ve güzergahtaki seferleri bul (Bilet arama için temel sorgu)
    @Query("SELECT t FROM Trip t WHERE t.date = :date AND t.voyage.route.departurePoint.id = :fromId AND t.voyage.route.arrivalPoint.id = :toId")
    List<Trip> searchTrips(LocalDate date, UUID fromId, UUID toId);

    // Tarih bazlı listeleme
    List<Trip> findByDate(LocalDate date);

    // Belirli bir tarihte ve voyageID'ye sahip gerçek sefer var mı?
    boolean existsByVoyageIdAndDate(UUID voyageId, LocalDate date);

    Optional<Trip> findByVoyageIdAndDate(UUID voyageId, LocalDate date);


    // O tarihteki ve rotadaki gerçek seferleri getir
    @Query("SELECT t FROM Trip t WHERE t.date = :date " +
            "AND t.voyage.route.departurePoint.id = :fromId " +
            "AND t.voyage.route.arrivalPoint.id = :toId " +
            "AND t.status <> 'CANCELLED'")
    List<Trip> findRealTrips(LocalDate date, UUID fromId, UUID toId);
}
