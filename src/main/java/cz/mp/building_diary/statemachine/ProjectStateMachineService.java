package cz.mp.building_diary.statemachine;

import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.statemachine.config.ProjectStateMachineConfig;
import cz.mp.building_diary.statemachine.events.ProjectEvent;
import cz.mp.building_diary.statemachine.states.ProjectStatus;
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

    public void tryTransition(Project project) {
        ProjectEvent event = determineEvent(project);
        if (event == null) {
            return;
        }

        StateMachine<ProjectStatus, ProjectEvent> stateMachine = buildStateMachine(project);
        boolean accepted = stateMachine.sendEvent(event);

        if (accepted) {
            ProjectStatus newStatus = stateMachine.getState().getId();
            project.setStatus(newStatus);

            switch (event) {
                case START_WORK -> project.setStartDate(LocalDate.now());
                case COMPLETE -> project.setEndDate(LocalDate.now());
            }

            LOG.info("Project {} transitioned from {} to {}", project.getId(), project.getStatus(), newStatus);
        }
    }

    private ProjectEvent determineEvent(Project project) {
        return switch (project.getStatus()) {
            case PLANNING -> ProjectEvent.START_WORK;
            case IN_PROGRESS -> ProjectEvent.COMPLETE;
            case COMPLETED -> null;
        };
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