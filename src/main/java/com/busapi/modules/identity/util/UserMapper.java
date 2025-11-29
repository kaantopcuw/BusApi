package com.busapi.modules.identity.util;

import com.busapi.modules.identity.dto.CreateUserRequest;
import com.busapi.modules.identity.dto.UserResponse;
import com.busapi.modules.identity.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {

    // Request -> Entity (Password şifreleme serviste yapılacak, burada ignore ediyoruz)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "agency", ignore = true) // ID'den bulup elle set edeceğiz
    User toEntity(CreateUserRequest request);

    // Entity -> Response
    @Mapping(target = "agencyName", source = "agency.name")
    UserResponse toResponse(User user);
}