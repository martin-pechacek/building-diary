package cz.mp.construction_site_diary.mapper;

import cz.mp.construction_site_diary.dto.ProjectDto;
import cz.mp.construction_site_diary.entity.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface ProjectMapper {

    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "constructionManagerId", source = "constructionManager.id")
    ProjectDto toDto(Project project);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Project toEntity(ProjectDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void updateFromDto(ProjectDto dto, @MappingTarget Project project);
}