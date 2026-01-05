package cz.mp.building_diary.mapper;

import cz.mp.building_diary.dto.WorkforceEntryDto;
import cz.mp.building_diary.entity.WorkforceEntry;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkforceEntryMapper {

    WorkforceEntryDto toDto(WorkforceEntry entity);

    List<WorkforceEntryDto> toDtoList(List<WorkforceEntry> entities);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    WorkforceEntry toEntity(WorkforceEntryDto dto);

    List<WorkforceEntry> toEntityList(List<WorkforceEntryDto> dtos);
}