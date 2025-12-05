package com.busapi.modules.voyage.bootstrap;

import com.busapi.core.exception.ResourceNotFoundException;
import com.busapi.modules.fleet.entity.Bus;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.fleet.repository.BusRepository;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.location.repository.DistrictRepository;
import com.busapi.modules.voyage.entity.Route;
import com.busapi.modules.voyage.entity.RouteStop;
import com.busapi.modules.voyage.entity.Trip;
import com.busapi.modules.voyage.entity.Voyage;
import com.busapi.modules.voyage.enums.TripStatus;
import com.busapi.modules.voyage.repository.RouteRepository;
import com.busapi.modules.voyage.repository.TripRepository;
import com.busapi.modules.voyage.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("!test") // Testlerde çalışmasın
@Order(2) // LocationDataSeeder(1) çalıştıktan sonra çalışsın
@RequiredArgsConstructor
public class VoyageDataSeeder implements CommandLineRunner {

    private final DistrictRepository districtRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final VoyageRepository voyageRepository;
    private final TripRepository tripRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (routeRepository.count() > 0) {
            log.info("Voyage data already seeded.");
            return;
        }

        log.info("Seeding Voyage (Route, Bus, Trip) data...");

        try {
            // 1. GEREKLİ LOKASYONLARI BUL (İsimle arıyoruz)
            // Not: LocationDataSeeder bunları veritabanına basmış olmalı.
            District enez = findDistrict("Enez");
            District kesan = findDistrict("Keşan");
            District tekirdag = findDistrict("Süleymanpaşa"); // Tekirdağ Merkez
            District silivri = findDistrict("Silivri");
            District esenler = findDistrict("Esenler"); // İstanbul Avrupa

            // 2. ROTA OLUŞTUR (Enez -> İstanbul Hattı)
            Route route = new Route();
            route.setName("Enez - Keşan - Tekirdağ - İstanbul (Esenler)");
            route.setDeparturePoint(enez);
            route.setArrivalPoint(esenler);

            // Durakları Ekle
            List<RouteStop> stops = new ArrayList<>();

            // Durak 1: Keşan (+60km, +45dk)
            stops.add(createStop(route, kesan, 1, 60, 45));

            // Durak 2: Tekirdağ (+140km, +120dk)
            stops.add(createStop(route, tekirdag, 2, 140, 120));

            // Durak 3: Silivri (+200km, +180dk)
            stops.add(createStop(route, silivri, 3, 200, 180));

            route.setStops(stops);
            routeRepository.save(route);
            log.info("Route created: {}", route.getName());

            // 3. OTOBÜSLERİ OLUŞTUR (Filo)
            // 3 Tane 2+2 (Standart), 2 Tane 2+1 (Suit)
            List<Bus> fleet = new ArrayList<>();
            fleet.add(createBus("22 ENZ 101", BusType.STANDARD_2_2, 46));
            fleet.add(createBus("22 KSN 202", BusType.STANDARD_2_2, 46));
            fleet.add(createBus("34 IST 303", BusType.STANDARD_2_2, 46));
            fleet.add(createBus("59 TKR 404", BusType.SUITE_2_1, 30)); // Suit
            fleet.add(createBus("34 VIP 505", BusType.SUITE_2_1, 30)); // Suit

            busRepository.saveAll(fleet);

            // 4. SEFER ŞABLONLARI (VOYAGE) OLUŞTUR
            // Günde 5 Sefer
            List<Voyage> voyages = new ArrayList<>();
            voyages.add(createVoyage(route, LocalTime.of(8, 0), BusType.STANDARD_2_2, 400));
            voyages.add(createVoyage(route, LocalTime.of(11, 0), BusType.SUITE_2_1, 550)); // VIP
            voyages.add(createVoyage(route, LocalTime.of(14, 0), BusType.STANDARD_2_2, 400));
            voyages.add(createVoyage(route, LocalTime.of(17, 30), BusType.SUITE_2_1, 600)); // VIP Akşam
            voyages.add(createVoyage(route, LocalTime.of(21, 0), BusType.STANDARD_2_2, 350)); // Gece

            voyageRepository.saveAll(voyages);

            // 5. GERÇEK SEFERLERİ (TRIP) OLUŞTUR (Önümüzdeki 3 gün için)
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 3; i++) { // Bugün, Yarın, Sonraki gün
                LocalDate tripDate = today.plusDays(i);

                for (int j = 0; j < voyages.size(); j++) {
                    Voyage v = voyages.get(j);
                    Bus assignedBus = fleet.get(j % fleet.size()); // Filodaki otobüsleri sırayla ata

                    // Otobüs tipi uyuşmazsa standart ata (Basit mantık)
                    if(assignedBus.getBusType() != v.getBusType()) {
                        assignedBus = fleet.stream()
                                .filter(b -> b.getBusType() == v.getBusType())
                                .findFirst().orElse(assignedBus);
                    }

                    Trip trip = new Trip();
                    trip.setVoyage(v);
                    trip.setDate(tripDate);
                    trip.setDepartureDateTime(LocalDateTime.of(tripDate, v.getDepartureTime()));
                    trip.setBus(assignedBus);
                    trip.setStatus(TripStatus.SCHEDULED);

                    tripRepository.save(trip);
                }
            }

            log.info("Trips generated for next 3 days.");

        } catch (Exception e) {
            log.error("Error seeding voyage data: ", e);
        }
    }

    // --- YARDIMCI METODLAR ---

    private District findDistrict(String name) {
        // Gerçek projede ilçe isimleri benzersiz olmayabilir (Örn: Merkez),
        // bu yüzden City ile aramak daha doğru olur ama şimdilik isimle bulalım.
        // LocationDataSeeder'da isimleri unique varsayıyoruz.
        return districtRepository.findAll().stream()
                .filter(d -> d.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("District not found: " + name, "name", name));
    }

    private RouteStop createStop(Route route, District district, int order, int km, int minutes) {
        RouteStop stop = new RouteStop();
        stop.setRoute(route);
        stop.setDistrict(district);
        stop.setStopOrder(order);
        stop.setKmFromStart(km);
        stop.setDurationMinutesFromStart(minutes);
        return stop;
    }

    private Bus createBus(String plate, BusType type, int capacity) {
        Bus bus = new Bus();
        bus.setPlateNumber(plate);
        bus.setBusType(type);
        bus.setSeatCapacity(capacity);
        bus.setActive(true);
        return bus;
    }

    private Voyage createVoyage(Route route, LocalTime time, BusType type, double price) {
        Voyage v = new Voyage();
        v.setRoute(route);
        v.setDepartureTime(time);
        v.setBusType(type);
        v.setBasePrice(BigDecimal.valueOf(price));
        return v;
    }
}