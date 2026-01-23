package cz.mp.building_diary.strategy;

import cz.mp.building_diary.entity.DiaryEntry;
import cz.mp.building_diary.entity.Project;

import java.util.List;

public interface FileExporter {

    byte[] export(Project project, List<DiaryEntry> entries);
}