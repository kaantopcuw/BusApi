package com.busapi.modules.voyage.repository;

import com.busapi.core.repository.BaseRepository;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.voyage.entity.Route;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RouteRepository extends BaseRepository<Route> {

    // 1. Sadece rotası tanımlanmış KALKIŞ noktalarını getir (Tekrarsız)
    // Join kullanarak N+1 sorununu önlüyoruz
    @Query("SELECT DISTINCT r.departurePoint FROM Route r JOIN FETCH r.departurePoint.city ORDER BY r.departurePoint.city.name, r.departurePoint.name")
    List<District> findDistinctDeparturePoints();

    // 2. Seçilen kalkış noktasına göre gidilebilecek VARIŞ noktalarını getir
    @Query("SELECT DISTINCT r.arrivalPoint FROM Route r JOIN FETCH r.arrivalPoint.city WHERE r.departurePoint.id = :departureId ORDER BY r.arrivalPoint.city.name, r.arrivalPoint.name")
    List<District> findDistinctArrivalPointsByDepartureId(Long departureId);

//    // Tüm rotaları, ilişkili şehir verileriyle birlikte (N+1 olmasın diye) çek
//    @Query("SELECT r FROM Route r " +
//            "JOIN FETCH r.departurePoint dp JOIN FETCH dp.city " +
//            "JOIN FETCH r.arrivalPoint ap JOIN FETCH ap.city " +
//            "ORDER BY dp.city.name, dp.name")
//    List<Route> findAllRoutesWithDetails();

    // Durakları (stops) ve onların bağlı olduğu ilçeleri (district) ve şehirleri (city) de çekiyoruz.
    // distinct ekliyoruz çünkü join işlemi satır sayısını çoğaltır.
    @Query("SELECT DISTINCT r FROM Route r " +
            "JOIN FETCH r.departurePoint dp JOIN FETCH dp.city " +
            "JOIN FETCH r.arrivalPoint ap JOIN FETCH ap.city " +
            "LEFT JOIN FETCH r.stops s " +             // Durakları çek
            "LEFT JOIN FETCH s.district sd " +         // Durak ilçelerini çek
            "LEFT JOIN FETCH sd.city " +               // Durak şehirlerini çek
            "ORDER BY dp.city.name, dp.name")
    List<Route> findAllRoutesWithDetails();
}

