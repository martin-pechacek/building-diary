package cz.mp.construction_site_diary.strategy;

import cz.mp.construction_site_diary.entity.DiaryEntry;
import cz.mp.construction_site_diary.entity.Project;

import java.util.List;

public interface FileExporterStrategy {

    byte[] export(Project project, List<DiaryEntry> entries);
}