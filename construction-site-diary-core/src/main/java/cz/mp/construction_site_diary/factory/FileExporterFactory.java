package cz.mp.construction_site_diary.factory;

import cz.mp.construction_site_diary.enums.ExportFormat;
import cz.mp.construction_site_diary.strategy.FileExporterStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FileExporterFactory {

    private final Map<String, FileExporterStrategy> fileExporters;

    public FileExporterStrategy get(ExportFormat exportFileType) {

        return Optional.ofNullable(exportFileType)
                .map(Enum::name)
                .map(fileExporters::get)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported file type"));
    }
}