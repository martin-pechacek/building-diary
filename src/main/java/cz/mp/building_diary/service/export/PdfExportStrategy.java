package cz.mp.building_diary.service.export;

import cz.mp.building_diary.entity.DiaryEntry;
import cz.mp.building_diary.entity.MaterialUsage;
import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.entity.WorkforceEntry;
import cz.mp.building_diary.exception.ExportException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class PdfExportStrategy implements DiaryExportStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final float MARGIN = 50;

    @Override
    public byte[] export(Project project, List<DiaryEntry> entries) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float yPosition;
            PDPage currentPage;
            PDPageContentStream contentStream;

            // Title page
            currentPage = new PDPage(PDRectangle.A4);
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            yPosition = currentPage.getMediaBox().getHeight() - MARGIN;

            // Title
            contentStream.beginText();
            contentStream.setFont(titleFont, 18);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Building Diary Export");
            contentStream.endText();
            yPosition -= 30;

            // Project info
            contentStream.beginText();
            contentStream.setFont(normalFont, 12);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Project: " + project.getName());
            contentStream.endText();
            yPosition -= 20;

            contentStream.beginText();
            contentStream.setFont(normalFont, 12);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Total entries: " + entries.size());
            contentStream.endText();
            yPosition -= 40;

            // Entries
            for (DiaryEntry entry : entries) {
                // Check if we need a new page
                if (yPosition < 150) {
                    contentStream.close();
                    currentPage = new PDPage(PDRectangle.A4);
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    yPosition = currentPage.getMediaBox().getHeight() - MARGIN;
                }

                // Entry date header
                contentStream.beginText();
                contentStream.setFont(headerFont, 14);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(entry.getDate().format(DATE_FORMATTER));
                contentStream.endText();
                yPosition -= 20;

                // Weather and temperature
                contentStream.beginText();
                contentStream.setFont(normalFont, 10);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Weather: " + entry.getWeatherCondition() + " | Temperature: " + entry.getTemperature() + " C");
                contentStream.endText();
                yPosition -= 15;

                // Summary
                if (entry.getSummary() != null && !entry.getSummary().isEmpty()) {
                    String summary = truncateText(entry.getSummary(), 100);
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 10);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("Summary: " + summary);
                    contentStream.endText();
                    yPosition -= 15;
                }

                // Workforce entries
                if (!entry.getWorkforceEntries().isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 10);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("Workforce (" + entry.getWorkforceEntries().size() + "):");
                    contentStream.endText();
                    yPosition -= 12;

                    for (WorkforceEntry worker : entry.getWorkforceEntries()) {
                        if (yPosition < 50) {
                            contentStream.close();
                            currentPage = new PDPage(PDRectangle.A4);
                            document.addPage(currentPage);
                            contentStream = new PDPageContentStream(document, currentPage);
                            yPosition = currentPage.getMediaBox().getHeight() - MARGIN;
                        }
                        String hours = worker.getWorkingHours() != null ? worker.getWorkingHours() + "h" : "N/A";
                        contentStream.beginText();
                        contentStream.setFont(normalFont, 9);
                        contentStream.newLineAtOffset(MARGIN + 20, yPosition);
                        contentStream.showText("- " + worker.getFirstname() + " " + worker.getLastname() +
                                " (" + worker.getRole() + ") - " + hours);
                        contentStream.endText();
                        yPosition -= 12;
                    }
                }

                // Material usages
                if (!entry.getMaterialUsages().isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 10);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("Materials (" + entry.getMaterialUsages().size() + "):");
                    contentStream.endText();
                    yPosition -= 12;

                    for (MaterialUsage material : entry.getMaterialUsages()) {
                        if (yPosition < 50) {
                            contentStream.close();
                            currentPage = new PDPage(PDRectangle.A4);
                            document.addPage(currentPage);
                            contentStream = new PDPageContentStream(document, currentPage);
                            yPosition = currentPage.getMediaBox().getHeight() - MARGIN;
                        }
                        contentStream.beginText();
                        contentStream.setFont(normalFont, 9);
                        contentStream.newLineAtOffset(MARGIN + 20, yPosition);
                        contentStream.showText("- " + material.getMaterialName() + ": " +
                                material.getQuantity() + " " + material.getUnit());
                        contentStream.endText();
                        yPosition -= 12;
                    }
                }

                yPosition -= 20; // Space between entries
            }

            contentStream.close();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ExportException("Failed to export diary entries to PDF", e);
        }
    }

    private String truncateText(String text, int maxLength) {
        return Optional.ofNullable(text)
                .map(t -> t.length() <= maxLength ? t : t.substring(0, maxLength) + "...")
                .orElse(null);
    }
}