package cz.mp.building_diary.mapper;

import cz.mp.building_diary.dto.AddressDto;
import cz.mp.building_diary.entity.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressDto toDto(Address address);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Address toEntity(AddressDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void updateFromDto(AddressDto dto, @MappingTarget Address address);
}