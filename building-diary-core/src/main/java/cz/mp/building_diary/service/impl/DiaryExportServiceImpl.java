package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.entity.DiaryEntry;
import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.enums.ExportFormat;
import cz.mp.building_diary.factory.FileExporterFactory;
import cz.mp.building_diary.repository.DiaryEntryRepository;
import cz.mp.building_diary.repository.ProjectRepository;
import cz.mp.building_diary.service.DiaryExportService;
import cz.mp.building_diary.service.ProjectService;
import cz.mp.building_diary.strategy.FileExporterStrategy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryExportServiceImpl implements DiaryExportService {

    private static final Logger LOG = LoggerFactory.getLogger(DiaryExportServiceImpl.class);

    private final DiaryEntryRepository diaryEntryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final FileExporterFactory fileExporterFactory;

    @Override
    @Transactional(readOnly = true)
    public byte[] export(UUID projectId, ExportFormat format) {
        projectService.hasAccess(projectId);

        Project project = projectRepository.getReferenceById(projectId);
        List<DiaryEntry> entries = diaryEntryRepository.findByProjectIdOrderByDateAsc(projectId);

        LOG.info("Exporting {} diary entries to {} for project {}", entries.size(), format, projectId);

        FileExporterStrategy fileExporter = fileExporterFactory.get(format);
        return fileExporter.export(project, entries);
    }
}