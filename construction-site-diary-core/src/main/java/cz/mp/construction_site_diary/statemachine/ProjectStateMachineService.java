package cz.mp.construction_site_diary.statemachine;

import cz.mp.construction_site_diary.entity.Project;
import cz.mp.construction_site_diary.statemachine.config.ProjectStateMachineConfig;
import cz.mp.construction_site_diary.statemachine.events.ProjectEvent;
import cz.mp.construction_site_diary.statemachine.states.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProjectStateMachineService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectStateMachineService.class);

    private final StateMachineFactory<ProjectStatus, ProjectEvent> stateMachineFactory;

    public boolean sendEvent(Project project, ProjectEvent event) {
        StateMachine<ProjectStatus, ProjectEvent> stateMachine = buildStateMachine(project);
        ProjectStatus originalStatus = project.getStatus();

        stateMachine.sendEvent(event);

        ProjectStatus newStatus = stateMachine.getState().getId();
        boolean transitioned = !originalStatus.equals(newStatus);

        if (transitioned) {
            project.setStatus(newStatus);

            switch (event) {
                case START_WORK -> project.setStartDate(LocalDate.now());
                case COMPLETE -> project.setEndDate(LocalDate.now());
            }

            LOG.info("Project {} transitioned from {} to {}", project.getId(), originalStatus, newStatus);
        } else {
            LOG.info("Project {} transition with event {} rejected (guard failed)", project.getId(), event);
        }

        return transitioned;
    }

    private StateMachine<ProjectStatus, ProjectEvent> buildStateMachine(Project project) {
        StateMachine<ProjectStatus, ProjectEvent> stateMachine = stateMachineFactory.getStateMachine(project.getId().toString());

        stateMachine.stopReactively().block();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(accessor -> accessor.resetStateMachineReactively(
                        new DefaultStateMachineContext<>(project.getStatus(), null, null, null)
                ).block());

        stateMachine.getExtendedState().getVariables().put(ProjectStateMachineConfig.PROJECT_HEADER, project);

        stateMachine.startReactively().block();

        return stateMachine;
    }
}