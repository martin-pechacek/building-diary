package cz.mp.building_diary.strategy.export;

import cz.mp.building_diary.entity.DiaryEntry;
import cz.mp.building_diary.entity.MaterialUsage;
import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.entity.WorkforceEntry;
import cz.mp.building_diary.enums.ExportFormat;
import cz.mp.building_diary.exception.ExportException;
import cz.mp.building_diary.strategy.FileExporter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component(ExportFormat.Values.CSV)
public class CsvExporter implements FileExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public byte[] export(Project project, List<DiaryEntry> entries) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("Date", "Weather", "Temperature (C)", "Summary",
                             "Workforce Count", "Total Working Hours", "Workforce Details",
                             "Materials Count", "Material Details")
                     .build())) {

            entries.forEach(e -> {
                String workforceDetails = formatWorkforceDetails(e.getWorkforceEntries());
                String materialDetails = formatMaterialDetails(e.getMaterialUsages());
                double totalHours = e.getWorkforceEntries().stream()
                        .filter(w -> w.getWorkingHours() != null)
                        .mapToDouble(w -> w.getWorkingHours().doubleValue())
                        .sum();

                try {
                    csvPrinter.printRecord(
                            e.getDate().format(DATE_FORMATTER),
                            e.getWeatherCondition(),
                            e.getTemperature(),
                            e.getSummary() != null ? e.getSummary() : "",
                            e.getWorkforceEntries().size(),
                            totalHours,
                            workforceDetails,
                            e.getMaterialUsages().size(),
                            materialDetails
                    );
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            csvPrinter.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ExportException("Failed to export diary entries to CSV", e);
        }
    }

    private String formatWorkforceDetails(List<WorkforceEntry> entries) {
        if (entries.isEmpty()) {
            return "";
        }
        return entries.stream()
                .map(w -> {
                    String hours = w.getWorkingHours() != null ? w.getWorkingHours() + "h" : "";
                    return w.getFirstname() + " " + w.getLastname() + " (" + w.getRole() + ") " + hours;
                })
                .collect(Collectors.joining("; "));
    }

    private String formatMaterialDetails(List<MaterialUsage> usages) {
        if (usages.isEmpty()) {
            return "";
        }
        return usages.stream()
                .map(m -> m.getMaterialName() + ": " + m.getQuantity() + " " + m.getUnit())
                .collect(Collectors.joining("; "));
    }
}