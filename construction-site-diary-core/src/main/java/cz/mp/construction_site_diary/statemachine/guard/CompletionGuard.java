package cz.mp.construction_site_diary.statemachine.guard;

import cz.mp.construction_site_diary.entity.Project;
import cz.mp.construction_site_diary.statemachine.events.ProjectEvent;
import cz.mp.construction_site_diary.statemachine.config.ProjectStateMachineConfig;
import cz.mp.construction_site_diary.statemachine.states.ProjectStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class CompletionGuard implements Guard<ProjectStatus, ProjectEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(CompletionGuard.class);

    // TODO: Inject DiaryEntryRepository when DiaryEntry entity is created
    // private final DiaryEntryRepository diaryEntryRepository;

    @Override
    public boolean evaluate(StateContext<ProjectStatus, ProjectEvent> context) {
        Project project = context.getExtendedState().get(ProjectStateMachineConfig.PROJECT_HEADER, Project.class);

        if (project == null) {
            LOG.warn("Project not found in state context");
            return false;
        }

        if (project.getStartDate() == null) {
            LOG.warn("Project {} has no start date", project.getId());
            return false;
        }

        long expectedDays = ChronoUnit.DAYS.between(project.getStartDate(), LocalDate.now()) + 1;

        // TODO: Replace with actual diary entry count when DiaryEntry entity is created
        // long filledDays = diaryEntryRepository.countByProjectId(project.getId());
        long filledDays = 0; // Placeholder

        boolean allDaysFilled = filledDays >= expectedDays;

        if (!allDaysFilled) {
            LOG.info("Cannot complete project {}: {} days filled out of {} expected",
                    project.getId(), filledDays, expectedDays);
        }

        return allDaysFilled;
    }
}