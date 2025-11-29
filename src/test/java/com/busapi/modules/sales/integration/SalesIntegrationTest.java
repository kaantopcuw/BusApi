package com.busapi.modules.sales.integration;

import com.busapi.core.exception.BusinessException;
import com.busapi.modules.fleet.entity.Bus;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.fleet.repository.BusRepository;
import com.busapi.modules.location.entity.City;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.location.repository.CityRepository;
import com.busapi.modules.location.repository.DistrictRepository;
import com.busapi.modules.sales.dto.TicketPurchaseRequest;
import com.busapi.modules.sales.dto.TicketResponse;
import com.busapi.modules.sales.enums.Gender;
import com.busapi.modules.sales.service.TicketService;
import com.busapi.modules.voyage.entity.Route;
import com.busapi.modules.voyage.entity.Trip;
import com.busapi.modules.voyage.entity.Voyage;
import com.busapi.modules.voyage.enums.TripStatus;
import com.busapi.modules.voyage.repository.RouteRepository;
import com.busapi.modules.voyage.repository.TripRepository;
import com.busapi.modules.voyage.repository.VoyageRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest // Tam Spring Context'i ve H2 DB'yi ayağa kaldırır
@ActiveProfiles("test") // application-test.properties varsa kullanır
@Transactional // Her testten sonra DB'yi rollback yapar (temizler)
class SalesIntegrationTest {

    @Autowired private TicketService ticketService;

    // Veri hazırlamak için repositoryler
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private BusRepository busRepository;
    @Autowired private VoyageRepository voyageRepository;
    @Autowired private TripRepository tripRepository;

    private Long tripId;

    @BeforeEach
    void setup() {
        // 1. Şehir ve İlçe Oluştur
        City city = new City(); city.setName("Test City"); city.setPlateCode(99);
        cityRepository.save(city);

        District d1 = new District(); d1.setName("Dist A"); d1.setCity(city);
        District d2 = new District(); d2.setName("Dist B"); d2.setCity(city);
        districtRepository.save(d1);
        districtRepository.save(d2);

        // 2. Rota Oluştur
        Route route = new Route();
        route.setName("A - B Hattı");
        route.setDeparturePoint(d1);
        route.setArrivalPoint(d2);
        routeRepository.save(route);

        // 3. Otobüs Oluştur (Standart 2+2, Cinsiyet kuralı için önemli)
        Bus bus = new Bus();
        bus.setPlateNumber("99 TST 99");
        bus.setBusType(BusType.STANDARD_2_2);
        bus.setSeatCapacity(40);
        busRepository.save(bus);

        // 4. Sefer Şablonu (Voyage)
        Voyage voyage = new Voyage();
        voyage.setRoute(route);
        voyage.setBusType(BusType.STANDARD_2_2);
        voyage.setDepartureTime(LocalTime.of(10, 0));
        voyage.setBasePrice(BigDecimal.valueOf(100));
        voyageRepository.save(voyage);

        // 5. Gerçek Sefer (Trip)
        Trip trip = new Trip();
        trip.setVoyage(voyage);
        trip.setBus(bus);
        trip.setDate(LocalDate.now().plusDays(1)); // Yarına sefer
        trip.setStatus(TripStatus.SCHEDULED);
        tripRepository.save(trip);

        this.tripId = trip.getId();
    }

    @Test
    @DisplayName("Senaryo: Başarılı bilet satışı ve PNR oluşumu")
    void sellTicket_Success() {
        // Given
        TicketPurchaseRequest request = new TicketPurchaseRequest();
        request.setTripId(tripId);
        request.setSeatNumber(1);
        request.setPassengerName("Ahmet");
        request.setPassengerSurname("Yılmaz");
        request.setPassengerTc("12345678901");
        request.setPassengerGender(Gender.MALE);

        // When
        TicketResponse response = ticketService.sellTicket(request);

        // Then
        assertThat(response.getPnrCode()).isNotNull().hasSize(6);
        assertThat(response.getSeatNumber()).isEqualTo(1);
        assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("Senaryo: Cinsiyet Kuralı (Erkek yanına Kadın oturamaz)")
    void sellTicket_GenderRestriction() {
        // 1. Adım: Koltuk 1'e ERKEK bilet alıyor
        TicketPurchaseRequest req1 = new TicketPurchaseRequest();
        req1.setTripId(tripId);
        req1.setSeatNumber(1);
        req1.setPassengerName("Ahmet");
        req1.setPassengerSurname("Yılmaz");
        req1.setPassengerTc("11111111111");
        req1.setPassengerGender(Gender.MALE);
        ticketService.sellTicket(req1);

        // 2. Adım: Koltuk 2'ye (Yanı) KADIN bilet almaya çalışıyor
        TicketPurchaseRequest req2 = new TicketPurchaseRequest();
        req2.setTripId(tripId);
        req2.setSeatNumber(2);
        req2.setPassengerName("Ayşe");
        req2.setPassengerSurname("Yılmaz");
        req2.setPassengerTc("22222222222");
        req2.setPassengerGender(Gender.FEMALE);

        // When & Then
        assertThatThrownBy(() -> ticketService.sellTicket(req2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Farklı cinsiyetteki yolcunun yanına");
    }

    @Test
    @DisplayName("Senaryo: Dolu koltuğa tekrar bilet satılamaz")
    void sellTicket_SeatOccupied() {
        // 1. Adım: Koltuk 5 Satıldı
        TicketPurchaseRequest req1 = new TicketPurchaseRequest();
        req1.setTripId(tripId);
        req1.setSeatNumber(5);
        req1.setPassengerName("Ali");
        req1.setPassengerSurname("Can");
        req1.setPassengerTc("11111111111");
        req1.setPassengerGender(Gender.MALE);
        ticketService.sellTicket(req1);

        // 2. Adım: Aynı koltuğa tekrar istek
        // When & Then
        assertThatThrownBy(() -> ticketService.sellTicket(req1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten dolu");
    }
}