package cz.mp.construction_site_diary.event;

import java.time.LocalDate;

public record ProjectStatusChangedEvent(
        String projectId,
        String projectName,
        String ownerEmail,
        String managerEmail,
        String status,
        LocalDate date
) {}