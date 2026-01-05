package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.dto.DiaryEntryDto;
import cz.mp.building_diary.entity.DiaryEntry;
import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.exception.DiaryEntryAlreadyExistsException;
import cz.mp.building_diary.exception.ProjectStateException;
import cz.mp.building_diary.statemachine.states.ProjectStatus;
import cz.mp.building_diary.mapper.DiaryEntryMapper;
import cz.mp.building_diary.mapper.ListMapper;
import cz.mp.building_diary.mapper.MaterialUsageMapper;
import cz.mp.building_diary.mapper.WorkforceEntryMapper;
import cz.mp.building_diary.repository.DiaryEntryRepository;
import cz.mp.building_diary.repository.ProjectRepository;
import cz.mp.building_diary.service.DiaryEntryService;
import cz.mp.building_diary.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryEntryServiceImpl implements DiaryEntryService {

    private static final Logger LOG = LoggerFactory.getLogger(DiaryEntryServiceImpl.class);

    private final DiaryEntryRepository diaryEntryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final DiaryEntryMapper diaryEntryMapper;
    private final WorkforceEntryMapper workforceEntryMapper;
    private final MaterialUsageMapper materialUsageMapper;
    private final ListMapper listMapper;

    @Override
    @Transactional
    public DiaryEntryDto create(UUID projectId, DiaryEntryDto dto) {
        checkConditions(projectId, dto);

        Project project = projectRepository.getReferenceById(projectId);

        DiaryEntry entry = diaryEntryMapper.toEntity(dto);
        entry.setProject(project);

        // Map WorkforceEntryDto List to Entity List
        listMapper.convertAndAdd(dto.workforceEntries(), workforceEntryMapper::toEntity, entry::addWorkforceEntry);
        // Map MaterialUsageDto List to Entity List
        listMapper.convertAndAdd(dto.materialUsages(), materialUsageMapper::toEntity, entry::addMaterialUsage);

        diaryEntryRepository.save(entry);
        LOG.info("Diary entry created for project {} on date {}", projectId, dto.date());

        return diaryEntryMapper.toDto(entry);
    }

    private void checkConditions(UUID projectId, DiaryEntryDto dto) {
        projectService.hasAccess(projectId);
        ProjectStatus status = projectRepository.findStatusById(projectId);
        if (status == ProjectStatus.COMPLETED) {
            throw new ProjectStateException("Cannot add diary entry to a completed project");
        }

        if (diaryEntryRepository.findByProjectIdAndDate(projectId, dto.date()).isPresent()) {
            throw new DiaryEntryAlreadyExistsException(
                    "Diary entry already exists for project " + projectId + " on date " + dto.date());
        }
    }
}
