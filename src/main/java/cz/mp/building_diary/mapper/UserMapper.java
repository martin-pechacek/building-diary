package cz.mp.building_diary.mapper;

import cz.mp.building_diary.controller.v1.dto.UserRegistrationRequestDto;
import cz.mp.building_diary.controller.v1.dto.UserRegistrationResponseDto;
import cz.mp.building_diary.entity.User;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", source = "keycloakId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRegistrationRequestDto requestDto, String keycloakId);

    @Mapping(target = "userId", source = "keycloakId")
    UserRegistrationResponseDto toResponseDto(User user);

    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "false")
    UserRepresentation toKeycloakUser(UserRegistrationRequestDto requestDto);
}