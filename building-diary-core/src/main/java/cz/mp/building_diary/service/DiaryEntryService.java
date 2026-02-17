package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.DiaryEntryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface DiaryEntryService {

    DiaryEntryDto create(UUID projectId, DiaryEntryDto dto);

    DiaryEntryDto getByProjectIdAndDate(UUID projectId, LocalDate date);

    Page<DiaryEntryDto> getAllByProjectId(UUID projectId, Pageable pageable);

    DiaryEntryDto update(UUID id, DiaryEntryDto dto);
}