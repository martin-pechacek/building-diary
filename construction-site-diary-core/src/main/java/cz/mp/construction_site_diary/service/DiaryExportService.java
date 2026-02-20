package cz.mp.construction_site_diary.service;

import cz.mp.construction_site_diary.enums.ExportFormat;

import java.util.UUID;

public interface DiaryExportService {

    byte[] export(UUID projectId, ExportFormat format);
}