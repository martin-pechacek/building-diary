package cz.mp.building_diary.mapper;

import cz.mp.building_diary.dto.PhotoDto;
import cz.mp.building_diary.entity.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PhotoMapper {

    PhotoDto toDto(Photo photo);
}