package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.ProjectDto;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectDto create(ProjectDto dto);

    ProjectDto getById(UUID id);

    List<ProjectDto> getAll();

    ProjectDto update(UUID id, ProjectDto dto);

    void archive(UUID id);

    ProjectDto start(UUID id);

    ProjectDto complete(UUID id);
}