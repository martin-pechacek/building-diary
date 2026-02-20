package cz.mp.construction_site_diary.mapper;

import cz.mp.construction_site_diary.dto.DiaryEntryDto;
import cz.mp.construction_site_diary.entity.DiaryEntry;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {WorkforceEntryMapper.class, MaterialUsageMapper.class})
public interface DiaryEntryMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "createdById", source = "project.createdBy.id")
    DiaryEntryDto toDto(DiaryEntry entity);

    List<DiaryEntryDto> toDtoList(List<DiaryEntry> entities);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "workforceEntries", ignore = true)
    @Mapping(target = "materialUsages", ignore = true)
    DiaryEntry toEntity(DiaryEntryDto dto,
                        @Context WorkforceEntryMapper workforceMapper,
                        @Context MaterialUsageMapper materialMapper);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "workforceEntries", ignore = true)
    @Mapping(target = "materialUsages", ignore = true)
    void updateEntity(DiaryEntryDto dto,
                      @MappingTarget DiaryEntry entry,
                      @Context WorkforceEntryMapper workforceMapper,
                      @Context MaterialUsageMapper materialMapper);

    @AfterMapping
    default void mapCollections(DiaryEntryDto dto,
                                @MappingTarget DiaryEntry entry,
                                @Context WorkforceEntryMapper workforceMapper,
                                @Context MaterialUsageMapper materialMapper) {
        entry.getWorkforceEntries().clear();
        entry.getMaterialUsages().clear();

        if (dto.workforceEntries() != null) {
            dto.workforceEntries().stream()
                    .map(workforceMapper::toEntity)
                    .forEach(entry::addWorkforceEntry);
        }
        if (dto.materialUsages() != null) {
            dto.materialUsages().stream()
                    .map(materialMapper::toEntity)
                    .forEach(entry::addMaterialUsage);
        }
    }
}