package cz.mp.construction_site_diary.mapper;

import cz.mp.construction_site_diary.dto.UserRegistrationDto;
import cz.mp.construction_site_diary.entity.User;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", source = "keycloakId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRegistrationDto dto, String keycloakId);

    @Mapping(target = "userId", source = "keycloakId")
    @Mapping(target = "password", ignore = true)
    UserRegistrationDto toDto(User user);

    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "true")
    UserRepresentation toKeycloakUser(UserRegistrationDto dto);
}