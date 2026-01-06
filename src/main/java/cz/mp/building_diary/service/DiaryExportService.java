package cz.mp.building_diary.service;

import cz.mp.building_diary.service.export.ExportFormat;

import java.util.UUID;

public interface DiaryExportService {

    byte[] export(UUID projectId, ExportFormat format);
}