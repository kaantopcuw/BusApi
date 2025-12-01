package com.busapi.modules.identity.mapper;

import com.busapi.modules.identity.dto.CreateUserRequest;
import com.busapi.modules.identity.dto.UserResponse;
import com.busapi.modules.identity.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // Request -> Entity (Password şifreleme serviste yapılacak, burada ignore ediyoruz)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "agency", ignore = true) // ID'den bulup elle set edeceğiz
    User toEntity(CreateUserRequest request);

    // Entity -> Response
    @Mapping(target = "agencyName", source = "agency.name")
    UserResponse toResponse(User user);
}