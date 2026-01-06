package cz.mp.building_diary.config;

import cz.mp.building_diary.service.export.CsvExportStrategy;
import cz.mp.building_diary.service.export.DiaryExportStrategy;
import cz.mp.building_diary.service.export.ExportFormat;
import cz.mp.building_diary.service.export.PdfExportStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ExportConfig {

    @Bean
    public Map<ExportFormat, DiaryExportStrategy> exportStrategies(
            CsvExportStrategy csvExportStrategy,
            PdfExportStrategy pdfExportStrategy) {
        return Map.of(
                ExportFormat.CSV, csvExportStrategy,
                ExportFormat.PDF, pdfExportStrategy
        );
    }
}