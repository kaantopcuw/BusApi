package com.busapi.modules.voyage.integration;

import com.busapi.modules.fleet.entity.Bus;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.fleet.repository.BusRepository;
import com.busapi.modules.location.entity.City;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.location.repository.CityRepository;
import com.busapi.modules.location.repository.DistrictRepository;
import com.busapi.modules.voyage.dto.CreateRouteRequest;
import com.busapi.modules.voyage.dto.CreateStopRequest;
import com.busapi.modules.voyage.dto.CreateVoyageRequest;
import com.busapi.modules.voyage.dto.TripResponse;
import com.busapi.modules.voyage.entity.Route;
import com.busapi.modules.voyage.entity.Trip;
import com.busapi.modules.voyage.enums.TripStatus;
import com.busapi.modules.voyage.repository.RouteRepository;
import com.busapi.modules.voyage.repository.TripRepository;
import com.busapi.modules.voyage.repository.VoyageRepository;
import com.busapi.modules.voyage.service.VoyageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // application-test.properties (H2 DB)
@Transactional // Her test sonrası rollback
class VoyageIntegrationTest {

    @Autowired private VoyageService voyageService;

    // Doğrulama ve Setup için Repository'ler
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private BusRepository busRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private VoyageRepository voyageRepository;
    @Autowired private TripRepository tripRepository;

    private Long districtIdIstanbul;
    private Long districtIdAnkara;
    private Long districtIdBolu; // Ara durak
    private Long busId;

    @BeforeEach
    void setup() {
        // 1. Şehir ve İlçeleri Oluştur
        City ist = new City(); ist.setName("İstanbul"); ist.setPlateCode(34);
        cityRepository.save(ist);
        District distIst = new District(); distIst.setName("Esenler"); distIst.setCity(ist);
        districtIdIstanbul = districtRepository.save(distIst).getId();

        City ank = new City(); ank.setName("Ankara"); ank.setPlateCode(6);
        cityRepository.save(ank);
        District distAnk = new District(); distAnk.setName("Aşti"); distAnk.setCity(ank);
        districtIdAnkara = districtRepository.save(distAnk).getId();

        City bolu = new City(); bolu.setName("Bolu"); bolu.setPlateCode(14);
        cityRepository.save(bolu);
        District distBolu = new District(); distBolu.setName("Merkez"); distBolu.setCity(bolu);
        districtIdBolu = districtRepository.save(distBolu).getId();

        // 2. Otobüs Oluştur
        Bus bus = new Bus();
        bus.setPlateNumber("34 BUS 34");
        bus.setBusType(BusType.SUITE_2_1);
        bus.setSeatCapacity(30);
        busId = busRepository.save(bus).getId();
    }

    @Test
    @DisplayName("Tam Akış: Rota -> Şablon -> Sefer Üretimi -> Arama -> Otobüs Atama")
    void fullVoyageFlowTest() {
        // --- ADIM 1: ROTA OLUŞTURMA (Service üzerinden) ---
        CreateRouteRequest routeReq = new CreateRouteRequest();
        routeReq.setName("İstanbul - Ankara (Bolu Üzerinden)");
        routeReq.setDepartureDistrictId(districtIdIstanbul);
        routeReq.setArrivalDistrictId(districtIdAnkara);

        // Ara durak ekle
        CreateStopRequest stopReq = new CreateStopRequest();
        stopReq.setDistrictId(districtIdBolu);
        stopReq.setStopOrder(1);
        stopReq.setKmFromStart(300);
        stopReq.setDurationMinutesFromStart(180);
        routeReq.setStops(List.of(stopReq));

        Long routeId = voyageService.createRoute(routeReq);

        // Doğrulama
        assertThat(routeId).isNotNull();
        Route savedRoute = routeRepository.findById(routeId).orElseThrow();
        assertThat(savedRoute.getStops()).hasSize(1);
        assertThat(savedRoute.getStops().get(0).getDistrict().getId()).isEqualTo(districtIdBolu);

        // --- ADIM 2: SEFER ŞABLONU (VOYAGE) OLUŞTURMA ---
        CreateVoyageRequest voyageReq = new CreateVoyageRequest();
        voyageReq.setRouteId(routeId);
        voyageReq.setDepartureTime(LocalTime.of(14, 30));
        voyageReq.setBusType(BusType.SUITE_2_1);
        voyageReq.setBasePrice(BigDecimal.valueOf(500));

        Long voyageId = voyageService.createVoyageDefinition(voyageReq);

        // Doğrulama
        assertThat(voyageId).isNotNull();
        assertThat(voyageRepository.count()).isEqualTo(1);

        // --- ADIM 3: SEFERLERİ OLUŞTUR (GENERATE TRIPS) ---
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        voyageService.generateTripsForDate(tomorrow);

        // Doğrulama
        List<Trip> trips = tripRepository.findAll();
        assertThat(trips).hasSize(1); // 1 şablon vardı, 1 sefer oluşmalı
        Trip trip = trips.get(0);
        assertThat(trip.getDate()).isEqualTo(tomorrow);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.SCHEDULED);
        assertThat(trip.getBus()).isNull(); // Henüz otobüs atanmadı

        // --- ADIM 4: SEFER ARAMA ---
        List<TripResponse> searchResults = voyageService.searchTrips(tomorrow, districtIdIstanbul, districtIdAnkara);

        // Doğrulama
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getId()).isEqualTo(trip.getId());
        assertThat(searchResults.get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(500));

        // --- ADIM 5: OTOBÜS ATAMA ---
        voyageService.assignBusToTrip(trip.getId(), busId);

        // Doğrulama
        Trip updatedTrip = tripRepository.findById(trip.getId()).orElseThrow();
        assertThat(updatedTrip.getBus()).isNotNull();
        assertThat(updatedTrip.getBus().getPlateNumber()).isEqualTo("34 BUS 34");
    }
}