package com.busapi.modules.report.integration;

import com.busapi.core.entity.types.UserRole;
import com.busapi.modules.fleet.entity.Bus;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.fleet.repository.BusRepository;
import com.busapi.modules.identity.entity.User;
import com.busapi.modules.identity.repository.UserRepository;
import com.busapi.modules.location.entity.City;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.location.repository.CityRepository;
import com.busapi.modules.location.repository.DistrictRepository;
import com.busapi.modules.report.dto.CreateExpenseRequest;
import com.busapi.modules.report.dto.DashboardStatsResponse;
import com.busapi.modules.report.enums.ExpenseType;
import com.busapi.modules.report.service.ReportService;
import com.busapi.modules.sales.entity.Ticket;
import com.busapi.modules.sales.entity.TicketOrder;
import com.busapi.modules.sales.enums.Gender;
import com.busapi.modules.sales.enums.TicketStatus;
import com.busapi.modules.sales.repository.TicketOrderRepository;
import com.busapi.modules.sales.repository.TicketRepository;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportIntegrationTest {

    @Autowired private ReportService reportService;

    // Repositoryler
    @Autowired private TicketRepository ticketRepository;
    @Autowired private TicketOrderRepository orderRepository; // Sipariş için gerekli
    @Autowired private UserRepository userRepository; // Order buyer için
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private BusRepository busRepository;
    @Autowired private VoyageRepository voyageRepository;
    @Autowired private TripRepository tripRepository;

    @BeforeEach
    void setup() {
        // 1. Altyapı (User, City, Route, Bus, Voyage, Trip)

        // Siparişi yapan kullanıcı (Opsiyonel ama iyi pratik)
        User buyer = new User();
        buyer.setFirstName("Test"); buyer.setLastName("Buyer"); buyer.setEmail("buyer@test.com");
        buyer.setPassword("pass"); buyer.setPhoneNumber("555"); buyer.setRole(UserRole.ROLE_CUSTOMER);
        userRepository.save(buyer);

        City city = new City(); city.setName("Report City"); city.setPlateCode(88);
        cityRepository.save(city);
        District d1 = new District(); d1.setName("Dist X"); d1.setCity(city);
        District d2 = new District(); d2.setName("Dist Y"); d2.setCity(city);
        districtRepository.save(d1); districtRepository.save(d2);

        Route route = new Route(); route.setName("Report Route"); route.setDeparturePoint(d1); route.setArrivalPoint(d2);
        routeRepository.save(route);

        Bus bus = new Bus(); bus.setPlateNumber("88 TST 88"); bus.setBusType(BusType.STANDARD_2_2); bus.setSeatCapacity(40);
        busRepository.save(bus);

        Voyage voyage = new Voyage();
        voyage.setRoute(route);
        voyage.setBusType(BusType.STANDARD_2_2);
        voyage.setDepartureTime(LocalTime.of(10, 0));
        voyage.setBasePrice(BigDecimal.valueOf(100));

        // --- EKLENEN KISIM (Zorunlu Alanlar) ---
        voyage.setValidFrom(LocalDate.now().minusDays(1)); // Dünden beri geçerli
        voyage.setValidTo(LocalDate.now().plusYears(1));   // 1 Yıl geçerli
        voyage.setDaysOfWeek(Set.of(DayOfWeek.values()));  // Her gün çalışıyor
        voyageRepository.save(voyage);

        Trip trip = new Trip(); trip.setVoyage(voyage); trip.setBus(bus); trip.setDate(LocalDate.now()); trip.setStatus(TripStatus.SCHEDULED);
        tripRepository.save(trip);

        // 2. SİPARİŞ (ORDER) VE BİLET (TICKET) OLUŞTURMA
        // Yeni yapıda biletler tek başına havada duramaz, bir Order'a bağlı olmalıdır.

        TicketOrder order = new TicketOrder();
        order.setBuyer(buyer);
        order.setContactEmail("buyer@test.com");
        order.setContactPhone("5551112233");
        order.setOrderPnr("PNR-REPORT-001"); // Order PNR buraya geldi
        order.setTotalAmount(BigDecimal.valueOf(200)); // 2 bilet * 100

        // Bilet 1
        Ticket t1 = new Ticket();
        t1.setTrip(trip);
        t1.setOrder(order); // İlişki kuruldu
        t1.setSeatNumber(1);
        t1.setPrice(BigDecimal.valueOf(100));
        t1.setStatus(TicketStatus.SOLD);
        t1.setPassengerName("A"); t1.setPassengerSurname("B"); t1.setPassengerTc("1"); t1.setPassengerGender(Gender.MALE);
        // t1.setPnrCode("PNR001"); // BU ARTIK YOK, SİLDİK.

        // Bilet 2
        Ticket t2 = new Ticket();
        t2.setTrip(trip);
        t2.setOrder(order); // İlişki kuruldu
        t2.setSeatNumber(2);
        t2.setPrice(BigDecimal.valueOf(100));
        t2.setStatus(TicketStatus.SOLD);
        t2.setPassengerName("C"); t2.setPassengerSurname("D"); t2.setPassengerTc("2"); t2.setPassengerGender(Gender.MALE);

        // Listeye ekle (Cascade Save için)
        order.getTickets().add(t1);
        order.getTickets().add(t2);

        // Sadece Order'ı kaydetmek yeterli, CascadeType.ALL sayesinde biletler de kaydedilir.
        orderRepository.save(order);

        // 3. Gider Ekleme
        CreateExpenseRequest expenseReq = new CreateExpenseRequest();
        expenseReq.setTitle("Mazot");
        expenseReq.setAmount(BigDecimal.valueOf(50));
        expenseReq.setExpenseDate(LocalDate.now());
        expenseReq.setExpenseType(ExpenseType.FUEL);
        reportService.addExpense(expenseReq);
    }

    @Test
    @DisplayName("Dashboard hesaplaması doğru çalışmalı: (200 Ciro - 50 Gider = 150 Kar)")
    void getMonthlyStats_ShouldCalculateCorrectly() {
        // When
        DashboardStatsResponse stats = reportService.getMonthlyStats();

        // Then
        // Toplam Ciro: 200
        assertThat(stats.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(200));

        // Toplam Gider: 50
        assertThat(stats.getTotalExpense()).isEqualByComparingTo(BigDecimal.valueOf(50));

        // Net Kar: 150
        assertThat(stats.getNetProfit()).isEqualByComparingTo(BigDecimal.valueOf(150));

        // Toplam Bilet: 2
        assertThat(stats.getTotalTicketsSold()).isEqualTo(2);
    }
}