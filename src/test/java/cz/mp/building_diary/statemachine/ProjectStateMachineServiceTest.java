package cz.mp.building_diary.statemachine;

import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.statemachine.events.ProjectEvent;
import cz.mp.building_diary.statemachine.states.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.config.StateMachineFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ProjectStateMachineServiceTest {

    @Mock
    private StateMachineFactory<ProjectStatus, ProjectEvent> stateMachineFactory;

    @Mock
    private StateMachine<ProjectStatus, ProjectEvent> stateMachine;

    @Mock
    private StateMachineAccessor<ProjectStatus, ProjectEvent> accessor;

    @Mock
    private ExtendedState extendedState;

    @Mock
    private State<ProjectStatus, ProjectEvent> state;

    private ProjectStateMachineService service;

    @BeforeEach
    void setUp() {
        service = new ProjectStateMachineService(stateMachineFactory);
    }

    @Nested
    class TryTransition {

        @BeforeEach
        void setUpStateMachine() {
            when(stateMachineFactory.getStateMachine(any(String.class))).thenReturn(stateMachine);
            when(stateMachine.stopReactively()).thenReturn(Mono.empty());
            when(stateMachine.startReactively()).thenReturn(Mono.empty());
            when(stateMachine.getStateMachineAccessor()).thenReturn(accessor);
            when(stateMachine.getExtendedState()).thenReturn(extendedState);
            when(extendedState.getVariables()).thenReturn(new java.util.HashMap<>());

            doAnswer(inv -> {
                java.util.function.Consumer<org.springframework.statemachine.access.StateMachineAccess<ProjectStatus, ProjectEvent>> consumer = inv.getArgument(0);
                org.springframework.statemachine.access.StateMachineAccess<ProjectStatus, ProjectEvent> accessMock = mock(org.springframework.statemachine.access.StateMachineAccess.class);
                when(accessMock.resetStateMachineReactively(any())).thenReturn(Mono.empty());
                consumer.accept(accessMock);
                return null;
            }).when(accessor).doWithAllRegions(any());
        }

        @Test
        void shouldTransitionFromPlanningToInProgress() {
            Project project = createProject(ProjectStatus.PLANNING);

            when(stateMachine.sendEvent(ProjectEvent.START_WORK)).thenReturn(true);
            when(stateMachine.getState()).thenReturn(state);
            when(state.getId()).thenReturn(ProjectStatus.IN_PROGRESS);

            service.tryTransition(project);

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
            assertThat(project.getStartDate()).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldTransitionFromInProgressToCompleted() {
            Project project = createProject(ProjectStatus.IN_PROGRESS);
            project.setStartDate(LocalDate.now().minusDays(10));

            when(stateMachine.sendEvent(ProjectEvent.COMPLETE)).thenReturn(true);
            when(stateMachine.getState()).thenReturn(state);
            when(state.getId()).thenReturn(ProjectStatus.COMPLETED);

            service.tryTransition(project);

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
            assertThat(project.getEndDate()).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldNotTransitionWhenEventRejected() {
            Project project = createProject(ProjectStatus.PLANNING);

            when(stateMachine.sendEvent(ProjectEvent.START_WORK)).thenReturn(false);

            service.tryTransition(project);

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.PLANNING);
            assertThat(project.getStartDate()).isNull();
        }

        @Test
        void shouldNotTransitionWhenAlreadyCompleted() {
            Project project = createProject(ProjectStatus.COMPLETED);

            service.tryTransition(project);

            verify(stateMachine, never()).sendEvent(any(ProjectEvent.class));
        }
    }

    private Project createProject(ProjectStatus status) {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Test Project");
        project.setStatus(status);
        return project;
    }
}