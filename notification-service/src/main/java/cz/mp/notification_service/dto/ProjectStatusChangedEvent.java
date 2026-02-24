package cz.mp.notification_service.dto;

import java.time.LocalDate;

public record ProjectStatusChangedEvent(
        String projectId,
        String projectName,
        String ownerEmail,
        String managerEmail,
        String status,
        LocalDate date
) {}