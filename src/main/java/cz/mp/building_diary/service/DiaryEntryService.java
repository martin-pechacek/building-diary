package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.DiaryEntryDto;

import java.util.UUID;

public interface DiaryEntryService {

    DiaryEntryDto create(UUID projectId, DiaryEntryDto dto);
}