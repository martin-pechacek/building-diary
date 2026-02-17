package cz.mp.photos_service.mapper;

import cz.mp.photos_service.dto.PhotoDto;
import cz.mp.photos_service.entity.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PhotoMapper {

    PhotoDto toDto(Photo photo);
}
