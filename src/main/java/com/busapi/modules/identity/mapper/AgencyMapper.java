package com.busapi.modules.identity.mapper;

import com.busapi.modules.identity.dto.AgencyResponse;
import com.busapi.modules.identity.dto.CreateAgencyRequest;
import com.busapi.modules.identity.entity.Agency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AgencyMapper {

    @Mapping(target = "location", ignore = true) // Service içinde set edeceğiz
    Agency toEntity(CreateAgencyRequest request);

    @Mapping(target = "districtName", source = "location.name")
    @Mapping(target = "cityName", source = "location.city.name")
    AgencyResponse toResponse(Agency agency);
}