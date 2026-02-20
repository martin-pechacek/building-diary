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

@Component
public class StartWorkGuard implements Guard<ProjectStatus, ProjectEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StartWorkGuard.class);

    @Override
    public boolean evaluate(StateContext<ProjectStatus, ProjectEvent> context) {
        Project project = context.getExtendedState().get(ProjectStateMachineConfig.PROJECT_HEADER, Project.class);

        if (project == null) {
            LOG.warn("Project not found in state context");
            return false;
        }

        boolean hasConstructionManager = project.getConstructionManager() != null;
        boolean hasAddress = project.getConstructionSiteAddress() != null;
        boolean hasPermit = project.getPermitNumber() != null
                && !project.getPermitNumber().isBlank();

        if (!hasConstructionManager) {
            LOG.info("Cannot start work on project {}: construction manager not assigned", project.getId());
        }
        if (!hasAddress) {
            LOG.info("Cannot start work on project {}: construction site address not set", project.getId());
        }
        if (!hasPermit) {
            LOG.info("Cannot start work on project {}: permit number not set", project.getId());
        }

        return hasConstructionManager && hasAddress && hasPermit;
    }
}