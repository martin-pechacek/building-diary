package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.dto.DiaryEntryDto;
import cz.mp.building_diary.entity.DiaryEntry;
import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.exception.DiaryEntryAlreadyExistsException;
import cz.mp.building_diary.exception.DiaryEntryNotFoundException;
import cz.mp.building_diary.exception.ProjectStateException;
import cz.mp.building_diary.statemachine.states.ProjectStatus;
import cz.mp.building_diary.mapper.DiaryEntryMapper;
import cz.mp.building_diary.mapper.MaterialUsageMapper;
import cz.mp.building_diary.mapper.WorkforceEntryMapper;
import cz.mp.building_diary.repository.DiaryEntryRepository;
import cz.mp.building_diary.repository.ProjectRepository;
import cz.mp.building_diary.service.DiaryEntryService;
import cz.mp.building_diary.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Override
    @Transactional
    public DiaryEntryDto create(UUID projectId, DiaryEntryDto dto) {
        checkConditions(projectId, dto);

        Project project = projectRepository.getReferenceById(projectId);

        DiaryEntry entry = diaryEntryMapper.toEntity(dto, workforceEntryMapper, materialUsageMapper);
        entry.setProject(project);

        diaryEntryRepository.save(entry);
        LOG.info("Diary entry created for project {} on date {}", projectId, dto.date());

        return diaryEntryMapper.toDto(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public DiaryEntryDto getByProjectIdAndDate(UUID projectId, LocalDate date) {
        projectService.hasAccess(projectId);

        DiaryEntry entry = diaryEntryRepository.findByProjectIdAndDate(projectId, date)
                .orElseThrow(() -> new DiaryEntryNotFoundException(
                        "Diary entry not found for project " + projectId + " on date " + date));

        return diaryEntryMapper.toDto(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiaryEntryDto> getAllByProjectId(UUID projectId, Pageable pageable) {
        projectService.hasAccess(projectId);

        return diaryEntryRepository.findByProjectIdOrderByDateDesc(projectId, pageable)
                .map(diaryEntryMapper::toDto);
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
