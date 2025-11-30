package com.busapi.modules.location.bootstrap;

import com.busapi.modules.location.entity.City;
import com.busapi.modules.location.entity.District;
import com.busapi.modules.location.repository.CityRepository;
import com.busapi.modules.location.repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class LocationDataSeeder implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (cityRepository.count() == 0) {
            // İstanbul
            City ist = new City();
            ist.setName("İstanbul");
            ist.setPlateCode(34);
            cityRepository.save(ist);

            District d1 = new District();
            d1.setName("Kadıköy");
            d1.setCity(ist);
            districtRepository.save(d1);

            District d2 = new District();
            d2.setName("Beşiktaş");
            d2.setCity(ist);
            districtRepository.save(d2);

            // Ankara
            City ank = new City();
            ank.setName("Ankara");
            ank.setPlateCode(6);
            cityRepository.save(ank);

            System.out.println("--- LOCATION DATA LOADED ---");
        }
    }
}