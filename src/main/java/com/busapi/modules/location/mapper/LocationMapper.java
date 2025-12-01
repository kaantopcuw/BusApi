package com.busapi.modules.location.mapper;

import com.busapi.modules.location.dto.CityResponse;
import com.busapi.modules.location.dto.CreateCityRequest;
import com.busapi.modules.location.dto.CreateDistrictRequest;
import com.busapi.modules.location.dto.DistrictResponse;
import com.busapi.modules.location.entity.City;
import com.busapi.modules.location.entity.District;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {

    City toCity(CreateCityRequest request);

    CityResponse toCityResponse(City city);
    List<CityResponse> toCityResponseList(List<City> cities);

    @Mapping(target = "city", ignore = true) // İlişkiyi service katmanında kuracağız
    District toDistrict(CreateDistrictRequest request);

    @Mapping(target = "cityId", source = "city.id")
    DistrictResponse toDistrictResponse(District district);

    List<DistrictResponse> toDistrictResponseList(List<District> districts);
}