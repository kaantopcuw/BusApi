package com.busapi.modules.fleet.mapper;

import com.busapi.modules.fleet.dto.BusResponse;
import com.busapi.modules.fleet.dto.CreateBusRequest;
import com.busapi.modules.fleet.entity.Bus;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusMapper {
    Bus toEntity(CreateBusRequest request);
    BusResponse toResponse(Bus bus);
    List<BusResponse> toResponseList(List<Bus> buses);
}