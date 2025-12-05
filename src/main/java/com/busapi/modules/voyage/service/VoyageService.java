package com.busapi.modules.voyage.service;

import com.busapi.core.entity.types.UserRole;
import com.busapi.core.exception.BusinessException;
import com.busapi.core.exception.ResourceNotFoundException;
import com.busapi.modules.fleet.entity.Bus;
import com.busapi.modules.fleet.repository.BusRepository;
import com.busapi.modules.identity.entity.User;
import com.busapi.modules.identity.service.UserService;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.location.repository.DistrictRepository;
import com.busapi.modules.sales.entity.Ticket;
import com.busapi.modules.sales.repository.TicketRepository;
import com.busapi.modules.voyage.dto.*;
import com.busapi.modules.voyage.entity.Route;
import com.busapi.modules.voyage.entity.RouteStop;
import com.busapi.modules.voyage.entity.Trip;
import com.busapi.modules.voyage.entity.Voyage;
import com.busapi.modules.voyage.enums.TripStatus;
import com.busapi.modules.voyage.repository.RouteRepository;
import com.busapi.modules.voyage.repository.TripRepository;
import com.busapi.modules.voyage.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoyageService {

    private final RouteRepository routeRepository;
    private final VoyageRepository voyageRepository;
    private final TripRepository tripRepository;
    private final DistrictRepository districtRepository;
    private final BusRepository busRepository;
    private final UserService userService;
    private final TicketRepository ticketRepository;

    // --- ROUTE İŞLEMLERİ ---

    @Transactional
    public UUID createRoute(CreateRouteRequest request) {
        Route route = new Route();
        route.setName(request.getName());

        District dep = districtRepository.findById(request.getDepartureDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", request.getDepartureDistrictId()));
        District arr = districtRepository.findById(request.getArrivalDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", request.getArrivalDistrictId()));

        route.setDeparturePoint(dep);
        route.setArrivalPoint(arr);

        // Durakları ekle
        if (request.getStops() != null) {
            for (CreateStopRequest stopReq : request.getStops()) {
                District stopDist = districtRepository.findById(stopReq.getDistrictId())
                        .orElseThrow(() -> new ResourceNotFoundException("District", "id", stopReq.getDistrictId()));

                RouteStop stop = new RouteStop();
                stop.setRoute(route); // İlişkiyi kur
                stop.setDistrict(stopDist);
                stop.setStopOrder(stopReq.getStopOrder());
                stop.setKmFromStart(stopReq.getKmFromStart());
                stop.setDurationMinutesFromStart(stopReq.getDurationMinutesFromStart());

                route.getStops().add(stop);
            }
        }

        Route saved = routeRepository.save(route);
        return saved.getId();
    }

    // --- VOYAGE (SEFER ŞABLONU) İŞLEMLERİ ---

    @Transactional
    public UUID createVoyageDefinition(CreateVoyageRequest request) {
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", request.getRouteId()));

        Voyage voyage = new Voyage();
        voyage.setRoute(route);
        voyage.setDepartureTime(request.getDepartureTime());
        voyage.setBusType(request.getBusType());
        voyage.setBasePrice(request.getBasePrice());

        return voyageRepository.save(voyage).getId();
    }

    // --- TRIP (GÜNLÜK SEFER) OLUŞTURMA ---

    // Bu metod genellikle bir Cron Job veya Admin panelinden "Gelecek 1 haftayı oluştur" butonu ile çağrılır
    @Transactional
    public void generateTripsForDate(LocalDate date) {
        List<Voyage> allVoyages = voyageRepository.findAll();

        for (Voyage voyage : allVoyages) {
            Trip trip = new Trip();
            trip.setVoyage(voyage);
            trip.setDate(date);
            trip.setDepartureDateTime(LocalDateTime.of(date, voyage.getDepartureTime()));
            trip.setStatus(TripStatus.SCHEDULED);
            // Bus ataması daha sonra operasyonel olarak yapılır, başta null olabilir
            tripRepository.save(trip);
        }
    }

    // Belirli bir sefere otobüs atama
    @Transactional
    public void assignBusToTrip(UUID tripId, UUID busId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus", "id", busId));

        // Basit bir validasyon: Otobüs tipi seferin gerektirdiği tipte mi?
        if (trip.getVoyage().getBusType() != bus.getBusType()) {
            throw new RuntimeException("Otobüs tipi uyumsuz! Sefer: " + trip.getVoyage().getBusType());
        }

        trip.setBus(bus);
        tripRepository.save(trip);
    }

    public List<TripResponse> searchTrips(LocalDate date, UUID fromDistrictId, UUID toDistrictId) {
        // Bu basit bir arama. Gerçek hayatta ara duraklardan binişler için daha karmaşık query gerekir.
        // Şimdilik sadece ana kalkış ve varış noktasına göre arıyoruz.
        List<Trip> trips = tripRepository.searchTrips(date, fromDistrictId, toDistrictId);

        return trips.stream().map(trip -> {
            TripResponse res = new TripResponse();
            res.setId(trip.getId());
            res.setRouteName(trip.getVoyage().getRoute().getName());
            res.setDepartureTime(trip.getVoyage().getDepartureTime().toString());
            res.setDate(trip.getDate().toString());
            res.setPrice(trip.getVoyage().getBasePrice()); // Dinamik fiyatlama sonra eklenebilir
            res.setStatus(trip.getStatus().name());
            if (trip.getBus() != null) {
                res.setBusPlateNumber(trip.getBus().getPlateNumber());
            }
            return res;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void assignCrewToTrip(UUID tripId, UUID driverId, UUID hostId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        User driver = userService.findUserEntityById(driverId); // Entity dönen metodu public yapmalısın
        User host = userService.findUserEntityById(hostId); // Entity dönen metodu public yapmalısın

        // Rol kontrolü (Business Rule)
        if (driver.getRole() != UserRole.ROLE_DRIVER) {
            throw new BusinessException("Seçilen kullanıcı Şoför değil!");
        }
        if (host.getRole() != UserRole.ROLE_HOST) {
            throw new BusinessException("Seçilen kullanıcı Muavin değil!");
        }

        trip.setDriver(driver);
        trip.setHost(host);
        tripRepository.save(trip);
    }

    public ManifestResponse getTripManifest(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        List<Ticket> tickets = ticketRepository.findActiveTicketsByTripId(tripId);

        List<ManifestResponse.PassengerInfo> passengers = tickets.stream()
                .map(t -> ManifestResponse.PassengerInfo.builder()
                        .seatNumber(t.getSeatNumber())
                        .fullName(t.getPassengerName() + " " + t.getPassengerSurname())
                        .tcNo(t.getPassengerTc()) // İleride rol kontrolüne göre maskelenebilir
                        .phone(t.getPassengerPhone())
                        .status(t.getStatus())
                        .build())
                .sorted(Comparator.comparingInt(ManifestResponse.PassengerInfo::getSeatNumber))
                .toList();

        return ManifestResponse.builder()
                .tripId(trip.getId())
                .plateNumber(trip.getBus() != null ? trip.getBus().getPlateNumber() : "ATANMADI")
                .routeName(trip.getVoyage().getRoute().getName())
                .date(trip.getDate().toString())
                .time(trip.getVoyage().getDepartureTime().toString())
                .passengers(passengers)
                .build();
    }

    // ... Mevcut kodlar ...

    // --- ARAMA MATRİSİ (PORT MATRIX) ---

    public List<SearchLocationResponse> getAvailableOrigins() {
        List<District> origins = routeRepository.findDistinctDeparturePoints();
        return mapToSearchResponse(origins);
    }

    public List<SearchLocationResponse> getAvailableDestinations(Long fromId) {
        List<District> destinations = routeRepository.findDistinctArrivalPointsByDepartureId(fromId);
        return mapToSearchResponse(destinations);
    }

    // Yardımcı Mapping Metodu
    private List<SearchLocationResponse> mapToSearchResponse(List<District> districts) {
        return districts.stream()
                .map(d -> SearchLocationResponse.builder()
                        .id(d.getId())
                        .cityName(d.getCity().getName())
                        .districtName(d.getName())
                        .label(d.getCity().getName() + " - " + d.getName()) // Frontend'de görünecek kısım
                        .build())
                .toList();
    }

    // --- GÜNCELLENMİŞ MATRİS ALGORİTMASI ---
    public List<RouteMapResponse> getFullRouteMap() {
        List<Route> allRoutes = routeRepository.findAllRoutesWithDetails();

        // Key: Origin ID (Kalkış İlçe ID)
        // Value: Gidilebilecek Varış Noktaları (DestinationDTO Listesi)
        // Map kullanarak aynı kalkış noktasından farklı rotalarla gidilen yerleri birleştiriyoruz.
        Map<UUID, List<DestinationDTO>> adjacencyList = new HashMap<>();

        // Yardımcı Map: ID'den Label bulmak için (Response oluştururken lazım olacak)
        Map<UUID, String> districtIdToLabelMap = new HashMap<>();

        for (Route route : allRoutes) {
            // 1. Güzergahı Sıralı Bir Liste Haline Getir
            // Örn: [Enez, Keşan, Tekirdağ, Silivri, İstanbul]
            List<District> path = new ArrayList<>();

            // Başlangıç
            path.add(route.getDeparturePoint());

            // Ara Duraklar (Sırasına göre)
            route.getStops().stream()
                    .sorted(Comparator.comparingInt(RouteStop::getStopOrder))
                    .forEach(stop -> path.add(stop.getDistrict()));

            // Bitiş
            path.add(route.getArrivalPoint());

            // 2. Matrisi Doldur (Forward Pass)
            // i = Kalkış Adayı, j = Varış Adayı
            for (int i = 0; i < path.size() - 1; i++) {
                District origin = path.get(i);
                String originLabel = origin.getCity().getName() + " - " + origin.getName();
                districtIdToLabelMap.put(origin.getId(), originLabel);

                for (int j = i + 1; j < path.size(); j++) {
                    District dest = path.get(j);
                    String destLabel = dest.getCity().getName() + " - " + dest.getName();

                    // Varış DTO'su
                    DestinationDTO destDTO = DestinationDTO.builder()
                            .id(dest.getId())
                            .label(destLabel)
                            .build();

                    // Listeye Ekle (Varsa getir yoksa yeni liste oluştur)
                    adjacencyList.computeIfAbsent(origin.getId(), k -> new ArrayList<>()).add(destDTO);
                }
            }
        }

        // 3. Map'i Response Formatına Çevir
        List<RouteMapResponse> response = new ArrayList<>();

        for (Map.Entry<UUID, List<DestinationDTO>> entry : adjacencyList.entrySet()) {
            UUID originId = entry.getKey();

            // Gidilecek yerlerde tekrarı önle (Örn: Hem Rota A hem Rota B ile İstanbul'a gidiliyorsa tek gözüksün)
            // distinct() kullanabilmek için DestinationDTO'da equals/hashCode olması lazım veya stream distinct key trick
            List<DestinationDTO> uniqueDestinations = entry.getValue().stream()
                    .filter(distinctByKey(DestinationDTO::getId)) // ID'ye göre tekilleştir
                    .sorted(Comparator.comparing(DestinationDTO::getLabel)) // Alfabetik sırala
                    .collect(Collectors.toList());

            response.add(RouteMapResponse.builder()
                    .originId(originId)
                    .originLabel(districtIdToLabelMap.get(originId))
                    .destinations(uniqueDestinations)
                    .build());
        }

        // Sonuç listesini de Şehir ismine göre sırala
        response.sort(Comparator.comparing(RouteMapResponse::getOriginLabel));

        return response;
    }

    // Stream Distinct için Yardımcı Metod
    private static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        Set<Object> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}