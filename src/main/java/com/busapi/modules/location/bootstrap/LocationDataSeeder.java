package com.busapi.modules.location.bootstrap;

import com.busapi.modules.location.entity.City;
import com.busapi.modules.location.entity.Coordinates;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.location.repository.CityRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("!test") // Testlerde çalışmasın, sadece dev/prod
@RequiredArgsConstructor
public class LocationDataSeeder implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Eğer veritabanında şehir varsa tekrar yükleme yapma
        if (cityRepository.count() > 0) {
            log.info("Location data already seeded.");
            return;
        }

        log.info("Seeding location data from JSON...");

        try {
            // 1. JSON Dosyasını Oku
            ClassPathResource resource = new ClassPathResource("data/turkey_districts.json");
            InputStream inputStream = resource.getInputStream();

            // 2. JSON'u Geçici DTO Yapısına Çevir
            JsonRoot root = objectMapper.readValue(inputStream, new TypeReference<JsonRoot>() {});

            List<City> citiesToSave = new ArrayList<>();

            // 3. Mapping (DTO -> Entity)
            for (JsonCity jsonCity : root.getData()) {
                City city = new City();
                city.setPlateCode(jsonCity.getId()); // JSON id = Plaka Kodu
                city.setName(jsonCity.getName());
                city.setPopulation(jsonCity.getPopulation());
                city.setArea(jsonCity.getArea());
                city.setAltitude(jsonCity.getAltitude());
                city.setCoastal(jsonCity.isCoastal());
                city.setMetropolitan(jsonCity.isMetropolitan());

                // Alan Kodları
                if (jsonCity.getAreaCode() != null) {
                    city.setAreaCodes(jsonCity.getAreaCode());
                }

                // Koordinatlar
                if (jsonCity.getCoordinates() != null) {
                    city.setCoordinates(new Coordinates(
                            jsonCity.getCoordinates().getLatitude(),
                            jsonCity.getCoordinates().getLongitude()
                    ));
                }

                // Maps (OpenStreetMap URL)
                if (jsonCity.getMaps() != null) {
                    city.setOpenStreetMap(jsonCity.getMaps().get("openStreetMap"));
                    city.setGoogleMapUrl(jsonCity.getMaps().get("googleMaps"));
                }

                if (jsonCity.getRegion() != null) {
                    city.setRegion(jsonCity.getRegion().get("tr"));
                    city.setRegion(jsonCity.getRegion().get("en"));
                }

                // İlçeler
                if (jsonCity.getDistricts() != null) {
                    List<District> districts = new ArrayList<>();
                    for (JsonDistrict jsonDistrict : jsonCity.getDistricts()) {
                        District district = new District();
                        district.setName(jsonDistrict.getName());
                        district.setPopulation(jsonDistrict.getPopulation());
                        district.setArea(jsonDistrict.getArea());
                        district.setCity(city); // İlişkiyi kur
                        districts.add(district);
                    }
                    city.setDistricts(districts);
                }

                citiesToSave.add(city);
            }

            // 4. Toplu Kayıt
            cityRepository.saveAll(citiesToSave);
            log.info("Successfully seeded {} cities and their districts.", citiesToSave.size());

        } catch (Exception e) {
            log.error("Failed to seed location data", e);
        }
    }

    // --- JSON MAPPING DTOs (Sadece bu işlem için kullanılan iç sınıflar) ---

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class JsonRoot {
        private String status;
        private List<JsonCity> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class JsonCity {
        private int id; // Plaka Kodu
        private String name;
        private Long population;
        private Integer area;
        private Integer altitude;
        private List<Integer> areaCode;
        private boolean isCoastal;
        private boolean isMetropolitan;
        private JsonCoordinates coordinates;
        private Map<String, String> maps; // googleMaps, openStreetMap
        private Map<String, String> region; // en, tr
        private List<JsonDistrict> districts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class JsonDistrict {
        private int id;
        private String name;
        private Long population;
        private Integer area;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class JsonCoordinates {
        private Double latitude;
        private Double longitude;
    }
}