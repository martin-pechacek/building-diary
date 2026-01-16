package cz.mp.building_diary.config;

import cz.mp.building_diary.strategy.export.CsvExportStrategy;
import cz.mp.building_diary.strategy.DiaryExportStrategy;
import cz.mp.building_diary.enums.ExportFormat;
import cz.mp.building_diary.strategy.export.PdfExportStrategy;
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