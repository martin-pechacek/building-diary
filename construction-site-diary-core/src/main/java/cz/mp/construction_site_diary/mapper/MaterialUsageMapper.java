package cz.mp.construction_site_diary.mapper;

import cz.mp.construction_site_diary.dto.MaterialUsageDto;
import cz.mp.construction_site_diary.entity.MaterialUsage;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MaterialUsageMapper {

    MaterialUsageDto toDto(MaterialUsage entity);

    List<MaterialUsageDto> toDtoList(List<MaterialUsage> entities);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    MaterialUsage toEntity(MaterialUsageDto dto);

    List<MaterialUsage> toEntityList(List<MaterialUsageDto> dtos);
}